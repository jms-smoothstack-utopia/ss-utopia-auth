package com.ss.utopia.auth.exception;

public class InvalidTokenException extends IllegalStateException {
  public InvalidTokenException(String token) {
    super("Invalid password reset token="+token);
  }
}
