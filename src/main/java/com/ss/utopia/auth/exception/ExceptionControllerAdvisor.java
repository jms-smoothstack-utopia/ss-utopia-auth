package com.ss.utopia.auth.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvisor {

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(DuplicateEmailException.class)
  public Map<String, Object> duplicateEmailException(DuplicateEmailException ex) {
    log.error(ex.getMessage());

    return Map.of("error", "duplicate email, account already exists.",
                  "email", ex.getEmail());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
    log.error(ex.getMessage());

    var response = new HashMap<String, Object>();

    response.put("error", "Invalid field(s) in request.");
    response.put("status", 400);

    // get field name and error message as map
    var errors = ex.getBindingResult()
        .getAllErrors()
        .stream()
        .collect(
            Collectors.toMap(error -> ((FieldError) error).getField(),
                             error -> getErrorMessageOrDefault((FieldError) error)));

    response.put("message", errors);
    return response;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoSuchElementException.class)
  public Map<String, Object> handleNoSuchElementException(NoSuchElementException ex) {
    log.error(ex.getMessage());

    var response = new HashMap<String, Object>();
    response.put("error", ex.getMessage());
    response.put("status", HttpStatus.BAD_REQUEST.value());

    return response;
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(EmailNotSentException.class)
  public Map<String, Object> handleEmailNotSentException(EmailNotSentException ex) {
    log.error(ex.getMessage());

    var response = new HashMap<String, Object>();
    response.put("error", "Unable to send email. Please try again.");
    response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    return response;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InvalidTokenException.class)
  public Map<String, Object> handleInvalidTokenException(InvalidTokenException ex) {
    log.error(ex.getMessage());

    var response = new HashMap<String, Object>();
    response.put("error", ex.getMessage());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    return response;
  }

  private String getErrorMessageOrDefault(FieldError error) {
    var msg = error.getDefaultMessage();
    msg = msg == null || msg.isBlank() ? "Unknown validation failure." : msg;

    log.debug("Field" + error.getField() + " Message: " + msg);
    return msg;
  }
}
