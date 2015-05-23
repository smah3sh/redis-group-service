package com.glomming.shared.sgs.service;

import com.glomming.shared.sgs.bean.Group;
import com.glomming.shared.sgs.bean.GroupInvitation;
import com.glomming.shared.sgs.exception.DuplicateInvitationException;
import com.glomming.shared.sgs.exception.GroupMembershipExceededException;
import com.glomming.shared.sgs.exception.InvalidGroupException;
import com.glomming.shared.sgs.exception.InvalidGroupOperationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles inviting members to groups
 * Maintain lists of invitations sent and received by user as hashes
 *
 * InvitationsSent
 * key - inviterId:invitationsSent,
 * field - group:inviteeId - A player can send multiple invites to another player for different groups
 * value - invitationId
 *
 * InvitationsReceived
 * key - inviteeId:invitationsReceived
 * field - group - This has to be unique
 * value - inviterId:invitationId
 *
 * 1. Inviter + Invitee + Group combination is unique
 * 2. A user can send multiple invites to another user for different groups
 * 3. A user cannot get 2 invites for the same group
 *
 */
@Component
public class GroupInvitationService extends BaseGroupService {

  private static final Logger logger = LoggerFactory.getLogger(GroupInvitationService.class);

  @Autowired
  private SimpleGroupService simpleGroupService;



  /**
   * Invite user only iff he does not have outstanding invitation for this group and is not already member
   * @param inviterId
   * @param inviteeId
   */
  public GroupInvitation inviteUserToGroup(String inviterId, String inviteeId, String groupId) throws DuplicateInvitationException, InvalidGroupException, InvalidGroupOperationException, GroupMembershipExceededException {

    // Check if group is valid, throws exception if not
    Group group = simpleGroupService.findGroup(groupId);

    // Check membership count
    if (group.currentSize >= group.maxSize) {
      throw new GroupMembershipExceededException("", group.toString());
    }

    // Check if user is member of this group
    boolean isMember = simpleGroupService.isMember(inviteeId, group.id);
    if (isMember) {
      StringBuilder sb = new StringBuilder();
      sb.append("User ");
      sb.append(inviteeId);
      sb.append(" already member of " + group.toString());
      throw new InvalidGroupOperationException("", sb.toString());
    }

    // Check if user already has outstanding invitation for this group
    String invitationId = UUID.randomUUID().toString();
    String message = null;
    long result = jedisCluster.hsetnx(GroupInvitation.createInvitationsReceivedKey(inviteeId),
        groupId, GroupInvitation.createInvitationReceivedValue(inviterId, invitationId));
    if (result > 0) {
      // Success
      result = jedisCluster.hsetnx(GroupInvitation.createInvitationsSentKey(inviterId),
          GroupInvitation.createInvitationSentFieldName(groupId, inviteeId), invitationId);
      if (result == 0) {
        message = "Cannot invite same user twice to the same group";
      }
    } else {
      message = "User " + inviteeId + " already has outstanding invitation to this group";
    }
    if (!StringUtils.isEmpty(message)) {
      throw new DuplicateInvitationException("", message);
    }
    return new GroupInvitation(inviterId, inviteeId, groupId, invitationId);
  }

  /**
   * Accept invite only if invitationIds match, cleanup invitations and add invitee to group
   * @param groupInvitation
   */
  public String acceptInviteToGroup(GroupInvitation groupInvitation) {
    String value = jedisCluster.hget(GroupInvitation.createInvitationsReceivedKey(groupInvitation.inviteeId), groupInvitation.groupId);
    String result = null;
    if (value.equals(GroupInvitation.createInvitationReceivedValue(groupInvitation.inviterId, groupInvitation.invitationId))) {
      // Add member
      try {
        result = simpleGroupService.addGroupMember(groupInvitation.groupId, groupInvitation.inviteeId);
        // Remove this invitation from list of invitations sent and received for sender and receiver
        this.deleteGroupInviteSent(groupInvitation.inviterId, groupInvitation.groupId, groupInvitation.inviteeId);
        this.deleteGroupInviteReceived(groupInvitation.inviteeId, groupInvitation.groupId);
      } catch (GroupMembershipExceededException e) {
        logger.error("Cannot accept invite", e);
      } catch (InvalidGroupException e) {
        logger.error("Cannot accept invite", e);
      }
    }
    return result;
  }

  /**
   * Remove invitations from both the sender and receiver list
   * @param groupInvitation
   */
  public void rejectInviteToGroup(GroupInvitation groupInvitation) {
    String value = jedisCluster.hget(GroupInvitation.createInvitationsReceivedKey(groupInvitation.inviteeId), groupInvitation.groupId);
    if (value.equals(GroupInvitation.createInvitationReceivedValue(groupInvitation.inviterId, groupInvitation.invitationId))) {
      // Remove from both structures
      this.deleteGroupInviteSent(groupInvitation.inviterId, groupInvitation.groupId, groupInvitation.inviteeId);
      this.deleteGroupInviteReceived(groupInvitation.inviteeId, groupInvitation.groupId);
    }
  }

  /**
   * List invitations received. There can be only one outstanding invitation per group
   * @param inviteeId
   * @return
   */
  public List<GroupInvitation> listGroupInvitesReceived(String inviteeId) {
    List<GroupInvitation> listInvitations = new ArrayList<>();
    Map<String, String> results = jedisCluster.hgetAll(GroupInvitation.createInvitationsReceivedKey(inviteeId));
    for (Map.Entry<String, String> result : results.entrySet()) {
      String [] tokens = result.getValue().split(":");  // inviterId:invitationId
      // Value is a combination of
      GroupInvitation groupInvitation = new GroupInvitation(tokens[0], inviteeId, result.getKey(), tokens[1]);
      listInvitations.add(groupInvitation);
    }
    return listInvitations;
  }

  /**
   * List invitations sent. There can be only one invitation sent per group + player.
   * @param inviterId
   * @return
   */
  public List<GroupInvitation> listGroupInvitesSent(String inviterId) {
    List<GroupInvitation> listInvitations = new ArrayList<>();
    Map<String, String> results = jedisCluster.hgetAll(GroupInvitation.createInvitationsSentKey(inviterId));
    for (Map.Entry<String, String> result : results.entrySet()) {
      String [] tokens = result.getKey().split(":");
      GroupInvitation groupInvitation = new GroupInvitation(inviterId, tokens[1], tokens[0], result.getValue());
      listInvitations.add(groupInvitation);
    }
    return listInvitations;
  }

  /**
   * Delete a group invitation
   * @param inviteeId
   * @param groupId
   */
  private long deleteGroupInviteReceived(String inviteeId, String groupId) {
    String key = GroupInvitation.createInvitationsReceivedKey(inviteeId);
      // delete invite for this group
    return jedisCluster.hdel(key, groupId);
  }

  /**
   * Also delete from the corresponding recipients list
   * @param inviterId
   * @param groupId
   */
  private long deleteGroupInviteSent(String inviterId, String groupId, String inviteeId) {
    String key = GroupInvitation.createInvitationsSentKey(inviterId);
    // delete invite for this group
    return jedisCluster.hdel(key, GroupInvitation.createInvitationSentFieldName(groupId, inviteeId));
  }

}
