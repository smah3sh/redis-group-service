package com.glomming.shared.sgs.service;

import com.glomming.shared.sgs.bean.GroupAttribute;
import com.glomming.shared.sgs.bean.GroupMemberAttribute;
import com.glomming.shared.sgs.bean.ResultWithCursor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SimpleGroupAttributeService extends BaseGroupService {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SimpleGroupAttributeService.class);

  @Autowired
  private SimpleGroupService simpleGroupService;

  /**
   * Set one or more group attributes
   * @param groupAttribute
   * @return
   */
  public String setGroupAttributes(GroupAttribute groupAttribute) {
    String result = jedisCluster.hmset(groupAttribute.groupId, groupAttribute.attributes);
    return result;
  }

  /**
   * Get all group attributes
   * @param groupId
   * @return
   */
  public GroupAttribute getGroupAttributes(String groupId) {
    Map<String, String> attributes = jedisCluster.hgetAll(groupId);
    GroupAttribute groupAttribute = new GroupAttribute(groupId, attributes);
    return groupAttribute;
  }

  /**
   * Increment a numeric attribute
   * Even if a value has been initially set as a string, it can be incremented as it is internally stored as a number in Redis
   * @param groupId
   * @param attribute
   * @param value
   * @return
   */
  public Long incrementGroupAttribute(String groupId, String attribute, long value) {
    return jedisCluster.hincrBy(groupId, attribute, value);
  }


  /**
   * Set group member attributes
   * @param memberAttribute
   * @return
   */
  public String setGroupMemberAttributes(GroupMemberAttribute memberAttribute) {
    String key = GroupMemberAttribute.getKey(memberAttribute.groupId, memberAttribute.userId);
    String result = jedisCluster.hmset(key, memberAttribute.attributes);
    return result;
  }

  /**
   * Get all attributes for a member
   * @param groupId
   * @param userId
   * @return
   */
  public GroupMemberAttribute getAllGroupMemberAttributes(String groupId, String userId) {
    String key = GroupMemberAttribute.getKey(groupId, userId);
    Map<String, String> attributes = jedisCluster.hgetAll(key);
    GroupMemberAttribute result = new GroupMemberAttribute(groupId, userId, attributes);
    return result;
  }

  /**
   * Paginated list attributes for group members.
   * @param groupId
   * @param cursor
   * @return
   */
  public ResultWithCursor<GroupMemberAttribute> listAttributesForGroupMembers(String groupId, String cursor) {
    // Get list of members
    ResultWithCursor<String> members = simpleGroupService.getPaginatedMembers(groupId, cursor);
    List<GroupMemberAttribute> groupMemberAttributeList = new ArrayList<>();
    for (String member : members.results) {
      GroupMemberAttribute groupMemberAttribute = this.getAllGroupMemberAttributes(groupId, member);
      if (!groupMemberAttribute.attributes.isEmpty())
        groupMemberAttributeList.add(groupMemberAttribute);
    }
    ResultWithCursor<GroupMemberAttribute> results = new ResultWithCursor();
    results.results = groupMemberAttributeList;
    results.cursor = members.cursor;
    return results;
  }

  /**
   * Increment an individual numeric  group member attribute
   * @param groupId
   * @param userId
   * @param attribute
   * @param value
   * @return - Value after increment operation
   */
  public Long incrementGroupMemberAttribute(String groupId, String userId, String attribute, long value) {
    String key = GroupMemberAttribute.getKey(groupId, userId);
    return jedisCluster.hincrBy(key, attribute, value);
  }

}
