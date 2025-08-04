package com.github.codogma.codogmaback.exception;

public class EmailNotConfirmedException extends RuntimeException {

  public EmailNotConfirmedException(String message) {
    super(message);
  }
}