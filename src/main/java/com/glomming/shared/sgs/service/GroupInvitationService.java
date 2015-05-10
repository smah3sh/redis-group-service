package com.glomming.shared.sgs.service;

import com.glomming.shared.sgs.bean.GroupAttribute;
import com.glomming.shared.sgs.bean.GroupMemberAttribute;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handles inviting members to groups
 */
@Component
public class GroupInvitationService extends BaseGroupService {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GroupInvitationService.class);

  public String setGroupAttributes(GroupAttribute groupAttribute) {
    String result = jedisCluster.hmset(groupAttribute.groupId, groupAttribute.attributes);
    return result;
  }

  public GroupAttribute getGroupAttributes(String groupId) {
    Map<String, String> attributes = jedisCluster.hgetAll(groupId);
    GroupAttribute groupAttribute = new GroupAttribute(groupId, attributes);
    return groupAttribute;
  }

  public Long incrementGroupAttribute(String groupId, String attribute, long value) {
    return jedisCluster.hincrBy(groupId, attribute, value);
  }


  public String setGroupMemberAttributes(GroupMemberAttribute attributes) {
    String result = jedisCluster.hmset(attributes.groupId, attributes.attributes);
    return result;
  }

  public GroupMemberAttribute getAllGroupMemberAttributes(String userId, String groupId) {
    Map<String, String> attributes = jedisCluster.hgetAll(groupId);
    GroupMemberAttribute result = new GroupMemberAttribute(groupId, userId, attributes);
    return result;
  }
}
