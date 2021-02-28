package com.ss.utopia.auth.client.email;

import lombok.Data;

@Data
public class AccountConfirmationEmail {

  private final String email;
  private final String subject = "Confirm Utopia Account.";
  private final String content;
  private final String confirmationUrl;

  public AccountConfirmationEmail(String recipient, String confirmationUrl) {
    this.email = recipient;
    this.confirmationUrl = confirmationUrl;
    this.content = createContent();
  }

  private String createContent() {
    return "<h1>Confirm your Utopia Account</h1>"
        + "<span>Please click the following link to confirm your account!</span>"
        + "<h2><a href='" + confirmationUrl + "'>Confirm account</a></h2>"
        + "<h3><span>Thanks</span></h3>"
        + "<h3><span>The Utopia Team</h3>";
  }

}
