package com.ss.utopia.auth.exception;

public class DuplicateEmailException extends IllegalStateException {

  private final String email;

  public DuplicateEmailException(String email) {
    super("An account with the email '" + email + "' already exists.");
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
}

