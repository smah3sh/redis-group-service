package com.glomming.shared.sgs.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCluster;

@Component
public class BaseGroupService {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BaseGroupService.class);

  @Autowired
  @Qualifier(value = "redis_cluster")
  protected JedisCluster jedisCluster;

  public static final String RedisOk = "OK";

  public JedisCluster getJedisCluster() {
    return this.jedisCluster;
  }

}
