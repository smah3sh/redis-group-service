package com.glomming.shared.sgs.exception;

import com.glomming.shared.sgs.ServiceName;

public class InvalidGroupException extends BaseException {

  public InvalidGroupException(String name, String message) {
    super(ServiceName.SERVICE_NAME, name, message);
  }
}
