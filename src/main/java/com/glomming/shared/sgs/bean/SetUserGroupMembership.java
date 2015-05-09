package com.glomming.shared.sgs.bean;

import java.util.Set;

/**
 * Created by smahesh on 5/2/15.
 */
public class SetUserGroupMembership {
  String userId;
  Set<String> groups;   // The set of groups a user belongs to

  public static String getKey(String userId) {
    StringBuilder sb = new StringBuilder();
    sb.append(userId);
    sb.append(":");
    sb.append(SetUserGroupMembership.class.getSimpleName());
    return sb.toString();
  }
}
