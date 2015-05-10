package com.glomming.shared.sgs.service;

import com.glomming.shared.sgs.bean.*;
import com.glomming.shared.sgs.exception.*;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.ScanResult;

import java.text.ParseException;
import java.util.*;

@Component
public class SimpleGroupService extends BaseGroupService {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SimpleGroupService.class);

  /**
   * Create group
   * Set group by id
   * Set id by group
   * Set groups by member
   * Set members by group
   * Increment member count
   *
   * @param appName
   * @param groupName
   * @param maxSize
   * @param ownerId
   * @param groupJoinState
   * @return
   */
  public String createGroup(String appName, String groupName, long maxSize, String ownerId, GroupJoinState groupJoinState) throws Exception {
    String uuid = UUID.randomUUID().toString();
    String groupId = Group.getKey(uuid);
    Date now = Calendar.getInstance().getTime();
    // Set group id by name
    long result = mapGroupIdByName(appName, groupName, groupId);
    String createGroupResult = null;
    if (result > 0) {
      // Continue with group creation
      Group group = new Group(groupId, appName, groupName, maxSize, ownerId, groupJoinState, now, now);
      createGroupResult = jedisCluster.hmset(groupId, group.toMap());
      // Set first group member
      this.addGroupMember(groupId, ownerId);
      if (createGroupResult.equals(RedisOk))
        return groupId;
    }
    // Group already exists
    return null;
  }

  /**
   * Delete group, members, group id-name mapping and users to groups mapping
   * @param groupId
   */
  public void deleteGroup(String groupId) throws InvalidGroupException, OwnerLeaveGroupException {

    Group group = this.findGroup(groupId);
    if (group != null) {
      unmapGroupIdByName(group.appName, group.name);
      // Get group members
      Set<String> members = this.getMembersByGroup(groupId);
      for (String member : members) {
        this.removeGroupFromUser(member, groupId);
      }
      // Now remove set of users
      String key = SetGroupMembers.getKey(groupId);
      jedisCluster.del(key);
      // Remove the group
      jedisCluster.del(groupId);
    }
  }

  /**
   * Get group members
   * @param groupId
   * @return
   */
  public Set<String> getMembersByGroup(String groupId) {
    String key = SetGroupMembers.getKey(groupId);
    return jedisCluster.smembers(key);
  }


  /**
   * Get paginated members
   * @param groupId
   * @param cursor
   * @return
   */
  public ResultWithCursor<String> getPaginatedMembers(String groupId, String cursor) {
    String key = SetGroupMembers.getKey(groupId);
    ScanResult<String> scanResult = jedisCluster.sscan(key, cursor);
    ResultWithCursor<String> resultWithCursor = new ResultWithCursor<>();
    resultWithCursor.cursor = scanResult.getStringCursor();
    resultWithCursor.results = scanResult.getResult();
    return resultWithCursor;
  }

  /**
   * Map group id to name. A failure implies that group already exists
   *
   * @param appName
   * @param groupName
   * @param groupId
   */
  public long mapGroupIdByName(String appName, String groupName, String groupId) {
    String groupIdByNameKey = GroupIdByName.getKey(appName);
    Long result = jedisCluster.hsetnx(groupIdByNameKey, groupName, groupId);
    return result;
  }

  /**
   * Remove both group name by id and group id by name mappings
   *
   * @param appName
   * @param groupName
   */
  public void unmapGroupIdByName(String appName, String groupName) {
    String groupIdByNameKey = GroupIdByName.getKey(appName);
    jedisCluster.hdel(groupIdByNameKey, groupName);
  }

  /**
   * Get group meta data
   *
   * @param appName
   * @param groupName
   * @return
   */
  public Group findGroup(String appName, String groupName) throws InvalidGroupException {
    // Get groupId by appName and groupName
    String groupIdByNameKey = GroupIdByName.getKey(appName);
    String groupId = jedisCluster.hget(groupIdByNameKey, groupName);
    if (StringUtils.isEmpty(groupId)) {
      return null;
    }
    return this.findGroup(Group.getKey(groupId));
  }

  /**
   * Find group by group id
   * @param groupId
   * @return
   */
  public Group findGroup(String groupId) throws InvalidGroupException {
    Map<String, String> groupMap = jedisCluster.hgetAll(Group.getKey(groupId));
    Group group = null;
    try {
      if (!groupMap.isEmpty()) {
        group = Group.fromMap(groupMap);
      } else {
        throw new InvalidGroupException("", "Invalid groupId: " + groupId);
      }
    } catch (ParseException e) {
      logger.error("", e);
    }
    return group;
  }

  /**
   * Update group name, check if new name is available before updating
   * @param groupId
   * @param newName
   * @return
   */
  public String updateGroupName(String groupId, String newName) throws InvalidGroupException, InvalidGroupNameException {
    String updateNameResult = null;
    Group group = this.findGroup(groupId);
    if (group != null) {
      // Check if group exists
      long result = mapGroupIdByName(group.appName, newName, groupId);
      if (result > 0) {
        // Ok to update name
        Map<String, String> updates = new HashMap<>();
        updates.put(Group.NAME, newName);
        updates.put(Group.LAST_UPDATED, Group.dateFormat.format(Calendar.getInstance().getTime()));
        updateNameResult = jedisCluster.hmset(groupId, updates);
      } else {
        throw new InvalidGroupNameException("", "GroupName already taken : " + newName);
      }
    }
    return updateNameResult;
  }

  /**
   * Change the owner for a group. New owner has to be member
   * @param groupId
   * @param newOwnerId
   * @return
   */
  public String updateOwner(String groupId, String newOwnerId) {
    Map<String, String> updates = new HashMap<>();
    updates.put(Group.OWNER_ID, newOwnerId);
    updates.put(Group.LAST_UPDATED, Group.dateFormat.format(Calendar.getInstance().getTime()));
    String result = jedisCluster.hmset(groupId, updates);
    return result;
  }

  /**
   * Check if new max size is greater than current size
   * @param groupId
   * @param newMaxSize
   * @return
   */
  public String updateMaxSize(String groupId, long newMaxSize) throws InvalidGroupException, InvalidParameterException {
    Group group = this.findGroup(groupId);
    String result = null;
    if (group.currentSize < newMaxSize) {
      Map<String, String> updates = new HashMap<>();
      updates.put(Group.MAX_SIZE, Long.toString(newMaxSize));
      updates.put(Group.LAST_UPDATED, Group.dateFormat.format(Calendar.getInstance().getTime()));
      result = jedisCluster.hmset(groupId, updates);
    } else {
      throw new InvalidParameterException("", "Max size cannot be lower than number of members :" + group.toString());
    }
    return result;
  }

  /**
   * Cannot be called externally, called only when adding or removing group members
   * @param groupId
   * @param currentSize
   * @return
   */
  private String updateCurrentSize(String groupId, long currentSize) {
    Map<String, String> updates = new HashMap<>();
    updates.put(Group.CURRENT_SIZE, Long.toString(currentSize));
    updates.put(Group.LAST_UPDATED, Group.dateFormat.format(Calendar.getInstance().getTime()));
    String result = jedisCluster.hmset(groupId, updates);
    return result;
  }

  /**
   * Add member to group. Check max members not exceeded
   * Update member count
   * @param groupId
   * @param userId
   */
  public String addGroupMember(String groupId, String userId) throws GroupMembershipExceededException, InvalidGroupException {
    String result = null;
    Group group = this.findGroup(groupId);
    if (group != null) {
      // Check if there is room to add 1 more
      if (group.maxSize > 0 && group.currentSize < group.maxSize) {
        String key = SetGroupMembers.getKey(groupId);
        long numAdded = jedisCluster.sadd(key, userId);
        if (numAdded > 0) {
          // Update group with new size
          long groupSize = jedisCluster.scard(key);
          result = this.updateCurrentSize(groupId, groupSize);
          // Add this group to user's list of groups
          this.addGroupToUser(userId, groupId);
        }
      } else {
        // Cannot add new members, capacity reached
        throw new GroupMembershipExceededException("", "Unable to add member : " + group.toString());
      }
    } else {
      throw new InvalidGroupException("", "Unable to find group with id " + groupId);
    }
    return result;
  }

  /**
   * Remove member from group. Cannot remove owner
   * Update member count
   * @param groupId
   * @param userId
   */
  public String removeGroupMember(String groupId, String userId) throws InvalidGroupException, OwnerLeaveGroupException {
    String result = null;
    Group group = this.findGroup(groupId);
    if (group != null) {
      if (group.ownerId.equals(userId)) {
        throw new OwnerLeaveGroupException("", "");
      }
      String key = SetGroupMembers.getKey(groupId);
      long numRemoved = jedisCluster.srem(key, userId);
      if (numRemoved > 0) {
        // Update group with new size
        long groupSize = jedisCluster.scard(key);
        result = this.updateCurrentSize(groupId, groupSize);
        // Remove group from list of groups this user is a member of
        this.removeGroupFromUser(userId, groupId);
      }
    } else {
      throw new InvalidGroupException("", "Unable to find group with id " + groupId);
    }
    return result;
  }

  /**
   * Add this group to the set of groups a user is a member of
   * @param userId
   * @param groupId
   * @return
   */
  public long addGroupToUser(String userId, String groupId) {
    String key = SetUserGroupMembership.getKey(userId);
    Long numAdded = jedisCluster.sadd(key, groupId);
    return numAdded;
  }

  /**
   * Remove this group from a the set of groups a user is member of
   * @param userId
   * @param groupId
   * @return
   */
  public long removeGroupFromUser(String userId, String groupId) {
    String key = SetUserGroupMembership.getKey(userId);
    Long numRemoved = jedisCluster.srem(key, groupId);
    return numRemoved;
  }

  /**
   * Return all the groups a user is member of
   * @param userId
   * @return
   */
  public Set<String> getGroupMembershipsByUser(String userId) {
    String key = SetUserGroupMembership.getKey(userId);
    return jedisCluster.smembers(key);
  }

  /**
   * Get number of groups a user is member of
   * @param userId
   * @return
   */
  public long getCountGroupMembershipsByUser(String userId) {
    String key = SetUserGroupMembership.getKey(userId);
    return jedisCluster.scard(key);
  }

  /**
   * Check if a user is a member of a group or not
   * @param userId
   * @param groupId
   * @return
   */
  public boolean isMember(String userId, String groupId) {
    String key = SetGroupMembers.getKey(groupId);
    boolean isMember = jedisCluster.sismember(key, userId);
    return isMember;
  }

  /**
   * Get all groups for an app
   * @param appName
   * @return
   */
  public Map<String, String> getAllGroupsByApp(String appName) {
    String groupIdByNameKey = GroupIdByName.getKey(appName);
    Map<String, String> groupNameToIdMap = jedisCluster.hgetAll(groupIdByNameKey);
    return groupNameToIdMap;
  }

  /**
   * List paginated groups by app
   * @param appName
   * @param cursor
   * @return
   */
  public ResultWithCursor<Map.Entry<String, String>> listPaginatedGroupsByApp(String appName, String cursor) {
    String groupIdByNameKey = GroupIdByName.getKey(appName);
    ScanResult<Map.Entry<String, String>> scanResult = jedisCluster.hscan(groupIdByNameKey, cursor);
    ResultWithCursor<Map.Entry<String, String>> resultWithCursor = new ResultWithCursor<>();
    resultWithCursor.results = scanResult.getResult();
    resultWithCursor.cursor = scanResult.getStringCursor();
    return resultWithCursor;
  }

}
