package com.glomming.shared.sgs.exception;

import com.glomming.shared.sgs.ServiceName;

public class InvalidGroupOperationException extends BaseException {

  public InvalidGroupOperationException(String name, String message) {
    super(ServiceName.SERVICE_NAME, name, message);
  }
}
