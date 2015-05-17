package com.glomming.shared.sgs.exception;

import com.glomming.shared.sgs.ServiceName;

public class DuplicateInvitationException extends BaseException {

  public DuplicateInvitationException(String name, String message) {
    super(ServiceName.SERVICE_NAME, name, message);
  }
}
