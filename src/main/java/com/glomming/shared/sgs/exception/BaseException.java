package com.glomming.shared.sgs.exception;

public class BaseException extends Exception {

  public static final String UNKNOWN = "UNKNOWN";
  public static final String DESERIALIZATION_ERROR = "DESERIALIZATION_ERROR";
  public static final String INVALID_DATA = "INVALID_DATA";
  public static final String ENTITY_ALREADY_EXISTS = "ENTITY_ALREADY_EXISTS";

  public String serviceName;
  public String name;

  public BaseException(String serviceName, String name, String message) {
    super(message);
    this.serviceName = serviceName;
    this.name = name;
  }

  public String toString() {
    return serviceName + "-" + name + ": " + getMessage();
  }

  public String getErrorMessage(Class<?> originatingClass) {
    return "Error #" + toString();
  }

}
