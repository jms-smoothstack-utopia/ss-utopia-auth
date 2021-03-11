package com.ss.utopia.auth.service;

import com.ss.utopia.auth.client.EmailClient;
import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.dto.DeleteAccountDto;
import com.ss.utopia.auth.entity.AccountAction;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.entity.UserRole;
import com.ss.utopia.auth.exception.DuplicateEmailException;
import com.ss.utopia.auth.exception.EmailNotSentException;
import com.ss.utopia.auth.exception.IllegalCustomerAccountDeletionException;
import com.ss.utopia.auth.exception.NoSuchUserAccountException;
import com.ss.utopia.auth.repository.UserAccountRepository;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserAccountServiceImpl implements UserAccountService {

  private final UserAccountRepository userAccountRepository;
  private final AuthenticationManager authenticationManager;
  private final BCryptPasswordEncoder passwordEncoder;
  private final AccountActionTokenService accountActionTokenService;
  private final EmailClient emailClient;
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Override
  public List<UserAccount> getAll() {
    return userAccountRepository.findAll()
        .stream()
        .peek(account -> account.setPassword(null))
        .collect(Collectors.toList());
  }

  @Override
  public UserAccount getById(UUID id) {
    return userAccountRepository.findById(id)
        .orElseThrow(() -> new NoSuchUserAccountException(id));
  }

  @Override
  public UserAccount getByEmail(String email) {
    return userAccountRepository.findByEmail(email)
        .orElseThrow(() -> new NoSuchUserAccountException(email));
  }

  @Override
  @Transactional(rollbackFor = EmailNotSentException.class)
  public UserAccount createNewAccount(CreateUserAccountDto createUserAccountDto) {
    validateDto(createUserAccountDto);

    userAccountRepository.findByEmail(createUserAccountDto.getEmail())
        .ifPresent(userAccount -> {
          throw new DuplicateEmailException(createUserAccountDto.getEmail());
        });

    var account = UserAccount.builder()
        .email(createUserAccountDto.getEmail())
        .password(passwordEncoder.encode(createUserAccountDto.getPassword()))
        .build();

    account = userAccountRepository.save(account);

    sendAccountConfirmation(account);

    return account;
  }

  @Override
  public void sendAccountConfirmation(UserAccount userAccount) {
    var confirmationToken = accountActionTokenService.createToken(userAccount.getId(),
                                                                  AccountAction.CONFIRMATION);

    emailClient.sendConfirmAccountEmail(userAccount.getEmail(), confirmationToken.getToken());
  }

  @Override
  public void confirmAccountRegistration(UUID confirmationTokenId) {
    var token = accountActionTokenService.getAndValidateToken(confirmationTokenId);

    var account = getById(token.getOwnerAccountId());
    account.setConfirmed(true);

    // don't modify non-customer accounts with confirmation
    if (isCustomerOrDefault(account)) {
      account.setUserRole(UserRole.CUSTOMER);
    }
    userAccountRepository.save(account);
  }

  @Override
  public void updateAccount(UserAccount userAccount) {
    userAccountRepository.findById(userAccount.getId())
        .ifPresent(a -> userAccountRepository.save(userAccount));
  }

  @Override
  public void deleteAccountById(UUID accountId) {
    var account = getById(accountId);
    userAccountRepository.delete(account);
  }

  @Override
  public UUID deleteAccountByEmail(String accountEmail) {
    var account = getByEmail(accountEmail);
    userAccountRepository.delete(account);
    return account.getId();
  }

  @Override
  public void initiateCustomerDeletion(DeleteAccountDto deleteAccountDto) {
    authenticate(deleteAccountDto);

    var account = getById(deleteAccountDto.getId());

    if (!isCustomerOrDefault(account)) {
      throw new IllegalCustomerAccountDeletionException(account);
    }

    var actionToken = accountActionTokenService.createToken(account.getId(),
                                                            AccountAction.DELETION);
    emailClient.sendDeleteAccountEmail(account.getEmail(), actionToken.getToken());
  }

  @Override
  public UUID completeCustomerDeletion(DeleteAccountDto deleteAccountDto) {
    authenticate(deleteAccountDto);

    var actionToken = accountActionTokenService.getAndValidateToken(deleteAccountDto.getId());
    var account = getByEmail(deleteAccountDto.getEmail());

    if (!isCustomerOrDefault(account)) {
      throw new IllegalCustomerAccountDeletionException(account);
    }

    deleteAccountByEmail(deleteAccountDto.getEmail());
    accountActionTokenService.deleteToken(actionToken);
    return account.getId();
  }

  private void authenticate(DeleteAccountDto deleteAccountDto) {
    var authToken = new UsernamePasswordAuthenticationToken(deleteAccountDto.getEmail(),
                                                            deleteAccountDto.getPassword(),
                                                            Collections.emptySet());
    authenticationManager.authenticate(authToken);
  }

  private boolean isCustomerOrDefault(UserAccount userAccount) {
    return userAccount.getUserRole().equals(UserRole.CUSTOMER)
        || userAccount.getUserRole().equals(UserRole.DEFAULT);
  }

  private void validateDto(CreateUserAccountDto dto) {
    var violations = validator.validate(dto);

    if (!violations.isEmpty()) {
      throw new IllegalArgumentException("Invalid DTO " + dto);
    }
  }
}