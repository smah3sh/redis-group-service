package com.glomming.shared.sgs.bean;


/**
 * Created by smahesh on 5/2/15.
 */
public class GroupInvitation {

  public String inviterId;   // The inviter player
  public String inviteeId;     // The invited player
  public String groupId;     // The group id
  public String invitationId;

  public GroupInvitation(String inviterId, String inviteeId, String groupId, String invitationId) {
    this.inviterId = inviterId;
    this.inviteeId = inviteeId;
    this.groupId = groupId;
    this.invitationId = invitationId;
  }

  public static final String INVITATIONS_SENT = "InvitationsSent";
  public static final String INVITATIONS_RECEVIED = "InvitationsReceived";

  public static String createInvitationSentFieldName(String groupId, String inviteeId) {
    StringBuilder sb = new StringBuilder();
    sb.append(groupId);
    sb.append(":");
    sb.append(inviteeId);
    return sb.toString();
  }

  public static String createInvitationReceivedValue(String inviterId, String invitationId) {
    StringBuilder sb = new StringBuilder();
    sb.append(inviterId);
    sb.append(":");
    sb.append(invitationId);
    return sb.toString();
  }

  public static String createInvitationsSentKey(String inviterId) {
    StringBuilder sb = new StringBuilder();
    sb.append(inviterId);
    sb.append(":");
    sb.append(INVITATIONS_SENT);
    return sb.toString();
  }

  public static String createInvitationsReceivedKey(String inviteeId) {
    StringBuilder sb = new StringBuilder();
    sb.append(inviteeId);
    sb.append(":");
    sb.append(INVITATIONS_RECEVIED);
    return sb.toString();
  }


  @Override
  public String toString() {
    return "GroupInvitation{" +
        "inviterId='" + inviterId + '\'' +
        ", inviteeId='" + inviteeId + '\'' +
        ", groupId='" + groupId + '\'' +
        ", invitationId='" + invitationId + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GroupInvitation that = (GroupInvitation) o;

    if (inviterId != null ? !inviterId.equals(that.inviterId) : that.inviterId != null) return false;
    if (inviteeId != null ? !inviteeId.equals(that.inviteeId) : that.inviteeId != null) return false;
    if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
    return !(invitationId != null ? !invitationId.equals(that.invitationId) : that.invitationId != null);

  }

  @Override
  public int hashCode() {
    int result = inviterId != null ? inviterId.hashCode() : 0;
    result = 31 * result + (inviteeId != null ? inviteeId.hashCode() : 0);
    result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
    result = 31 * result + (invitationId != null ? invitationId.hashCode() : 0);
    return result;
  }
}
