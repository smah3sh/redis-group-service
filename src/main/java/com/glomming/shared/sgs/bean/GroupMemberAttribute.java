package com.glomming.shared.sgs.bean;

import java.util.Map;

/**
 * Created by smahesh on 5/2/15.
 */
public class GroupMemberAttribute {
  public String userId;         // UserId
  public String groupId;
  public Map<String, String> attributes;

  public GroupMemberAttribute(String userId, String groupId, Map<String, String> attributes) {
    this.userId = userId;
    this.groupId = groupId;
    this.attributes = attributes;
  }
}
