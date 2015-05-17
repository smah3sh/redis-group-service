package com.glomming.shared.sgs.bean;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * Created by smahesh on 5/2/15.
 */
public class GroupJoinRequest {

  public String groupId;
  public String inviterUserId;        // Has to be a member
  public String invitedUserId;        // Can be a non-member
  public String invitationId;         // Unique invitation Id
  public Date   inviteTimestamp;      // When invited

  public GroupJoinRequest() {
  }

  public GroupJoinRequest(String groupId, String inviterUserId, String invitedUserId, String invitationId, Date inviteTimestamp) {
    this.groupId = groupId;
    this.inviterUserId = inviterUserId;
    this.invitedUserId = invitedUserId;
    this.invitationId = invitationId;
    this.inviteTimestamp = inviteTimestamp;
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

    GroupJoinRequest that = (GroupJoinRequest) o;

    if (!groupId.equals(that.groupId)) return false;
    if (!inviterUserId.equals(that.inviterUserId)) return false;
    if (!invitedUserId.equals(that.invitedUserId)) return false;
    if (!invitationId.equals(that.invitationId)) return false;
    return inviteTimestamp.equals(that.inviteTimestamp);

  }

  @Override
  public int hashCode() {
    int result = groupId.hashCode();
    result = 31 * result + inviterUserId.hashCode();
    result = 31 * result + invitedUserId.hashCode();
    result = 31 * result + invitationId.hashCode();
    result = 31 * result + inviteTimestamp.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
