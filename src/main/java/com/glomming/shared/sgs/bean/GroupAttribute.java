package com.glomming.shared.sgs.bean;

import java.util.Map;

/**
 * Created by smahesh on 5/2/15.
 */
public class GroupAttribute {
  public String groupId;         // Key
  public Map<String, String> attributes;

  public GroupAttribute(String groupId, Map<String, String> attributes) {
    this.groupId = groupId;
    this.attributes = attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GroupAttribute that = (GroupAttribute) o;

    if (!groupId.equals(that.groupId)) return false;
    return attributes.equals(that.attributes);

  }

  @Override
  public int hashCode() {
    int result = groupId.hashCode();
    result = 31 * result + attributes.hashCode();
    return result;
  }
}
