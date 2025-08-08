package com.github.codogma.codogmaback.exception;

public class RefreshTokenExpiredException extends RuntimeException {

  public RefreshTokenExpiredException(String message) {
    super(message);
  }
}