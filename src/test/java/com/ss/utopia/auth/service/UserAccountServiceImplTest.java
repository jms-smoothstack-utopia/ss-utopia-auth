package com.ss.utopia.auth.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.ss.utopia.auth.client.EmailClient;
import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.dto.DeleteAccountDto;
import com.ss.utopia.auth.entity.AccountAction;
import com.ss.utopia.auth.entity.AccountActionToken;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.entity.UserRole;
import com.ss.utopia.auth.exception.DuplicateEmailException;
import com.ss.utopia.auth.exception.IllegalCustomerAccountDeletionException;
import com.ss.utopia.auth.exception.NoSuchAccountActionToken;
import com.ss.utopia.auth.exception.NoSuchUserAccountException;
import com.ss.utopia.auth.repository.UserAccountRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserAccountServiceImplTest {


  final UUID accountId = UUID.randomUUID();
  final ZonedDateTime now = ZonedDateTime.now();
  final String validEmail = "test@test.com";
  final String validUnhashedPassword = "abCD1234!@";
  final String validHashedPassword = "ImFiQ0QxMjM0IUAi";

  final UserAccount mockDefaultAccountWithoutId = UserAccount.builder()
      .email(validEmail)
      .password(validHashedPassword)
      .userRole(UserRole.DEFAULT)
      .build();

  final UserAccount mockDefaultAccountWithId = UserAccount.builder()
      .id(accountId)
      .email(validEmail)
      .password(validHashedPassword)
      .userRole(UserRole.DEFAULT)
      .build();

  final UserAccount mockAdminAccount = UserAccount.builder()
      .id(UUID.randomUUID())
      .email("admin@test.com")
      .password(validHashedPassword)
      .userRole(UserRole.ADMIN)
      .build();

  final UserAccount mockServiceAccount = UserAccount.builder()
      .id(UUID.randomUUID())
      .email("service@test.com")
      .password(validHashedPassword)
      .userRole(UserRole.SERVICE)
      .build();

  final UserAccount mockEmployeeAccount = UserAccount.builder()
      .id(UUID.randomUUID())
      .email("employee@test.com")
      .password(validHashedPassword)
      .userRole(UserRole.EMPLOYEE)
      .build();

  final UserAccount mockTravelAgentAccount = UserAccount.builder()
      .id(UUID.randomUUID())
      .email("travel_agent@test.com")
      .password(validHashedPassword)
      .userRole(UserRole.TRAVEL_AGENT)
      .build();

  final UserAccount mockCustomerAccount = UserAccount.builder()
      .id(UUID.randomUUID())
      .email("customer@test.com")
      .password(validHashedPassword)
      .userRole(UserRole.CUSTOMER)
      .build();

  final List<UserAccount> elevatedUserList = List.of(mockAdminAccount,
                                                     mockServiceAccount,
                                                     mockEmployeeAccount,
                                                     mockTravelAgentAccount);

  final List<UserAccount> customerLevelAccountList = List.of(mockDefaultAccountWithId,
                                                             mockCustomerAccount);

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
  AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
  BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
  AccountActionTokenService accountActionTokenService = Mockito.mock(AccountActionTokenService.class);
  EmailClient emailClient = Mockito.mock(EmailClient.class);

  UserAccountService service = new UserAccountServiceImpl(userAccountRepository,
                                                          authenticationManager,
                                                          passwordEncoder,
                                                          accountActionTokenService,
                                                          emailClient);

  @BeforeEach
  void beforeEach() {
    Mockito.reset(userAccountRepository);
    when(passwordEncoder.encode(validUnhashedPassword))
        .thenReturn(validHashedPassword);
  }

  @Test
  void test_getAll_ReturnsListWithPasswordsAsNull() {
    when(userAccountRepository.findAll()).thenReturn(List.of(mockDefaultAccountWithId,
                                                             mockDefaultAccountWithoutId));

    service.getAll()
        .forEach(account -> assertNull(account.getPassword()));
  }

  @Test
  void test_createNewAccount_ReturnsCreatedAccountOnSuccess() {
    when(userAccountRepository.save(mockDefaultAccountWithoutId)).thenReturn(
        mockDefaultAccountWithId);

    when(accountActionTokenService.createToken(mockConfirmationToken.getOwnerAccountId(),
                                               mockConfirmationToken.getAction()))
        .thenReturn(persistedConfirmationToken);

    var dto = CreateUserAccountDto.builder()
        .email(validEmail)
        .password(validUnhashedPassword)
        .build();

    var result = service.createNewAccount(dto);

    assertEquals(mockDefaultAccountWithId, result);
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
                 () -> service.createNewAccount(dto));

    try {
      service.createNewAccount(dto);
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
                 () -> service.createNewAccount(dto));

    dto.setEmail("");
    assertThrows(IllegalArgumentException.class,
                 () -> service.createNewAccount(dto));

    dto.setEmail("Definitely not an email");
    assertThrows(IllegalArgumentException.class,
                 () -> service.createNewAccount(dto));

    dto.setEmail(validEmail);
    dto.setPassword(null);
    assertThrows(IllegalArgumentException.class,
                 () -> service.createNewAccount(dto));

    dto.setPassword("");
    assertThrows(IllegalArgumentException.class,
                 () -> service.createNewAccount(dto));

    dto.setPassword("asdfasdfasdf");
    assertThrows(IllegalArgumentException.class,
                 () -> service.createNewAccount(dto));

    assertThrows(IllegalArgumentException.class,
                 () -> service.createNewAccount(null));
  }

  @Test
  void test_confirmAccountRegistration_DoesNotThrowOnValidUUID() {
    when(accountActionTokenService.getAndValidateToken(any(UUID.class)))
        .thenReturn(persistedConfirmationToken);

    when(userAccountRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(mockDefaultAccountWithId));

    assertDoesNotThrow(() -> service.confirmAccountRegistration(UUID.randomUUID()));
  }

  @Test
  void test_confirmAccountRegistration_ThrowsNoSuchAccountTokenExceptionOnTokenNotFound() {
    when(accountActionTokenService.getAndValidateToken(any(UUID.class)))
        .thenThrow(new NoSuchAccountActionToken(null));

    assertThrows(NoSuchAccountActionToken.class,
                 () -> service.confirmAccountRegistration(UUID.randomUUID()));
  }

  @Test
  void test_confirmAccountRegistration_ThrowsNoSuchUserAccountExceptionOnUserNotFound() {
    when(accountActionTokenService.getAndValidateToken(any(UUID.class)))
        .thenReturn(persistedConfirmationToken);

    when(userAccountRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    assertThrows(NoSuchUserAccountException.class,
                 () -> service.confirmAccountRegistration(UUID.randomUUID()));
  }

  @Test
  void test_initiateCustomerDeletion_DoesNotThrowExceptionOnCustomerLevelUser() {
    when(userAccountRepository.findById(mockCustomerAccount.getId()))
        .thenReturn(Optional.of(mockCustomerAccount));
    when(userAccountRepository.findById(mockDefaultAccountWithId.getId()))
        .thenReturn(Optional.of(mockDefaultAccountWithId));

    when(accountActionTokenService.createToken(any(), any()))
        .thenReturn(AccountActionToken.builder().token(UUID.randomUUID()).build());

    customerLevelAccountList
        .forEach(account ->
                     assertDoesNotThrow(() -> service
                         .initiateCustomerDeletion(DeleteAccountDto.builder()
                                                       .id(account.getId())
                                                       .email(account.getEmail())
                                                       .password(validUnhashedPassword)
                                                       .build())));
  }

  @Test
  void test_initiateCustomerDeletion_ThrowsExceptionOnAttemptToDeleteElevatedUser() {
    when(userAccountRepository.findById(mockAdminAccount.getId()))
        .thenReturn(Optional.of(mockAdminAccount));
    when(userAccountRepository.findById(mockServiceAccount.getId()))
        .thenReturn(Optional.of(mockServiceAccount));
    when(userAccountRepository.findById(mockEmployeeAccount.getId()))
        .thenReturn(Optional.of(mockEmployeeAccount));
    when(userAccountRepository.findById(mockTravelAgentAccount.getId()))
        .thenReturn(Optional.of(mockTravelAgentAccount));

    elevatedUserList
        .forEach(account ->
                     assertThrows(IllegalCustomerAccountDeletionException.class,
                                  () -> service
                                      .initiateCustomerDeletion(DeleteAccountDto.builder()
                                                                    .id(account.getId())
                                                                    .email(account.getEmail())
                                                                    .password(validUnhashedPassword)
                                                                    .build())));
  }

  @Test
  void test_completeCustomerDeletion_DoesNotThrowExceptionOnCustomerLevelUser() {
    var confirmationToken = UUID.randomUUID();

    Function<UserAccount, Void> test = user -> {
      when(userAccountRepository.findById(user.getId()))
          .thenReturn(Optional.of(user));

      when(accountActionTokenService.getAndValidateToken(any()))
          .thenReturn(AccountActionToken.builder()
                          .ownerAccountId(user.getId())
                          .token(confirmationToken)
                          .build());

      assertDoesNotThrow(() -> service.completeCustomerDeletion(confirmationToken));
      return null;
    };

    customerLevelAccountList.forEach(test::apply);
  }

  @Test
  void test_completeCustomerDeletion_ThrowsExceptionOnAttemptToDeleteElevatedUser() {
    var confirmationToken = UUID.randomUUID();

    Function<UserAccount, Void> test = user -> {
      when(userAccountRepository.findById(user.getId()))
          .thenReturn(Optional.of(user));

      when(accountActionTokenService.getAndValidateToken(any()))
          .thenReturn(AccountActionToken.builder()
                          .ownerAccountId(user.getId())
                          .token(confirmationToken)
                          .build());

      assertThrows(IllegalCustomerAccountDeletionException.class,
                   () -> service.completeCustomerDeletion(confirmationToken));
      return null;
    };

    elevatedUserList.forEach(test::apply);
  }

  @Test
  void test_initiateCustomerDeletion_ThrowsAuthenticationException() {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new AuthenticationCredentialsNotFoundException(""));

    assertThrows(AuthenticationException.class,
                 () -> service.initiateCustomerDeletion(DeleteAccountDto.builder().build()));
  }
}