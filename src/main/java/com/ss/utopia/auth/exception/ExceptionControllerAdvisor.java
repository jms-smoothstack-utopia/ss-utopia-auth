package com.ss.utopia.auth.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionControllerAdvisor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionControllerAdvisor.class);


  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(DuplicateEmailException.class)
  public Map<String, Object> duplicateEmailException(DuplicateEmailException ex) {
    LOGGER.error(ex.getMessage());

    return Map.of("error", "duplicate email, account already exists.",
                  "email", ex.getEmail());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
    LOGGER.error(ex.getMessage());

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

  private String getErrorMessageOrDefault(FieldError error) {
    var msg = error.getDefaultMessage();
    msg = msg == null || msg.isBlank() ? "Unknown validation failure." : msg;

    LOGGER.debug("Field" + error.getField() + " Message: " + msg);
    return msg;
  }
}
