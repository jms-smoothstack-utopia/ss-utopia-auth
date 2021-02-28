package com.ss.utopia.auth.exception;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason = "Email was not sent by AWS")
public class EmailNotSentException extends RuntimeException{

  private final String responseBody;
  private final HttpStatus statusCode;

  public EmailNotSentException() {
    this(null, null);
    //todo deprecate no-args constructor
  }

  public EmailNotSentException(String body, HttpStatus statusCode) {
    this.responseBody = body;
    this.statusCode = statusCode;
  }

  public Optional<String> getResponseBody() {
    return Optional.ofNullable(responseBody);
  }

  public Optional<HttpStatus> getStatusCode() {
    return Optional.ofNullable(statusCode);
  }
}
