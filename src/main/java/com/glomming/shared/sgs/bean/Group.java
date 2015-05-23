package com.glomming.shared.sgs.bean;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by smahesh on 5/2/15.
 */
public class Group {

  public static final String ID = "id";
  public static final String APP_NAME = "appName";
  public static final String NAME = "name";
  public static final String MAX_SIZE = "maxSize";
  public static final String CURRENT_SIZE = "currentSize";
  public static final String OWNER_ID = "ownerId";
  public static final String GROUP_JOIN_STATE = "groupJoinState";
  public static final String CREATED = "created";
  public static final String LAST_UPDATED = "lastUpdated";

  public String id;         // Unique id across the whole system
  public String appName;      // The app this group belongs to; group names are unique within the app
  public String name;       // The name of the group; unique within the app
  public long maxSize;       // The max size of the group, set to 0 for no upper limit
  public long currentSize;  // The current number of members in this group
  public String ownerId;    // The owner user
  public GroupJoinState groupJoinState;   // If the group is open or closed
  public Date created;      // Group create datetime
  public Date lastUpdated;  // Group last update datetime

  public static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

  public Group() {
  }

  public Group(String id, String appId, String name, long maxSize, String ownerId, GroupJoinState groupJoinState, Date created, Date lastUpdated) {
    this.id = id;
    this.name = name;
    this.appName = appId;
    this.maxSize = maxSize;
    this.currentSize = 0;
    this.ownerId = ownerId;
    this.groupJoinState = groupJoinState;
    this.created = created;
    this.lastUpdated = lastUpdated;
  }

  public Map<String, String> toMap() {
    Map<String, String> map = new HashMap<>();
    map.put(ID, id);
    map.put(APP_NAME, appName);
    map.put(NAME, name);
    map.put(MAX_SIZE, Long.toString(maxSize));
    map.put(CURRENT_SIZE, Long.toString(currentSize));
    map.put(OWNER_ID, ownerId);
    map.put(GROUP_JOIN_STATE, groupJoinState.name());
    map.put(CREATED, dateFormat.format(created));
    map.put(LAST_UPDATED, dateFormat.format(lastUpdated));
    return map;
  }

  public static Group fromMap(Map<String, String> groupMap) throws ParseException {
    Group group = new Group();
    group.id = groupMap.get(ID);
    group.appName = groupMap.get(APP_NAME);
    group.name = groupMap.get(NAME);
    group.maxSize = Long.valueOf(groupMap.get(MAX_SIZE));
    group.currentSize = Long.valueOf(groupMap.get(CURRENT_SIZE));
    group.ownerId = groupMap.get(OWNER_ID);
    group.groupJoinState = GroupJoinState.valueOf(groupMap.get(GROUP_JOIN_STATE));
    group.created = dateFormat.parse(groupMap.get(CREATED));
    group.lastUpdated = dateFormat.parse(groupMap.get(LAST_UPDATED));
    return group;
  }

  public static String getKey(String groupId) {
    return groupId;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
