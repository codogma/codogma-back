package com.github.codogma.codogmaback.exception;

public class AccessTokenExpiredException extends RuntimeException {

  public AccessTokenExpiredException(String message) {
    super(message);
  }
}
