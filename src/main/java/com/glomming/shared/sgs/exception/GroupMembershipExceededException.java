package com.glomming.shared.sgs.exception;

import com.glomming.shared.sgs.ServiceName;

public class GroupMembershipExceededException extends BaseException {

  public GroupMembershipExceededException(String name, String message) {
    super(ServiceName.SERVICE_NAME, name, message);
  }
}
