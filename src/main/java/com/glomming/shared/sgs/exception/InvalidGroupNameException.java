package com.glomming.shared.sgs.exception;

import com.glomming.shared.sgs.ServiceName;

public class InvalidGroupNameException extends BaseException {

  public InvalidGroupNameException(String name, String message) {
    super(ServiceName.SERVICE_NAME, name, message);
  }
}
