package com.glomming.shared.sgs.bean;

/**
 * Created by smahesh on 5/2/15.
 */
public class GroupIdByName {

  public static String getKey(String appName) {
    StringBuffer sb = new StringBuffer();
    sb.append(appName);
    sb.append(":");
    sb.append(GroupIdByName.class.getSimpleName());
    return sb.toString();
  }
}
