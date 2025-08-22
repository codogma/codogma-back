package com.github.codogma.codogmaback.exception;

import org.springframework.security.core.AuthenticationException;

public class DeviceMismatchException extends AuthenticationException {

  public DeviceMismatchException(String message) {
    super(message);
  }
}