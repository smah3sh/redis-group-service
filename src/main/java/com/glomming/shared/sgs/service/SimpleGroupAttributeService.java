package com.glomming.shared.sgs.service;

import com.glomming.shared.sgs.bean.GroupAttribute;
import com.glomming.shared.sgs.bean.GroupMemberAttribute;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SimpleGroupAttributeService {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SimpleGroupAttributeService.class);

  @Autowired
  @Qualifier(value = "redis_cluster")
  private JedisCluster redisCluster;


  @Autowired
  private SimpleGroupService simpleGroupService;

  public static final String RedisOk = "OK";

  public JedisCluster getRedisCluster() {
    return this.redisCluster;
  }

  public String setGroupAttributes(GroupAttribute groupAttribute) {
    String result = redisCluster.hmset(groupAttribute.groupId, groupAttribute.attributes);
    return result;
  }

  public GroupAttribute getGroupAttributes(String groupId) {
    Map<String, String> attributes = redisCluster.hgetAll(groupId);
    GroupAttribute groupAttribute = new GroupAttribute(groupId, attributes);
    return groupAttribute;
  }

  public Long incrementGroupAttribute(String groupId, String attribute, long value) {
    return redisCluster.hincrBy(groupId, attribute, value);
  }


  public String setGroupMemberAttributes(GroupMemberAttribute attributes) {
    String result = redisCluster.hmset(attributes.groupId, attributes.attributes);
    return result;
  }

  public GroupMemberAttribute getAllGroupMemberAttributes(String userId, String groupId) {
    Map<String, String> attributes = redisCluster.hgetAll(groupId);
    GroupMemberAttribute result = new GroupMemberAttribute(userId, groupId, attributes);
    return result;
  }

  public List<GroupMemberAttribute> listAttributesForGroupMembers(String groupId) {

    List<GroupMemberAttribute> results = new ArrayList<>();
    // Get list of members
    Set<String> members = simpleGroupService.getMembersByGroup(groupId);

    for (String member : members) {
      GroupMemberAttribute groupMemberAttribute = this.getAllGroupMemberAttributes(member, groupId);
      results.add(groupMemberAttribute);
    }
    return results;
  }


  public Long incrementGroupMemberAttribute(String userId, String groupId, String attribute, long value) {
    return redisCluster.hincrBy(groupId, attribute, value);
  }



  public long inc(String key) {
    long result = -1L;
    try {
      result = redisCluster.incr(key);
    } catch (Exception e) {
      logger.error("", e);
    }
    return result;
  }

  public String get(String key) {
    String result = null;
    try {
      result = redisCluster.get(key);
    } catch (Exception e) {
      logger.error("", e);
    }
    return result;
  }

}
