package com.glomming.shared.sgs.exception;

import com.glomming.shared.sgs.ServiceName;

public class InvalidParameterException extends BaseException {

  public InvalidParameterException(String name, String message) {
    super(ServiceName.SERVICE_NAME, name, message);
  }
}
