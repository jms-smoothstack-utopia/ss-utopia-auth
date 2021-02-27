package com.ss.utopia.auth.client;

import com.ss.utopia.auth.dto.EmailDto;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
  private String apiHost;
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

  public void setApiHost(String apiHost) {
    this.apiHost = apiHost;
  }

  @Override
  public ResponseEntity<String> sendForgetPasswordEmail(String token, String email) {
    //Will replace with lambda and API gateway
    String mailUrl = apiHost + endpoint;

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
}
