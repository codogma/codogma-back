package com.github.codogma.codogmaback.exception;

public class UsernameOrEmailNotFoundException extends RuntimeException {

  public UsernameOrEmailNotFoundException(String message) {
    super(message);
  }
}