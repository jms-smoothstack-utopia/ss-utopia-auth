package com.ss.utopia.auth.client.email;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public abstract class AbstractUrlEmail {

  @Getter
  private final String recipient;
  @Getter
  private final String subject;
  @Getter
  private final String message;
  @Getter
  private final String content;
  @Getter
  private final String url;

  public AbstractUrlEmail(String recipient, String subject, String message, String url) {
    this.recipient = recipient;
    this.subject = subject;
    this.message = message;
    this.url = url;
    this.content = createContent();
  }

  protected String createContent() {
    return "<h1>" + subject + "<h1>"
        + "<h2>" + message + "</h2>"
        + "<h2><a href='" + url + "'>" + url + "</a></h2>"
        + "<h3><span>Thanks!</span></h3>"
        + "<h3><span>The Utopia Team</h3>";
  }

  public String getEmail() {
    return this.recipient;
  }
}
