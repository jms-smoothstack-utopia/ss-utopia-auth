package com.ss.utopia.auth.controller;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.dto.DeleteAccountDto;
import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.security.permissions.AdminOnlyPermission;
import com.ss.utopia.auth.security.permissions.ServiceOnlyPermission;
import com.ss.utopia.auth.service.PasswordResetService;
import com.ss.utopia.auth.service.UserAccountService;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(EndpointConstants.API_V_0_1_ACCOUNTS)
@RequiredArgsConstructor
public class UserAccountController {

  private final UserAccountService userAccountService;
  private final PasswordResetService passwordResetService;

  @GetMapping("/test")
  public ResponseEntity<String> testEndpoint() {
    return ResponseEntity.ok("{\"msg\":\"You are authenticated.\"}");
  }

  @AdminOnlyPermission
  @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<UserAccount>> getAllAccounts() {
    var accounts = userAccountService.getAll();
    if (accounts.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(accounts);
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

  @PostMapping(value = "/password-reset",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<?> addPasswordReset(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
    var token = passwordResetService.addPasswordReset(resetPasswordDto);
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "/new-password/{token}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<?> tokenCheck(@PathVariable String token) {
    log.info("Checking token: " + token);
    if (passwordResetService.tokenCheck(token)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping(value = "/new-password",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<?> changePassword(@Valid @RequestBody NewPasswordDto newPasswordDto) {
    log.info("Updating password initiated");
    passwordResetService.changePassword(newPasswordDto);
    return ResponseEntity.ok().build();
  }

  @AdminOnlyPermission
  @DeleteMapping("/{accountId}")
  public ResponseEntity<?> deleteAccount(@PathVariable UUID accountId) {
    userAccountService.deleteAccountById(accountId);
    return ResponseEntity.noContent().build();
  }

  @ServiceOnlyPermission
  @DeleteMapping("/customer")
  public ResponseEntity<?> initiateCustomerDeletion(@Valid @RequestBody DeleteAccountDto deleteAccountDto) {
    userAccountService.initiateCustomerDeletion(deleteAccountDto);
    return ResponseEntity.noContent().build();
  }

  @ServiceOnlyPermission
  @DeleteMapping("/customer/{confirmationToken}")
  public ResponseEntity<UUID> completeCustomerDeletion(@PathVariable UUID confirmationToken) {
    var accountId = userAccountService.completeCustomerDeletion(confirmationToken);
    return ResponseEntity.ok(accountId);
  }
}
