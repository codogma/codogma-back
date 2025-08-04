package com.github.codogma.codogmaback.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

  public UsernameAlreadyExistsException(String message) {
    super(message);
  }
}