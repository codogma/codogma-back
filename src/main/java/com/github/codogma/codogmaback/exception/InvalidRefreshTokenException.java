package com.github.codogma.codogmaback.exception;

public class InvalidRefreshTokenException extends RuntimeException {

  public InvalidRefreshTokenException(String message) {
    super(message);
  }
}