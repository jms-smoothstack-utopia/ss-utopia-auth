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

  public static final String ERROR_KEY = "error";
  public static final String STATUS_KEY = "status";

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(DuplicateEmailException.class)
  public Map<String, Object> duplicateEmailException(DuplicateEmailException ex) {
    log.error(ex.getMessage());

    return baseResponse(ex.getMessage(), HttpStatus.CONFLICT);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
    log.error(ex.getMessage());

    var response = baseResponse("Invalid field(s) in request.", HttpStatus.BAD_REQUEST);

    // get field name and error message as map
    var errors = ex.getBindingResult()
        .getAllErrors()
        .stream()
        .collect(
            Collectors.toMap(
                error -> ((FieldError) error).getField(),
                error -> getErrorMessageOrDefault((FieldError) error)));

    response.put("message", errors);
    return response;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoSuchElementException.class)
  public Map<String, Object> handleNoSuchElementException(NoSuchElementException ex) {
    log.error(ex.getMessage());

    return baseResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(EmailNotSentException.class)
  public Map<String, Object> handleEmailNotSentException(EmailNotSentException ex) {
    log.error(ex.getMessage());

    return baseResponse("Unable to send email. Please try again.",
                        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InvalidTokenException.class)
  public Map<String, Object> handleInvalidTokenException(InvalidTokenException ex) {
    log.error(ex.getMessage());

    return baseResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(IllegalCustomerAccountDeletionException.class)
  public Map<String, Object> illegalCustomerAccountDeletionException(
      IllegalCustomerAccountDeletionException ex) {
    log.error(ex.getMessage());

    var baseResponse = baseResponse(ex.getMessage(), HttpStatus.CONFLICT);
    baseResponse.put("role", ex.getUserAccount().getUserRole().getRoleName());
    return baseResponse;
  }

  private String getErrorMessageOrDefault(FieldError error) {
    var msg = error.getDefaultMessage();
    msg = msg == null || msg.isBlank() ? "Unknown validation failure." : msg;

    log.debug("Field" + error.getField() + " Message: " + msg);
    return msg;
  }

  private Map<String, Object> baseResponse(String errorMsg, HttpStatus status) {
    var response = new HashMap<String, Object>();
    response.put(ERROR_KEY, errorMsg);
    response.put(STATUS_KEY, status.value());
    return response;
  }
}
