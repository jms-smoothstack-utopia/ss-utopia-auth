package com.ss.utopia.auth.service;

import com.ss.utopia.auth.client.EmailClient;
import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.entity.AccountAction;
import com.ss.utopia.auth.entity.AccountActionToken;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.exception.DuplicateEmailException;
import com.ss.utopia.auth.exception.EmailNotSentException;
import com.ss.utopia.auth.exception.NoSuchAccountActionToken;
import com.ss.utopia.auth.exception.NoSuchUserAccountException;
import com.ss.utopia.auth.repository.AccountActionTokenRepository;
import com.ss.utopia.auth.repository.UserAccountRepository;
import java.util.UUID;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserAccountServiceImpl implements UserAccountService {

  private final UserAccountRepository userAccountRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final AccountActionTokenRepository accountActionTokenRepository;
  private final EmailClient emailClient;
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

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
    var confirmationToken = AccountActionToken.builder()
        .ownerAccountId(userAccount.getId())
        .action(AccountAction.CONFIRMATION)
        .build();

    confirmationToken = accountActionTokenRepository.save(confirmationToken);

    emailClient.sendConfirmAccountEmail(userAccount.getEmail(), confirmationToken.getToken());
  }

  @Override
  public void confirmAccountRegistration(UUID confirmationTokenId) {
    accountActionTokenRepository.findById(confirmationTokenId)
        .ifPresentOrElse(token -> {
          var account = getById(token.getOwnerAccountId());
          account.setConfirmed(true);
          userAccountRepository.save(account);
        }, () -> {
          throw new NoSuchAccountActionToken(confirmationTokenId);
        });
  }

  @Override
  public void updateAccount(UserAccount userAccount) {
    userAccountRepository.findById(userAccount.getId())
        .ifPresent(a -> userAccountRepository.save(userAccount));
  }

  private void validateDto(CreateUserAccountDto dto) {
    var violations = validator.validate(dto);

    if (!violations.isEmpty()) {
      throw new IllegalArgumentException("Invalid DTO " + dto);
    }
  }
}