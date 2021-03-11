package com.ss.utopia.auth.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ss.utopia.auth.entity.AccountAction;
import com.ss.utopia.auth.entity.AccountActionToken;
import com.ss.utopia.auth.exception.InvalidTokenException;
import com.ss.utopia.auth.exception.NoSuchAccountActionToken;
import com.ss.utopia.auth.repository.AccountActionTokenRepository;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AccountActionTokenServiceImplTests {

  AccountActionTokenRepository accountActionTokenRepository =
      Mockito.mock(AccountActionTokenRepository.class);

  AccountActionTokenService service =
      new AccountActionTokenServiceImpl(accountActionTokenRepository);

  ZonedDateTime tokenCreation = ZonedDateTime.now();

  AccountActionToken mockValidConfirmationToken = AccountActionToken.builder()
      .token(UUID.randomUUID())
      .ownerAccountId(UUID.randomUUID())
      .active(true)
      .action(AccountAction.CONFIRMATION)
      .creation(tokenCreation)
      .build();

  AccountActionToken mockInactiveConfirmationToken = AccountActionToken.builder()
      .token(mockValidConfirmationToken.getToken())
      .ownerAccountId(UUID.randomUUID())
      .active(false)
      .action(AccountAction.CONFIRMATION)
      .creation(tokenCreation)
      .build();

  AccountActionToken mockExpiredConfirmationToken = AccountActionToken.builder()
      .token(mockValidConfirmationToken.getToken())
      .ownerAccountId(UUID.randomUUID())
      .active(false)
      .action(AccountAction.CONFIRMATION)
      .creation(tokenCreation.minusMinutes(AccountAction.CONFIRMATION.getMinutesToLive() + 1))
      .build();

  @BeforeEach
  void beforeEach() {
    Mockito.reset(accountActionTokenRepository);
  }

  @Test
  void test_getToken_ReturnsExpectedToken() {
    when(accountActionTokenRepository.findById(mockValidConfirmationToken.getToken()))
        .thenReturn(Optional.of(mockValidConfirmationToken));

    var result = service.getToken(mockValidConfirmationToken.getToken());

    assertEquals(mockValidConfirmationToken, result);
  }

  @Test
  void test_getToken_ThrowsNoSuchAccountActionTokenOnNotFound() {
    when(accountActionTokenRepository.findById(any()))
        .thenReturn(Optional.empty());

    assertThrows(NoSuchAccountActionToken.class,
                 () -> service.getToken(mockValidConfirmationToken.getToken()));
  }

  @Test
  void test_validateToken_DoesNotThrowExceptionOnValidToken() {
    assertDoesNotThrow(() -> service.validateToken(mockValidConfirmationToken));
  }

  @Test
  void test_validateToken_ThrowsInvalidTokenExceptionOnInactiveToken() {
    when(accountActionTokenRepository.findById(any()))
        .thenReturn(Optional.of(mockInactiveConfirmationToken));

    assertThrows(InvalidTokenException.class,
                 () -> service.validateToken(mockInactiveConfirmationToken));

    assertThrows(InvalidTokenException.class,
                 () -> service.getAndValidateToken(mockInactiveConfirmationToken.getToken()));
  }

  @Test
  void test_validateToken_ThrowsInvalidTokenExceptionOnExpiredToken() {
    when(accountActionTokenRepository.findById(any()))
        .thenReturn(Optional.of(mockExpiredConfirmationToken));

    assertThrows(InvalidTokenException.class,
                 () -> service.validateToken(mockExpiredConfirmationToken));

    assertThrows(InvalidTokenException.class,
                 () -> service.getAndValidateToken(mockExpiredConfirmationToken.getToken()));
  }
}
