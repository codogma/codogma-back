package com.github.codogma.codogmaback.exception;

public class ConfirmationTokenExpiredException extends RuntimeException {

  public ConfirmationTokenExpiredException(String message) {
    super(message);
  }
}