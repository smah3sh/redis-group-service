package com.glomming.shared.sgs.exception;

import com.glomming.shared.sgs.ServiceName;

public class OwnerLeaveGroupException extends BaseException {

  public OwnerLeaveGroupException(String name, String message) {
    super(ServiceName.SERVICE_NAME, name, message);
  }
}
