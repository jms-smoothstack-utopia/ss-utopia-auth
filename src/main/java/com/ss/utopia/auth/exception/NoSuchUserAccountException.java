package com.ss.utopia.auth.exception;

import java.util.NoSuchElementException;
import java.util.UUID;

public class NoSuchUserAccountException extends NoSuchElementException {

  public NoSuchUserAccountException(UUID ownerAccountId) {
    super("No UserAccount found with id=" + ownerAccountId);
  }

  public NoSuchUserAccountException(String email) {
    super("No UserAccount found with email=" + email);
  }
}
