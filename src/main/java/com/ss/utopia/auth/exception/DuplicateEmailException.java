package com.ss.utopia.auth.exception;

import org.springframework.dao.DuplicateKeyException;

public class DuplicateEmailException extends DuplicateKeyException {

  private final String email;

  public DuplicateEmailException(String email) {
    super("An account with the email '" + email + "' already exists.");
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
}

