package com.ss.utopia.auth.controller;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.dto.DeleteAccountDto;
import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.exception.InvalidTokenException;
import com.ss.utopia.auth.security.permissions.AdminOnlyPermission;
import com.ss.utopia.auth.security.permissions.ServiceOnlyPermission;
import com.ss.utopia.auth.service.AccountActionTokenService;
import com.ss.utopia.auth.service.UserAccountService;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
  private final AccountActionTokenService accountActionTokenService;

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
  public ResponseEntity<UUID> createNewAccount(@Valid @RequestBody
                                                   CreateUserAccountDto createUserAccountDto) {
    log.info("POST accounts");
    var userAccount = userAccountService.createNewAccount(createUserAccountDto);
    var userId = userAccount.getId();
    var uri = URI.create(EndpointConstants.API_V_0_1_ACCOUNTS + "/" + userId);
    return ResponseEntity.created(uri).body(userId);
  }

  @PutMapping("/confirm/{confirmationTokenId}")
  public ResponseEntity<Void> confirmAccountRegistration(@PathVariable UUID confirmationTokenId) {
    userAccountService.confirmAccountRegistration(confirmationTokenId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/password-reset",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Void> initiatePasswordReset(@Valid @RequestBody
                                                        ResetPasswordDto resetPasswordDto) {
    userAccountService.initiatePasswordReset(resetPasswordDto.getEmail());
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/new-password",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Void> completePasswordReset(@Valid @RequestBody
                                                        NewPasswordDto newPasswordDto) {
    log.info("Updating password initiated");
    userAccountService.completePasswordReset(newPasswordDto);
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "/new-password/{token}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Void> tokenCheck(@PathVariable UUID token) {
    log.info("Checking token: " + token);
    try {
      accountActionTokenService.getAndValidateToken(token);
      return ResponseEntity.ok().build();
    } catch (InvalidTokenException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  @AdminOnlyPermission
  @DeleteMapping("/{accountId}")
  public ResponseEntity<Void> deleteAccount(@PathVariable UUID accountId) {
    userAccountService.deleteAccountById(accountId);
    return ResponseEntity.noContent().build();
  }

  @ServiceOnlyPermission
  @PutMapping(value = "/customer/{customerId}", consumes = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<Void> updateCustomerEmail(@PathVariable UUID customerId,
                                                  @Valid @Email @NotNull @NotBlank @RequestBody
                                                      String newEmail) {
    userAccountService.updateEmail(customerId, newEmail);
    return ResponseEntity.ok().build();
  }

  @ServiceOnlyPermission
  @DeleteMapping("/customer")
  public ResponseEntity<Void> initiateCustomerDeletion(@Valid @RequestBody
                                                           DeleteAccountDto deleteAccountDto) {
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
