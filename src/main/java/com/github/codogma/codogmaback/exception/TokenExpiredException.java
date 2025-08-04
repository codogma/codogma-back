package com.github.codogma.codogmaback.exception;

public class TokenExpiredException extends RuntimeException {

  public TokenExpiredException(String message) {
    super(message);
  }
}