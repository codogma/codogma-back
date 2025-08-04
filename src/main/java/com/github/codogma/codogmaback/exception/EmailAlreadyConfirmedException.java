package com.github.codogma.codogmaback.exception;

public class EmailAlreadyConfirmedException extends RuntimeException {

  public EmailAlreadyConfirmedException(String message) {
    super(message);
  }
}