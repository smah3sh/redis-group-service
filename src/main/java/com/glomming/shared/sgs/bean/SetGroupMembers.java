package com.glomming.shared.sgs.bean;

import java.util.Set;

/**
 * Created by smahesh on 5/2/15.
 */
public class SetGroupMembers {
  public String groupId;    // Key
  Set<String> members;      // Member set

  public static final String getKey(String groupId) {
    StringBuilder sb = new StringBuilder();
    sb.append(groupId);
    sb.append(":");
    sb.append(SetGroupMembers.class.getSimpleName());
    return sb.toString();
  }
}
