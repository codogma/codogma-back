package com.github.codogma.codogmaback.exception;

public class CommentNotFoundException extends RuntimeException {

  public CommentNotFoundException(String message) {
    super(message);
  }
}