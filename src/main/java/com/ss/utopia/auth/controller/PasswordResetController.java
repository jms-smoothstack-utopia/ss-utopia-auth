package com.ss.utopia.auth.controller;

import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.service.PasswordResetService;
import java.util.Map;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(EndpointConstants.API_V_0_1_ACCOUNTS)
public class PasswordResetController{

  private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetController.class);
  private final PasswordResetService passwordResetService;

  public PasswordResetController(PasswordResetService passwordResetService){
    this.passwordResetService = passwordResetService;
  }

  //SSUTO-10 - Reset Password
  @PostMapping(value = "/password-reset", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  public ResponseEntity<Map<String, String>> addPasswordReset(@Valid @RequestBody ResetPasswordDto resetPasswordDto){
    var object = passwordResetService.addPasswordReset(resetPasswordDto);
    if (object != null){
        return ResponseEntity.ok().body(object);
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping(value = "/new-password/{token}", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> tokenCheck(@PathVariable String token){
    LOGGER.info("Checking token: " + token);
    if (passwordResetService.tokenCheck(token)){
      return ResponseEntity.ok().body(Map.of("description", "token is valid"));
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping(value = "/new-password", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody NewPasswordDto newPasswordDto){
    LOGGER.info("Updating password initiated");
    var object = passwordResetService.changePassword(newPasswordDto);
    if (object != null){
      return ResponseEntity.ok().body(object);
    }
   return ResponseEntity.notFound().build();
  }
}
