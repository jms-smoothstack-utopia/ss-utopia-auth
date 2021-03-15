package com.ss.utopia.auth.client;

import com.ss.utopia.auth.client.email.AbstractUrlEmail;
import com.ss.utopia.auth.client.email.AccountConfirmationEmail;
import com.ss.utopia.auth.client.email.DeleteAccountEmail;
import com.ss.utopia.auth.client.email.PasswordResetEmail;
import com.ss.utopia.auth.exception.EmailNotSentException;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
@ConfigurationProperties(value = "com.ss.utopia.email", ignoreUnknownFields = false)
public class RestTemplateEmailClient implements EmailClient {

  @Setter
  private String sesEndpoint;
  @Setter
  private String passwordResetBaseUrl;
  @Setter
  private String confirmationBaseUrl;
  @Setter
  private String deletionBaseUrl;

  private RestTemplateBuilder builder;
  private RestTemplate restTemplate;

  @Autowired
  public void setBuilder(RestTemplateBuilder builder) {
    this.builder = builder;
  }

  @PostConstruct
  public void postConstruct() {
    restTemplate = builder.build();
  }

  @Override
  public void sendForgetPasswordEmail(String token, String recipientEmail) {
    var resetPasswordUrl = passwordResetBaseUrl + "/" + token;

    var email = new PasswordResetEmail(recipientEmail, resetPasswordUrl);
    var response = restTemplate.postForEntity(sesEndpoint, email, String.class);

    handleResponse(response, email);
  }

  @Override
  public void sendConfirmAccountEmail(String recipientEmail, UUID confirmationToken) {
    var confirmationUrl = confirmationBaseUrl + "/" + confirmationToken;

    var email = new AccountConfirmationEmail(recipientEmail, confirmationUrl);
    var response = restTemplate.postForEntity(sesEndpoint, email, String.class);

    handleResponse(response, email);
  }

  @Override
  public void sendDeleteAccountEmail(String recipient, UUID token) {
    var deletionUrl = deletionBaseUrl + "/" + token;
    var email = new DeleteAccountEmail(recipient, deletionUrl);
    var response = restTemplate.postForEntity(sesEndpoint, email, String.class);

    handleResponse(response, email);
  }

  private void handleResponse(ResponseEntity<String> response, AbstractUrlEmail email) {
    if (response == null) {
      throw new EmailNotSentException("NULL RESPONSE", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    if (response.getStatusCode().is2xxSuccessful()) {
      log.debug("Email sent to: " + email.getRecipient());
      log.debug(email.getSubject());
    } else {
      log.error("Unable to send confirmation email.");
      log.error("Status code: " + response.getStatusCode().value());
      log.error("Response body: " + response.getBody());
      throw new EmailNotSentException(response.getBody(), response.getStatusCode());
    }
  }
}
