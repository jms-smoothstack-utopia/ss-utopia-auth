package com.ss.utopia.auth.controller;

import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.service.PasswordResetService;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/password")
public class PasswordResetController{

  private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetController.class);
  private final PasswordResetService passwordResetService;

  public PasswordResetController(PasswordResetService passwordResetService){
    this.passwordResetService = passwordResetService;
  }

  //SSUTO-10 - Reset Password
  @PostMapping(value = "/reset/password-reset", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  public ResponseEntity<?> addPasswordReset(@Valid @RequestBody ResetPasswordDto resetPasswordDto){
    LOGGER.info("Initiating Password reset for: " + resetPasswordDto);
    passwordResetService.addPasswordReset(resetPasswordDto);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(value = "/reset/token/{token}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  public ResponseEntity<?> verifyPasswordToken(@PathVariable String token){
    passwordResetService.verifyToken(token);
    return ResponseEntity.noContent().build();
  }
}
