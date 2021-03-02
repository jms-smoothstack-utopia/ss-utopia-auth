package com.ss.utopia.auth.controller;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.service.UserAccountService;
import java.net.URI;
import java.util.UUID;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(EndpointConstants.API_V_0_1_ACCOUNTS)
public class UserAccountController {

  private final UserAccountService userAccountService;

  public UserAccountController(UserAccountService userAccountService) {
    this.userAccountService = userAccountService;
  }

  @GetMapping("/test")
  public ResponseEntity<String> testEndpoint() {
    return ResponseEntity.ok("{\"msg\":\"You are authenticated.\"}");
  }

  @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<UUID> createNewAccount(@Valid @RequestBody CreateUserAccountDto createUserAccountDto) {
    log.info("POST accounts");
    var userAccount = userAccountService.createNewAccount(createUserAccountDto);
    var userId = userAccount.getId();
    var uri = URI.create(EndpointConstants.API_V_0_1_ACCOUNTS + "/" + userId);
    return ResponseEntity.created(uri).body(userId);
  }

  @PutMapping("/confirm/{confirmationTokenId}")
  public ResponseEntity<?> confirmAccountRegistration(@PathVariable UUID confirmationTokenId) {
    userAccountService.confirmAccountRegistration(confirmationTokenId);
    return ResponseEntity.noContent().build();
  }
}
