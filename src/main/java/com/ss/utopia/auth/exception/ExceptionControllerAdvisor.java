package com.ss.utopia.auth.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionControllerAdvisor {

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(DuplicateEmailException.class)
  public Map<String, Object> duplicateEmailException(DuplicateEmailException ex) {
    return Map.of("error", "duplicate email, account already exists.",
                  "email", ex.getEmail());
  }

}
