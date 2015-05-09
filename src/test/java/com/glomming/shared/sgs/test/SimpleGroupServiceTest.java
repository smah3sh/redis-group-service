package com.glomming.shared.sgs.test;

import com.glomming.shared.sgs.bean.Group;
import com.glomming.shared.sgs.bean.GroupAttribute;
import com.glomming.shared.sgs.bean.GroupJoinState;
import com.glomming.shared.sgs.controller.RedisGroupServiceController;
import com.glomming.shared.sgs.exception.*;
import com.glomming.shared.sgs.service.SimpleGroupAttributeService;
import com.glomming.shared.sgs.service.SimpleGroupService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanResult;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {SimpleGroupServiceTestConfiguration.class})
@WebAppConfiguration
@IntegrationTest
public class SimpleGroupServiceTest {

  private static final Logger logger = LoggerFactory.getLogger(RedisGroupServiceController.class);

  @Autowired
  private SimpleGroupService simpleGroupService;

  @Autowired
  private SimpleGroupAttributeService simpleGroupAttributeService;


  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreateGroup() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    String groupName = UUID.randomUUID().toString();
    String ownerId = UUID.randomUUID().toString();
    long maxSize = 10;
    String groupId = simpleGroupService.createGroup(appNameOne, groupName, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));
    // Get group by groupId
    Group groupFromRedis = simpleGroupService.findGroup(groupId);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupName, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Read group by appName and groupName
    groupFromRedis = simpleGroupService.findGroup(appNameOne, groupName);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupName, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);
  }

  @Test
  public void testCreateDuplicateGroup() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    String groupName = UUID.randomUUID().toString();
    String ownerId = UUID.randomUUID().toString();
    long maxSize = 10;
    String groupId = simpleGroupService.createGroup(appNameOne, groupName, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));
    // Get group by groupId
    Group groupFromRedis = simpleGroupService.findGroup(groupId);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupName, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Create duplicate group
    String duplicateGroupId = simpleGroupService.createGroup(appNameOne, groupName, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertNull(duplicateGroupId);
  }

  @Test
  public void testCreateGroupWithSameNameButDifferentApp() throws Exception {

    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    String groupName = UUID.randomUUID().toString();
    String ownerId = UUID.randomUUID().toString();
    long maxSize = 10;
    String groupId = simpleGroupService.createGroup(appNameOne, groupName, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));
    // Get group by groupId
    Group groupFromRedis = simpleGroupService.findGroup(groupId);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupName, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Create a group with the same name under a different app
    String groupId2 = simpleGroupService.createGroup(appNameTwo, groupName, maxSize, ownerId, GroupJoinState.CLOSED);
    Assert.assertFalse(StringUtils.isEmpty(groupId2));
    // Get group by groupId
    Group groupFromRedis2 = simpleGroupService.findGroup(groupId2);
    Assert.assertNotNull(groupFromRedis2);
    Assert.assertEquals(appNameTwo, groupFromRedis2.appName);
    Assert.assertEquals(groupName, groupFromRedis2.name);
    Assert.assertEquals(1, groupFromRedis2.currentSize);
    Assert.assertEquals(maxSize, groupFromRedis2.maxSize);
    Assert.assertEquals(ownerId, groupFromRedis2.ownerId);
    Assert.assertEquals(GroupJoinState.CLOSED, groupFromRedis2.groupJoinState);
  }

  @Test
  public void testGroupMembership() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    String groupName = UUID.randomUUID().toString();
    String ownerId = UUID.randomUUID().toString();
    long maxSize = 3;
    String groupId = simpleGroupService.createGroup(appNameOne, groupName, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));
    // Get group by groupId
    Group groupFromRedis = simpleGroupService.findGroup(groupId);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupName, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Check group membership
    Set<String> members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 1);
    Assert.assertTrue(members.contains(ownerId));

    // Now add 2nd member to group
    String secondUser = UUID.randomUUID().toString();
    simpleGroupService.addGroupMember(groupId, secondUser);
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 2);
    Assert.assertTrue(members.contains(ownerId));
    Assert.assertTrue(members.contains(secondUser));

    // add 3rd member
    String thirdUser = UUID.randomUUID().toString();
    simpleGroupService.addGroupMember(groupId, thirdUser);
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 3);
    Assert.assertTrue(members.contains(ownerId));
    Assert.assertTrue(members.contains(secondUser));
    Assert.assertTrue(members.contains(thirdUser));

    // add 4th member, should throw exception
    String fourthUser = UUID.randomUUID().toString();
    try {
      simpleGroupService.addGroupMember(groupId, fourthUser);
    } catch (GroupMembershipExceededException gmee) {
      // ignore
    }
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 3);
    Assert.assertTrue(members.contains(ownerId));
    Assert.assertTrue(members.contains(secondUser));
    Assert.assertTrue(members.contains(thirdUser));

    // Now remove 3rd member
    simpleGroupService.removeGroupMember(groupId, thirdUser);
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 2);
    Assert.assertTrue(members.contains(ownerId));
    Assert.assertTrue(members.contains(secondUser));

    // Now remove 2rd member
    simpleGroupService.removeGroupMember(groupId, secondUser);
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 1);
    Assert.assertTrue(members.contains(ownerId));
  }


  @Test
  public void testGetPaginatedGroupMembers() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    String groupName = UUID.randomUUID().toString();
    String ownerId = UUID.randomUUID().toString();
    long groupSize = 45;
    String groupId = simpleGroupService.createGroup(appNameOne, groupName, groupSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));
    // Get group by groupId
    Group groupFromRedis = simpleGroupService.findGroup(groupId);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupName, groupFromRedis.name);
    Assert.assertEquals(groupSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Check group membership
    Set<String> members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 1);
    Assert.assertTrue(members.contains(ownerId));

    // Add more members
    for (int i = 1; i < groupSize; i++) {
      // Now members to group
      String user = "user_" + i;
      simpleGroupService.addGroupMember(groupId, user);
      members = simpleGroupService.getMembersByGroup(groupId);
      Assert.assertEquals(i + 1, members.size());
      Assert.assertTrue(members.contains(ownerId));
      Assert.assertTrue(members.contains(user));
      members.clear();
    }

    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertEquals(groupSize, members.size());
    // Read back the users
    String cursor = "0";
    ScanResult<String> results = null;
    Set<String> groupMembersFromDB = new HashSet<>();
    long count = 0;
    do {
      results = simpleGroupService.getPaginatedMembers(groupId, cursor);
      List<String> groupMembers = results.getResult();
      for (String groupMember : groupMembers) {
        Assert.assertTrue(members.contains(groupMember));
      }
      cursor = results.getStringCursor();
      count += groupMembers.size();
      groupMembersFromDB.addAll(groupMembers);
    } while (!cursor.equals("0"));
    // Verify that all the members were read
    Assert.assertEquals(groupSize, count);
    Assert.assertEquals(members, groupMembersFromDB);
  }

  // Test where a user joins multiple groups
  @Test
  public void testUserGroupMemberships() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    long maxSize = 10;

    // Create group one
    String groupNameOne = UUID.randomUUID().toString();
    String ownerIdOne = UUID.randomUUID().toString();
    String groupIdOne = simpleGroupService.createGroup(appNameOne, groupNameOne, maxSize, ownerIdOne, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupIdOne));
    // Get group by groupId
    Group groupOneFromRedis = simpleGroupService.findGroup(groupIdOne);
    Assert.assertNotNull(groupOneFromRedis);
    Assert.assertEquals(appNameOne, groupOneFromRedis.appName);
    Assert.assertEquals(groupNameOne, groupOneFromRedis.name);
    Assert.assertEquals(maxSize, groupOneFromRedis.maxSize);
    Assert.assertEquals(1, groupOneFromRedis.currentSize);
    Assert.assertEquals(ownerIdOne, groupOneFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupOneFromRedis.groupJoinState);

    // Create group two
    String groupNameTwo = UUID.randomUUID().toString();
    String ownerIdTwo = UUID.randomUUID().toString();
    String groupIdTwo = simpleGroupService.createGroup(appNameOne, groupNameTwo, maxSize, ownerIdTwo, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupIdTwo));
    // Get group by groupId
    Group groupTwoFromRedis = simpleGroupService.findGroup(groupIdTwo);
    Assert.assertNotNull(groupTwoFromRedis);
    Assert.assertEquals(appNameOne, groupTwoFromRedis.appName);
    Assert.assertEquals(groupNameTwo, groupTwoFromRedis.name);
    Assert.assertEquals(maxSize, groupTwoFromRedis.maxSize);
    Assert.assertEquals(1, groupTwoFromRedis.currentSize);
    Assert.assertEquals(ownerIdTwo, groupTwoFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupTwoFromRedis.groupJoinState);

    // Create group three
    String groupNameThree = UUID.randomUUID().toString();
    String ownerIdThree = UUID.randomUUID().toString();
    String groupIdThree = simpleGroupService.createGroup(appNameOne, groupNameThree, maxSize, ownerIdThree, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupIdThree));
    // Get group by groupId
    Group groupThreeFromRedis = simpleGroupService.findGroup(groupIdThree);
    Assert.assertNotNull(groupThreeFromRedis);
    Assert.assertEquals(appNameOne, groupThreeFromRedis.appName);
    Assert.assertEquals(groupNameThree, groupThreeFromRedis.name);
    Assert.assertEquals(maxSize, groupThreeFromRedis.maxSize);
    Assert.assertEquals(1, groupThreeFromRedis.currentSize);
    Assert.assertEquals(ownerIdThree, groupThreeFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupThreeFromRedis.groupJoinState);

    // a user joins all three groups
    String userId = UUID.randomUUID().toString();
    simpleGroupService.addGroupMember(groupIdOne, userId);
    simpleGroupService.addGroupMember(groupIdTwo, userId);
    simpleGroupService.addGroupMember(groupIdThree, userId);

    Set<String> members = null;

    // Check group memberships for groupOne
    members = simpleGroupService.getMembersByGroup(groupIdOne);
    Assert.assertTrue(members.size() == 2);
    Assert.assertTrue(members.contains(ownerIdOne));
    Assert.assertTrue(members.contains(userId));

    // Check group memberships for groupTwo
    members = simpleGroupService.getMembersByGroup(groupIdTwo);
    Assert.assertTrue(members.size() == 2);
    Assert.assertTrue(members.contains(ownerIdTwo));
    Assert.assertTrue(members.contains(userId));

    // Check group memberships for groupThree
    members = simpleGroupService.getMembersByGroup(groupIdThree);
    Assert.assertTrue(members.size() == 2);
    Assert.assertTrue(members.contains(ownerIdThree));
    Assert.assertTrue(members.contains(userId));

    // Verify this user is member of 3 groups
    Assert.assertEquals(3, simpleGroupService.getCountGroupMembershipsByUser(userId));

    // Check the groups this user is member of
    Set<String> groups = simpleGroupService.getGroupMembershipsByUser(userId);
    Assert.assertTrue(groups.size() == 3);
    Assert.assertTrue(groups.contains(groupIdOne));
    Assert.assertTrue(groups.contains(groupIdTwo));
    Assert.assertTrue(groups.contains(groupIdThree));

    Assert.assertTrue(simpleGroupService.isMember(userId, groupIdOne));
    Assert.assertTrue(simpleGroupService.isMember(userId, groupIdTwo));
    Assert.assertTrue(simpleGroupService.isMember(userId, groupIdThree));
  }

  @Test
  public void testUpdateOwner() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    String groupName = UUID.randomUUID().toString();
    String ownerId = UUID.randomUUID().toString();
    long maxSize = 10;
    String groupId = simpleGroupService.createGroup(appNameOne, groupName, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));
    // Get group by groupId
    Group groupFromRedis = simpleGroupService.findGroup(groupId);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupName, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Add 2nd user
    String userId = UUID.randomUUID().toString();
    simpleGroupService.addGroupMember(groupId, userId);
    Set<String> members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 2);
    Assert.assertTrue(members.contains(ownerId));
    Assert.assertTrue(members.contains(userId));

    // Change owner
    simpleGroupService.updateOwner(groupId, userId);
    Group group = simpleGroupService.findGroup(groupId);
    Assert.assertEquals(group.ownerId, userId);
  }

  @Test
  public void testUpdateGroupName() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    String groupNameOne = UUID.randomUUID().toString();
    String ownerId = UUID.randomUUID().toString();
    long maxSize = 10;
    String groupId = simpleGroupService.createGroup(appNameOne, groupNameOne, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));
    // Get group by groupId
    Group groupFromRedis = simpleGroupService.findGroup(groupId);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupNameOne, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Change name and verify
    String newName = UUID.randomUUID().toString();
    simpleGroupService.updateGroupName(groupId, newName);
    Group group = simpleGroupService.findGroup(groupId);
    Assert.assertEquals(newName, group.name);

    String groupNameTwo = UUID.randomUUID().toString();
    // Create 2nd group
    String groupIdTwo = simpleGroupService.createGroup(appNameOne, groupNameTwo, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupIdTwo));
    // Get group by groupId
    groupFromRedis = simpleGroupService.findGroup(groupIdTwo);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupNameTwo, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Change name of 2nd group to first group and verify failure
    try {
      simpleGroupService.updateGroupName(groupIdTwo, groupNameOne);
    } catch (InvalidGroupNameException igne) {
      // Ignore
    }
  }

  @Test
  public void testUpdateMaxSize() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    String groupName = UUID.randomUUID().toString();
    String ownerId = UUID.randomUUID().toString();
    long maxSize = 10;
    String groupId = simpleGroupService.createGroup(appNameOne, groupName, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));
    // Get group by groupId
    Group groupFromRedis = simpleGroupService.findGroup(groupId);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupName, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Check group membership
    Set<String> members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 1);
    Assert.assertTrue(members.contains(ownerId));

    // Now add 2nd member to group
    String secondUser = UUID.randomUUID().toString();
    simpleGroupService.addGroupMember(groupId, secondUser);
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 2);
    Assert.assertTrue(members.contains(ownerId));
    Assert.assertTrue(members.contains(secondUser));

    // add 3rd member
    String thirdUser = UUID.randomUUID().toString();
    simpleGroupService.addGroupMember(groupId, thirdUser);
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 3);
    Assert.assertTrue(members.contains(ownerId));
    Assert.assertTrue(members.contains(secondUser));
    Assert.assertTrue(members.contains(thirdUser));

    // Change max size to 20
    long newSize = 20;
    simpleGroupService.updateMaxSize(groupId, newSize);
    Group group = simpleGroupService.findGroup(groupId);
    Assert.assertEquals(newSize, group.maxSize);

    // Set maxSize t0 2, should fail
    newSize = 2;
    try {
      simpleGroupService.updateMaxSize(groupId, newSize);
    } catch (InvalidParameterException ipe) {
      // Ignore
    }

  }

  @Test
  public void testOwnerLeaveGroup() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    String groupName = UUID.randomUUID().toString();
    String ownerId = UUID.randomUUID().toString();
    long maxSize = 3;
    String groupId = simpleGroupService.createGroup(appNameOne, groupName, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));
    // Get group by groupId
    Group groupFromRedis = simpleGroupService.findGroup(groupId);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupName, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Check group membership
    Set<String> members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 1);
    Assert.assertTrue(members.contains(ownerId));

    // Now add 2nd member to group
    String secondUser = UUID.randomUUID().toString();
    simpleGroupService.addGroupMember(groupId, secondUser);
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 2);
    Assert.assertTrue(members.contains(ownerId));
    Assert.assertTrue(members.contains(secondUser));

    // Remove owner from group
    try {
      simpleGroupService.removeGroupMember(groupId, ownerId);
    } catch (OwnerLeaveGroupException olge) {
      // Ignore
    }

    // Change owner and retry
    simpleGroupService.updateOwner(groupId, secondUser);
    // Change owner
    simpleGroupService.removeGroupMember(groupId, ownerId);
    // Read back and verify
    Group group = simpleGroupService.findGroup(groupId);
    Assert.assertEquals(group.ownerId, secondUser);
    Assert.assertEquals(group.currentSize, 1);
  }

  @Test
  public void testDeleteGroup() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    String groupName = UUID.randomUUID().toString();
    String ownerId = UUID.randomUUID().toString();
    long maxSize = 3;
    String groupId = simpleGroupService.createGroup(appNameOne, groupName, maxSize, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));
    // Get group by groupId
    Group groupFromRedis = simpleGroupService.findGroup(groupId);
    Assert.assertNotNull(groupFromRedis);
    Assert.assertEquals(appNameOne, groupFromRedis.appName);
    Assert.assertEquals(groupName, groupFromRedis.name);
    Assert.assertEquals(maxSize, groupFromRedis.maxSize);
    Assert.assertEquals(1, groupFromRedis.currentSize);
    Assert.assertEquals(ownerId, groupFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);

    // Check group membership
    Set<String> members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 1);
    Assert.assertTrue(members.contains(ownerId));

    // Now add 2nd member to group
    String secondUser = UUID.randomUUID().toString();
    simpleGroupService.addGroupMember(groupId, secondUser);
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 2);
    Assert.assertTrue(members.contains(ownerId));
    Assert.assertTrue(members.contains(secondUser));

    // add 3rd member
    String thirdUser = UUID.randomUUID().toString();
    simpleGroupService.addGroupMember(groupId, thirdUser);
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertTrue(members.size() == 3);
    Assert.assertTrue(members.contains(ownerId));
    Assert.assertTrue(members.contains(secondUser));
    Assert.assertTrue(members.contains(thirdUser));

    // Now delete group
    simpleGroupService.deleteGroup(groupId);
    Group group = null;
    // Read back
    try {
      group = simpleGroupService.findGroup(groupId);
    } catch (InvalidGroupException ige) {
      // Ignore
    }
    // Try finding by appName and groupName
    try {
      group = simpleGroupService.findGroup(appNameOne, groupName);
    } catch (InvalidGroupException ige) {
      // Ignore
    }
    // Get list of groups for all members
    Assert.assertEquals(simpleGroupService.getGroupMembershipsByUser(ownerId).size(), 0);
    Assert.assertEquals(simpleGroupService.getGroupMembershipsByUser(secondUser).size(), 0);
    Assert.assertEquals(simpleGroupService.getGroupMembershipsByUser(thirdUser).size(), 0);

    // Get set of group members
    members = simpleGroupService.getMembersByGroup(groupId);
    Assert.assertEquals(members.size(), 0);


  }

  @Test
  public void testhmset() {
    String key = "key";
    String field = "field";
    String value = "1";
    simpleGroupService.getRedisCluster().hset(key, field, value);
    String valueFromRedis = simpleGroupService.getRedisCluster().hget(key, field);
    Assert.assertEquals(value, valueFromRedis);
    simpleGroupService.getRedisCluster().hincrBy(key, field, 5);
    valueFromRedis = simpleGroupService.getRedisCluster().hget(key, field);
    Assert.assertEquals(6, Long.valueOf(valueFromRedis).longValue());
  }

  @Test
  public void testGetGroupsByAppName() throws Exception {
    long maxSize = 10;

    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();
    // Create group one
    String groupNameOne = UUID.randomUUID().toString();
    String ownerIdOne = UUID.randomUUID().toString();
    String groupIdOne = simpleGroupService.createGroup(appNameTwo, groupNameOne, maxSize, ownerIdOne, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupIdOne));
    // Get group by groupId
    Group groupOneFromRedis = simpleGroupService.findGroup(groupIdOne);
    Assert.assertNotNull(groupOneFromRedis);
    Assert.assertEquals(appNameTwo, groupOneFromRedis.appName);
    Assert.assertEquals(groupNameOne, groupOneFromRedis.name);
    Assert.assertEquals(maxSize, groupOneFromRedis.maxSize);
    Assert.assertEquals(1, groupOneFromRedis.currentSize);
    Assert.assertEquals(ownerIdOne, groupOneFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupOneFromRedis.groupJoinState);

    // Create group two
    String groupNameTwo = UUID.randomUUID().toString();
    String ownerIdTwo = UUID.randomUUID().toString();
    String groupIdTwo = simpleGroupService.createGroup(appNameTwo, groupNameTwo, maxSize, ownerIdTwo, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupIdTwo));
    // Get group by groupId
    Group groupTwoFromRedis = simpleGroupService.findGroup(groupIdTwo);
    Assert.assertNotNull(groupTwoFromRedis);
    Assert.assertEquals(appNameTwo, groupTwoFromRedis.appName);
    Assert.assertEquals(groupNameTwo, groupTwoFromRedis.name);
    Assert.assertEquals(maxSize, groupTwoFromRedis.maxSize);
    Assert.assertEquals(1, groupTwoFromRedis.currentSize);
    Assert.assertEquals(ownerIdTwo, groupTwoFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupTwoFromRedis.groupJoinState);

    // Create group three
    String groupNameThree = UUID.randomUUID().toString();
    String ownerIdThree = UUID.randomUUID().toString();
    String groupIdThree = simpleGroupService.createGroup(appNameTwo, groupNameThree, maxSize, ownerIdThree, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupIdThree));
    // Get group by groupId
    Group groupThreeFromRedis = simpleGroupService.findGroup(groupIdThree);
    Assert.assertNotNull(groupThreeFromRedis);
    Assert.assertEquals(appNameTwo, groupThreeFromRedis.appName);
    Assert.assertEquals(groupNameThree, groupThreeFromRedis.name);
    Assert.assertEquals(maxSize, groupThreeFromRedis.maxSize);
    Assert.assertEquals(1, groupThreeFromRedis.currentSize);
    Assert.assertEquals(ownerIdThree, groupThreeFromRedis.ownerId);
    Assert.assertEquals(GroupJoinState.OPEN, groupThreeFromRedis.groupJoinState);

    Set<String> setGroupNames = new HashSet<>();
    setGroupNames.add(groupNameOne);
    setGroupNames.add(groupNameTwo);
    setGroupNames.add(groupNameThree);

    Set<String> setGroupIds = new HashSet<>();
    setGroupIds.add(groupIdOne);
    setGroupIds.add(groupIdTwo);
    setGroupIds.add(groupIdThree);

    // Get all groups under appOne
    Map<String, String> groupMap = simpleGroupService.getAllGroupsByApp(appNameTwo);
    for (Map.Entry<String, String> groupMapEntry : groupMap.entrySet()) {
      Assert.assertTrue(setGroupNames.contains(groupMapEntry.getKey()));
      Assert.assertTrue(setGroupIds.contains(groupMapEntry.getValue()));
    }

    groupMap = simpleGroupService.getAllGroupsByApp(appNameThree);
    Assert.assertTrue(groupMap.isEmpty());
  }

  @Test
  public void testListPaginatedGroupsByApp() throws Exception {
    String appNameOne = UUID.randomUUID().toString();
    String appNameTwo = UUID.randomUUID().toString();
    String appNameThree = UUID.randomUUID().toString();

    long numGroups = 137;

    String ownerId = UUID.randomUUID().toString();

    Set<String> groupNames = new HashSet<>();
    for (int i = 0; i < numGroups; i++) {
      // Create groups
      String groupName = "name_" + i + "_" + UUID.randomUUID().toString();
      String groupId = simpleGroupService.createGroup(appNameOne, groupName, numGroups, ownerId, GroupJoinState.OPEN);
      groupNames.add(groupName);
      Assert.assertFalse(StringUtils.isEmpty(groupId));
      Group groupFromRedis = simpleGroupService.findGroup(groupId);
      Assert.assertNotNull(groupFromRedis);
      Assert.assertEquals(appNameOne, groupFromRedis.appName);
      Assert.assertEquals(groupName, groupFromRedis.name);
      Assert.assertEquals(numGroups, groupFromRedis.maxSize);
      Assert.assertEquals(1, groupFromRedis.currentSize);
      Assert.assertEquals(ownerId, groupFromRedis.ownerId);
      Assert.assertEquals(GroupJoinState.OPEN, groupFromRedis.groupJoinState);
    }
    // Create a group under a second app
    String groupName = UUID.randomUUID().toString();
    String groupId = simpleGroupService.createGroup(appNameTwo, groupName, numGroups, ownerId, GroupJoinState.OPEN);
    Assert.assertFalse(StringUtils.isEmpty(groupId));



    ScanResult<Map.Entry<String, String>> results = null;
    String cursor = "0";
    Set<String> groupMembersFromDB = new HashSet<>();
    long count = 0;
    do {
      results = simpleGroupService.listPaginatedGroupsByApp(appNameOne, cursor);
      List<Map.Entry<String, String>> groups = results.getResult();
      for (Map.Entry<String, String> group : groups) {
        Assert.assertTrue(groupNames.contains(group.getKey()));
        groupMembersFromDB.add(group.getKey());
      }
      cursor = results.getStringCursor();
      count += groups.size();
    } while (!cursor.equals("0"));
    // Verify that all the members were read
    Assert.assertEquals(numGroups, count);
    Assert.assertEquals(groupNames, groupMembersFromDB);
  }


  // Test group attributes
  @Test
  public void testSetGetGroupAttributes() {
    Map<String, String> attributes = new HashMap<>();
    for (int i = 0; i < 5; i++) {
      attributes.put("attribute_" + i, UUID.randomUUID().toString());
    }
    String groupId = UUID.randomUUID().toString();
    GroupAttribute groupAttribute = new GroupAttribute(groupId, attributes);
    String result = simpleGroupAttributeService.setGroupAttributes(groupAttribute);
    Assert.assertTrue(result.equals(SimpleGroupService.RedisOk));

    // Now read it back and verify if equal
    GroupAttribute groupAttributeFromRedis = simpleGroupAttributeService.getGroupAttributes(groupId);
    Assert.assertEquals(groupAttribute, groupAttributeFromRedis);

    // Now set an extra field
    Map<String, String> newAttributes = new HashMap<>();
    String attributeName = "attribute_5";
    String attributeValue = "5";
    newAttributes.put(attributeName, attributeValue);
    groupAttribute = new GroupAttribute(groupId, newAttributes);
    result = simpleGroupAttributeService.setGroupAttributes(groupAttribute);
    Assert.assertTrue(result.equals(SimpleGroupService.RedisOk));

    // Now read the results
    groupAttributeFromRedis = simpleGroupAttributeService.getGroupAttributes(groupId);
    Assert.assertEquals(6, groupAttributeFromRedis.attributes.size());
    Assert.assertEquals(attributeValue, groupAttributeFromRedis.attributes.get(attributeName));
    // Verify the first five are the same attributes that were set initially
    for (Map.Entry<String, String> entry : groupAttribute.attributes.entrySet()) {
      Assert.assertEquals(entry.getValue(), groupAttributeFromRedis.attributes.get(entry.getKey()));
    }
  }

  Random generator = new Random();

  @Test
  public void testIncrementGroupAttributes() {
    Map<String, String> attributes = new HashMap<>();
    String attributeOne = "attribute_1", attributeTwo = "attribute_2";
    String valueOne = Integer.toString(generator.nextInt(1000));
    String valueTwo = Integer.toString(generator.nextInt(1000));

    attributes.put(attributeOne, valueOne);
    attributes.put(attributeTwo, valueTwo);

    String groupId = UUID.randomUUID().toString();
    GroupAttribute groupAttribute = new GroupAttribute(groupId, attributes);
    String result = simpleGroupAttributeService.setGroupAttributes(groupAttribute);
    Assert.assertTrue(result.equals(SimpleGroupService.RedisOk));

    // Now read it back and verify if equal
    GroupAttribute groupAttributeFromRedis = simpleGroupAttributeService.getGroupAttributes(groupId);
    Assert.assertEquals(groupAttribute, groupAttributeFromRedis);

    // Increment the attribute
    Long resultIncrement = simpleGroupAttributeService.incrementGroupAttribute(groupId, attributeOne, 1L);
    Assert.assertEquals(resultIncrement.longValue(), Long.parseLong(valueOne) + 1);

    resultIncrement = simpleGroupAttributeService.incrementGroupAttribute(groupId, attributeTwo, -1L);
    Assert.assertEquals(resultIncrement.longValue(), Long.parseLong(valueTwo) - 1);

    groupAttributeFromRedis = simpleGroupAttributeService.getGroupAttributes(groupId);
    Assert.assertEquals(Long.parseLong(valueOne) + 1, Long.parseLong(groupAttributeFromRedis.attributes.get(attributeOne)));
    Assert.assertEquals(Long.parseLong(valueTwo) - 1, Long.parseLong(groupAttributeFromRedis.attributes.get(attributeTwo)));
  }


  /**
   * Delete all the keys from all the nodes, run as the last test
   */
  @Test
  public void zdeleteAll() {

    Map<String, JedisPool> clusterNodes = simpleGroupService.getRedisCluster().getClusterNodes();
    try {
      for (Map.Entry<String, JedisPool> node : clusterNodes.entrySet()) {
        node.getValue().getResource().flushAll();
      }
    } catch (Exception ignore) {
    }
  }


}
