package com.ss.utopia.auth.exception;

import com.ss.utopia.auth.entity.AccountActionToken;

public class InvalidTokenException extends IllegalStateException {
  public InvalidTokenException(String token) {
    super("Invalid password reset token="+token);
  }

  public InvalidTokenException(AccountActionToken token) {
    super("Invalid action token id="+token.getToken());
  }
}
