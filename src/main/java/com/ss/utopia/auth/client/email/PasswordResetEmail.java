package com.ss.utopia.auth.client.email;


public class PasswordResetEmail extends AbstractUrlEmail {

  private static final String DEFAULT_SUBJECT = "Utopia Password Reset";
  private static final String DEFAULT_MESSAGE =
      "Please use this link to change your password (Link will expire in one day)";

  public PasswordResetEmail(String recipient, String url) {
    this(recipient, DEFAULT_SUBJECT, DEFAULT_MESSAGE, url);
  }

  public PasswordResetEmail(String recipient, String subject, String message, String url) {
    super(recipient, subject, message, url);
  }
}
