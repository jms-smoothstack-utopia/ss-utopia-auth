package com.ss.utopia.auth.client.email;

public class AccountConfirmationEmail extends AbstractUrlEmail {

  private static final String DEFAULT_SUBJECT = "Confirm Your Utopia Account.";
  private static final String DEFAULT_MESSAGE =
      "Please click the following link to confirm your account!";

  public AccountConfirmationEmail(String recipient, String url) {
    this(recipient, DEFAULT_SUBJECT, DEFAULT_MESSAGE, url);
  }

  public AccountConfirmationEmail(String recipient, String subject, String message, String url) {
    super(recipient, subject, message, url);
  }
}
