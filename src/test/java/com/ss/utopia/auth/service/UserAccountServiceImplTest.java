package com.ss.utopia.auth.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.ss.utopia.auth.client.EmailClient;
import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.entity.AccountAction;
import com.ss.utopia.auth.entity.AccountActionToken;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.entity.UserRole;
import com.ss.utopia.auth.exception.DuplicateEmailException;
import com.ss.utopia.auth.exception.NoSuchAccountActionToken;
import com.ss.utopia.auth.exception.NoSuchUserAccountException;
import com.ss.utopia.auth.repository.AccountActionTokenRepository;
import com.ss.utopia.auth.repository.UserAccountRepository;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserAccountServiceImplTest {


  final UUID accountId = UUID.randomUUID();
  final ZonedDateTime now = ZonedDateTime.now();
  final String validEmail = "test@test.com";
  final String validUnhashedPassword = "abCD1234!@";
  final String validHashedPassword = "ImFiQ0QxMjM0IUAi";

  final UserAccount accountWithoutId = UserAccount.builder()
      .email(validEmail)
      .password(validHashedPassword)
      .userRole(UserRole.DEFAULT)
      .build();

  final UserAccount accountWithId = UserAccount.builder()
      .id(accountId)
      .email(validEmail)
      .password(validHashedPassword)
      .userRole(UserRole.DEFAULT)
      .build();

  final AccountActionToken mockConfirmationToken = AccountActionToken.builder()
      .ownerAccountId(accountId)
      .action(AccountAction.CONFIRMATION)
      .build();

  final AccountActionToken persistedConfirmationToken = AccountActionToken.builder()
      .token(UUID.randomUUID())
      .active(true)
      .ownerAccountId(accountId)
      .action(AccountAction.CONFIRMATION)
      .creation(now)
      .build();

  UserAccountRepository userAccountRepository = Mockito.mock(UserAccountRepository.class);
  BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
  AccountActionTokenRepository accountActionTokenRepository = Mockito.mock(
      AccountActionTokenRepository.class);
  EmailClient emailClient = Mockito.mock(EmailClient.class);
  UserAccountService serviceThatIsBeingTested = new UserAccountServiceImpl(userAccountRepository,
                                                                           passwordEncoder,
                                                                           accountActionTokenRepository,
                                                                           emailClient);

  @BeforeEach
  void beforeEach() {
    Mockito.reset(userAccountRepository);
    when(passwordEncoder.encode(validUnhashedPassword))
        .thenReturn(validHashedPassword);
  }

  @Test
  void test_createNewAccount_ReturnsCreatedAccountOnSuccess() {
    when(userAccountRepository.save(accountWithoutId)).thenReturn(accountWithId);

    when(accountActionTokenRepository.save(mockConfirmationToken))
        .thenReturn(persistedConfirmationToken);

    var dto = CreateUserAccountDto.builder()
        .email(validEmail)
        .password(validUnhashedPassword)
        .build();

    var result = serviceThatIsBeingTested.createNewAccount(dto);

    assertEquals(accountWithId, result);
  }

  @Test
  void test_createNewAccount_ThrowsDuplicateEmailExceptionOnDuplicateEmailAccount() {
    var email = "test@test.com";
    when(userAccountRepository.findByEmail(anyString()))
        .thenReturn(Optional.of(UserAccount.builder().build()));

    var dto = CreateUserAccountDto.builder()
        .email(email)
        .password("abCD1234!@")
        .build();

    assertThrows(DuplicateEmailException.class,
                 () -> serviceThatIsBeingTested.createNewAccount(dto));

    try {
      serviceThatIsBeingTested.createNewAccount(dto);
    } catch (DuplicateEmailException ex) {
      var emailInEx = ex.getEmail();
      assertEquals(email, emailInEx);
    }
  }

  @Test
  void test_createNewAccount_ThrowsIllegalArgumentExceptionOnInvalidDTO() {
    when(userAccountRepository.findByEmail(anyString()))
        .thenReturn(Optional.empty());

    var validEmail = "test@test.com";
    var validPassword = "abCD1234!@";

    var dto = new CreateUserAccountDto();

    dto.setEmail(validEmail);
    dto.setPassword(validPassword);

    dto.setEmail(null);
    assertThrows(IllegalArgumentException.class,
                 () -> serviceThatIsBeingTested.createNewAccount(dto));

    dto.setEmail("");
    assertThrows(IllegalArgumentException.class,
                 () -> serviceThatIsBeingTested.createNewAccount(dto));

    dto.setEmail("Definitely not an email");
    assertThrows(IllegalArgumentException.class,
                 () -> serviceThatIsBeingTested.createNewAccount(dto));

    dto.setEmail(validEmail);
    dto.setPassword(null);
    assertThrows(IllegalArgumentException.class,
                 () -> serviceThatIsBeingTested.createNewAccount(dto));

    dto.setPassword("");
    assertThrows(IllegalArgumentException.class,
                 () -> serviceThatIsBeingTested.createNewAccount(dto));

    dto.setPassword("asdfasdfasdf");
    assertThrows(IllegalArgumentException.class,
                 () -> serviceThatIsBeingTested.createNewAccount(dto));

    assertThrows(IllegalArgumentException.class,
                 () -> serviceThatIsBeingTested.createNewAccount(null));
  }

  @Test
  void test_confirmAccountRegistration_DoesNotThrowOnValidUUID() {
    when(accountActionTokenRepository.findById(any(UUID.class)))
        .thenReturn(Optional.ofNullable(persistedConfirmationToken));

    when(userAccountRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(accountWithId));

    assertDoesNotThrow(() -> serviceThatIsBeingTested.confirmAccountRegistration(UUID.randomUUID()));
  }

  @Test
  void test_confirmAccountRegistration_ThrowsNoSuchAccountTokenExceptionOnTokenNotFound() {
    when(accountActionTokenRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    assertThrows(NoSuchAccountActionToken.class,
                 () -> serviceThatIsBeingTested.confirmAccountRegistration(UUID.randomUUID()));
  }

  @Test
  void test_confirmAccountRegistration_ThrowsNoSuchUserAccountExceptionOnUserNotFound() {
    when(accountActionTokenRepository.findById(any(UUID.class)))
        .thenReturn(Optional.ofNullable(persistedConfirmationToken));

    when(userAccountRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    assertThrows(NoSuchUserAccountException.class,
                 () -> serviceThatIsBeingTested.confirmAccountRegistration(UUID.randomUUID()));
  }
}