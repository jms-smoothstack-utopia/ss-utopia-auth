package com.ss.utopia.auth.client.email;

public class DeleteAccountEmail extends AbstractUrlEmail {

  public static final String DEFAULT_SUBJECT = "Confirm Your Account Deletion";
  public static final String DEFAULT_MESSAGE = "Please click the following link to confirm your account deletion."
      + "<br>If you've changed your mind, you can safely ignore this email.";

  public DeleteAccountEmail(String recipient, String url) {
    this(recipient, DEFAULT_SUBJECT, DEFAULT_MESSAGE, url);
  }

  public DeleteAccountEmail(String recipient, String subject, String message, String url) {
    super(recipient, subject, message, url);
  }
}
