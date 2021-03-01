package com.ss.utopia.auth.client;

import com.ss.utopia.auth.client.email.AccountConfirmationEmail;
import com.ss.utopia.auth.dto.EmailDto;
import com.ss.utopia.auth.exception.EmailNotSentException;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
@ConfigurationProperties(value = "com.ss.utopia.email", ignoreUnknownFields = false)
public class RestTemplateEmailClient implements EmailClient {

  private final String endpoint = "/beta";

  @Setter
  private String apiHost;
  @Setter
  private String confirmationBaseUrl;

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
  public ResponseEntity<String> sendForgetPasswordEmail(String token, String email) {
    //String mailUrl = apiHost + endpoint;

    String mailUrl = "https://c0uenga9m6.execute-api.us-east-1.amazonaws.com/beta";

    //Set email Dto
    EmailDto emailToSend = new EmailDto();
    emailToSend.setEmail(email);
    emailToSend.setSubject("Reset Utopia password");
    String customerUrl = "http://localhost:4200/login/password/resetform/" + token;
    String content =
        "<h1>Utopia Password Change</h1>" +
            "<span>Please use this link to change your password (Link will expire in one day) </span>"
            +
            "<h2><a href='" + customerUrl + "'>Change password</a></h2>" +
            "<h3><span>Thanks</span></h3>" +
            "<h3>The Utopia team</h3>";
    emailToSend.setContent(content);

    //Send response
    return restTemplate.postForEntity(mailUrl, emailToSend, String.class);
  }

  @Override
  public void sendConfirmAccountEmail(String recipientEmail, UUID confirmationToken) {
    var postToUrl = apiHost + endpoint;

    var confirmationUrl = confirmationBaseUrl + "/" + confirmationToken;

    var email = new AccountConfirmationEmail(recipientEmail, confirmationUrl);
    var response = restTemplate.postForEntity(postToUrl, email, String.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      log.info("Account creation confirmation email sent to: " + recipientEmail);
    } else {
      //todo retry if possible
      log.error("Unable to send confirmation email.");
      log.error("Status code: " + response.getStatusCode().value());
      log.error("Response body: " + response.getBody());
      throw new EmailNotSentException(response.getBody(), response.getStatusCode());
    }
  }
}
