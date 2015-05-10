package com.glomming.shared.sgs.bean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * Created by smahesh on 5/2/15.
 */
public class GroupMemberAttribute {
  public String groupId;
  public String userId;         // UserId
  public Map<String, String> attributes;

  public GroupMemberAttribute() {
  }

  public GroupMemberAttribute(String groupId, String userId, Map<String, String> attributes) {
    this.userId = userId;
    this.groupId = groupId;
    this.attributes = attributes;
  }

  public static String getKey(String groupId, String userId) {
    StringBuilder sb = new StringBuilder();
    sb.append(groupId);
    sb.append(":");
    sb.append(userId);
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GroupMemberAttribute that = (GroupMemberAttribute) o;

    if (!groupId.equals(that.groupId)) return false;
    if (!userId.equals(that.userId)) return false;
    return attributes.equals(that.attributes);

  }

  @Override
  public int hashCode() {
    int result = groupId.hashCode();
    result = 31 * result + userId.hashCode();
    result = 31 * result + attributes.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
