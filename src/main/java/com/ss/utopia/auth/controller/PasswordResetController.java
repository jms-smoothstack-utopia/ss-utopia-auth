package com.ss.utopia.auth.controller;

import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.service.PasswordResetService;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(EndpointConstants.ACCOUNTS_ENDPOINT)
public class PasswordResetController{

  private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetController.class);
  private final PasswordResetService passwordResetService;

  public PasswordResetController(PasswordResetService passwordResetService){
    this.passwordResetService = passwordResetService;
  }

  //SSUTO-10 - Reset Password
  @PostMapping(value = "/password-reset", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> addPasswordReset(@Valid @RequestBody ResetPasswordDto resetPasswordDto){
    LOGGER.info("Initiating Password reset for: " + resetPasswordDto.getEmail());
    return ResponseEntity.of(Optional.ofNullable(passwordResetService.addPasswordReset(resetPasswordDto)));
  }

  @PostMapping(value = "/new-password", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> verifyPasswordToken(@Valid @RequestBody NewPasswordDto newPasswordDto){
    LOGGER.info("Updating password initiated");
   return passwordResetService.verifyToken(newPasswordDto);
  }
}
