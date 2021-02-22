package com.ss.utopia.auth.client;

import com.ss.utopia.auth.entity.UserAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
@ConfigurationProperties(value = "com.ss.utopia.email", ignoreUnknownFields = false)
public class RestTemplateEmailClient implements EmailClient {

  private final String enpoint = "/registration";
  private final RestTemplateBuilder builder;
  private RestTemplate restTemplate;

  @Override
  public void getAccountConfirmation(UserAccount account) {

  }

  @Override
  public void setAccountConfirmed(UserAccount account) {

  }
}
