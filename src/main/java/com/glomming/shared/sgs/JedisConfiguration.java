package com.glomming.shared.sgs;

import com.amazonaws.services.cloudsearchv2.AmazonCloudSearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

@Configuration
@ComponentScan(basePackageClasses = {JedisConfiguration.class, AmazonCloudSearchClient.class})
public class JedisConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(JedisConfiguration.class);

  @Bean(name = {"redis_cluster"})
  public JedisCluster getRedisCluster(@Value("${redis.cluster.master.ips}") String redisClusterIps) {

    String arrayIpAndPort[] = redisClusterIps.split(",");
    Set<HostAndPort> nodes = new HashSet<>();

    for (String ipAndPort : arrayIpAndPort) {
      final String port_ip[] = ipAndPort.split(":");
      final String ip = port_ip[0];
      final String port = port_ip[1];
      nodes.add(new HostAndPort(ip, Integer.parseInt(port)));
    }

    JedisCluster jc = new JedisCluster(nodes);
    return jc;
  }
}
