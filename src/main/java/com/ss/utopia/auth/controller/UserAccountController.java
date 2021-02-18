package com.ss.utopia.auth.controller;

import com.ss.utopia.auth.config.Constants;
import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.service.UserAccountService;
import java.net.URI;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.API_V_0_1_ACCOUNTS)
public class UserAccountController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountController.class);
  private final UserAccountService userAccountService;

  public UserAccountController(UserAccountService userAccountService) {
    this.userAccountService = userAccountService;
  }

  @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<UUID> createNewAccount(@Valid @RequestBody CreateUserAccountDto createUserAccountDto) {
    LOGGER.info("POST accounts");
    var userAccount = userAccountService.createNewAccount(createUserAccountDto);
    var userId = userAccount.getId();
    var uri = URI.create(Constants.API_V_0_1_ACCOUNTS + "/" + userId);
    return ResponseEntity.created(uri).body(userId);
  }
}
