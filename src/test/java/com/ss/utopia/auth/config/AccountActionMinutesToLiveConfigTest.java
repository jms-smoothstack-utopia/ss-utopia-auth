package com.ss.utopia.auth.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ss.utopia.auth.entity.AccountAction;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountActionMinutesToLiveConfigTest {

  AccountActionMinutesToLiveConfig config = new AccountActionMinutesToLiveConfig();

  @BeforeEach
  void beforeEach() {
    config.setConfirmation("60");
    config.setPasswordReset("60");
    config.setDeletion("60");
  }

  @Test
  void test_getTimeToLive_ReturnsValueForAllPresentActions() {
    Arrays.stream(AccountAction.values())
        .forEach(action -> assertDoesNotThrow(() -> config.getMinutesToLive(action)));
  }

  @Test
  void test_setters_ThrowsExceptionOnInvalidValues() {
    assertThrows(IllegalArgumentException.class, () -> config.setPasswordReset("0"));
    assertThrows(IllegalArgumentException.class, () -> config.setPasswordReset("-1"));
    assertThrows(IllegalArgumentException.class, () -> config.setPasswordReset(""));
    assertThrows(IllegalArgumentException.class, () -> config.setPasswordReset("asdf"));

    assertThrows(IllegalArgumentException.class, () -> config.setConfirmation("0"));
    assertThrows(IllegalArgumentException.class, () -> config.setConfirmation("-1"));
    assertThrows(IllegalArgumentException.class, () -> config.setConfirmation(""));
    assertThrows(IllegalArgumentException.class, () -> config.setConfirmation("asdf"));

    assertThrows(IllegalArgumentException.class, () -> config.setDeletion("0"));
    assertThrows(IllegalArgumentException.class, () -> config.setDeletion("-1"));
    assertThrows(IllegalArgumentException.class, () -> config.setDeletion(""));
    assertThrows(IllegalArgumentException.class, () -> config.setDeletion("asdf"));
  }
}