package com.ss.utopia.auth.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.ss.utopia.auth.client.email.AccountConfirmationEmail;
import com.ss.utopia.auth.client.email.DeleteAccountEmail;
import com.ss.utopia.auth.client.email.PasswordResetEmail;
import com.ss.utopia.auth.exception.EmailNotSentException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class RestTemplateEmailClientTest {

  RestTemplateBuilder restTemplateBuilder = Mockito.mock(RestTemplateBuilder.class);
  RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

  RestTemplateEmailClient emailClient;

  String mockSesEndpoint = "http://asdf.aws.com/email";
  String mockPasswordResetBaseUrl = "http://localhost:4200/password-reset";
  String mockConfirmationBaseUrl = "http://localhost:4200/confirm";
  String mockDeletionBaseUrl = "http://localhost:4200/account/delete";

  @BeforeEach
  void beforeEach() {
    when(restTemplateBuilder.build()).thenReturn(restTemplate);
    emailClient = new RestTemplateEmailClient();
    emailClient.setBuilder(restTemplateBuilder);
    emailClient.postConstruct();

    Mockito.verify(restTemplateBuilder).build();

    emailClient.setSesEndpoint(mockSesEndpoint);
    emailClient.setPasswordResetBaseUrl(mockPasswordResetBaseUrl);
    emailClient.setConfirmationBaseUrl(mockConfirmationBaseUrl);
    emailClient.setDeletionBaseUrl(mockDeletionBaseUrl);
  }

  @Test
  void test_sendForgotPasswordEmail_CreatesAndPostsPasswordResetObject() {
    var mockToken = UUID.randomUUID();
    var mockEmail = "test@test.com";

    var mockUrl = mockPasswordResetBaseUrl + "/" + mockToken;

    var expectedObject = new PasswordResetEmail(mockEmail, mockUrl);

    when(restTemplate.postForEntity(mockSesEndpoint, expectedObject, String.class))
        .thenReturn(ResponseEntity.ok("some response"));

    assertDoesNotThrow(() -> emailClient.sendForgotPasswordEmail(mockEmail, mockToken));

    Mockito.verify(restTemplate)
        .postForEntity(mockSesEndpoint, expectedObject, String.class);
  }

  @Test
  void test_sendForgotPasswordEmail_ThrowsEmailNotSentExceptionIfBadResponse() {
    var mockToken = UUID.randomUUID();
    var mockEmail = "test@test.com";

    var mockUrl = mockPasswordResetBaseUrl + "/" + mockToken;

    var expectedObject = new PasswordResetEmail(mockEmail, mockUrl);

    when(restTemplate.postForEntity(mockSesEndpoint, expectedObject, String.class))
        .thenReturn(ResponseEntity.badRequest().build());

    assertThrows(EmailNotSentException.class,
                 () -> emailClient.sendForgotPasswordEmail(mockEmail, mockToken));

    Mockito.verify(restTemplate)
        .postForEntity(mockSesEndpoint, expectedObject, String.class);
  }

  @Test
  void test_sendForgotPasswordEmail_ThrowsEmailNotSentExceptionIfNullResponse() {
    var mockToken = UUID.randomUUID();
    var mockEmail = "test@test.com";

    var mockUrl = mockPasswordResetBaseUrl + "/" + mockToken;

    var expectedObject = new PasswordResetEmail(mockEmail, mockUrl);

    when(restTemplate.postForEntity(mockSesEndpoint, expectedObject, String.class))
        .thenReturn(null);

    assertThrows(EmailNotSentException.class,
                 () -> emailClient.sendForgotPasswordEmail(mockEmail, mockToken));

    Mockito.verify(restTemplate)
        .postForEntity(mockSesEndpoint, expectedObject, String.class);
  }

  @Test
  void test_sendConfirmAccountEmail_CreatesAndPostsCorrectEmailObject() {
    var mockToken = UUID.randomUUID();
    var mockEmail = "test@test.com";

    var mockUrl = mockConfirmationBaseUrl + "/" + mockToken;

    var expectedObject = new AccountConfirmationEmail(mockEmail, mockUrl);

    when(restTemplate.postForEntity(mockSesEndpoint, expectedObject, String.class))
        .thenReturn(ResponseEntity.ok("some response"));

    assertDoesNotThrow(() -> emailClient.sendConfirmAccountEmail(mockEmail, mockToken));

    Mockito.verify(restTemplate)
        .postForEntity(mockSesEndpoint, expectedObject, String.class);
  }

  @Test
  void test_sendConfirmAccountEmail_ThrowsEmailNotSentExceptionIfBadResponse() {
    var mockToken = UUID.randomUUID();
    var mockEmail = "test@test.com";

    var mockUrl = mockConfirmationBaseUrl + "/" + mockToken;

    var expectedObject = new AccountConfirmationEmail(mockEmail, mockUrl);

    when(restTemplate.postForEntity(mockSesEndpoint, expectedObject, String.class))
        .thenReturn(ResponseEntity.badRequest().build());

    assertThrows(EmailNotSentException.class,
                 () -> emailClient.sendConfirmAccountEmail(mockEmail, mockToken));

    Mockito.verify(restTemplate)
        .postForEntity(mockSesEndpoint, expectedObject, String.class);
  }

  @Test
  void test_sendConfirmAccountEmail_ThrowsEmailNotSentExceptionIfNullResponse() {
    var mockToken = UUID.randomUUID();
    var mockEmail = "test@test.com";

    var mockUrl = mockConfirmationBaseUrl + "/" + mockToken;

    var expectedObject = new AccountConfirmationEmail(mockEmail, mockUrl);

    when(restTemplate.postForEntity(mockSesEndpoint, expectedObject, String.class))
        .thenReturn(null);

    assertThrows(EmailNotSentException.class,
                 () -> emailClient.sendConfirmAccountEmail(mockEmail, mockToken));

    Mockito.verify(restTemplate)
        .postForEntity(mockSesEndpoint, expectedObject, String.class);
  }

  @Test
  void test_sendDeleteAccountEmail_CreatesAndPostsCorrectEmailObject() {
    var mockToken = UUID.randomUUID();
    var mockEmail = "test@test.com";

    var mockUrl = mockDeletionBaseUrl + "/" + mockToken;

    var expectedObject = new DeleteAccountEmail(mockEmail, mockUrl);

    when(restTemplate.postForEntity(mockSesEndpoint, expectedObject, String.class))
        .thenReturn(ResponseEntity.ok("some response"));

    assertDoesNotThrow(() -> emailClient.sendDeleteAccountEmail(mockEmail, mockToken));

    Mockito.verify(restTemplate)
        .postForEntity(mockSesEndpoint, expectedObject, String.class);
  }

  @Test
  void test_sendDeleteAccountEmail_ThrowsEmailNotSentExceptionIfBadResponse() {
    var mockToken = UUID.randomUUID();
    var mockEmail = "test@test.com";

    var mockUrl = mockDeletionBaseUrl + "/" + mockToken;

    var expectedObject = new DeleteAccountEmail(mockEmail, mockUrl);

    when(restTemplate.postForEntity(mockSesEndpoint, expectedObject, String.class))
        .thenReturn(ResponseEntity.badRequest().build());

    assertThrows(EmailNotSentException.class,
                 () -> emailClient.sendDeleteAccountEmail(mockEmail, mockToken));

    Mockito.verify(restTemplate)
        .postForEntity(mockSesEndpoint, expectedObject, String.class);
  }

  @Test
  void test_sendDeleteAccountEmail_ThrowsEmailNotSentExceptionIfNullResponse() {
    var mockToken = UUID.randomUUID();
    var mockEmail = "test@test.com";

    var mockUrl = mockDeletionBaseUrl + "/" + mockToken;

    var expectedObject = new DeleteAccountEmail(mockEmail, mockUrl);

    when(restTemplate.postForEntity(mockSesEndpoint, expectedObject, String.class))
        .thenReturn(null);

    assertThrows(EmailNotSentException.class,
                 () -> emailClient.sendDeleteAccountEmail(mockEmail, mockToken));

    Mockito.verify(restTemplate)
        .postForEntity(mockSesEndpoint, expectedObject, String.class);
  }
}