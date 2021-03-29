package com.ss.utopia.auth.config;

import com.ss.utopia.auth.entity.AccountAction;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@Getter
@ConfigurationProperties(prefix = "com.ss.utopia.account.token.ttl.minutes",
    ignoreUnknownFields = false)
public class AccountActionMinutesToLiveConfig {

  private int passwordReset;
  private int confirmation;
  private int deletion;

  public void setPasswordReset(String passwordReset) {
    this.passwordReset = validateAndParse(passwordReset);
    log.info("Password reset token minutes to live = " + getPasswordReset());
  }

  public void setConfirmation(String confirmation) {
    this.confirmation = validateAndParse(confirmation);
    log.info("Confirmation token minutes to live = " + getConfirmation());
  }

  public void setDeletion(String deletion) {
    this.deletion = validateAndParse(deletion);
    log.info("Deletion token minutes to live = " + getDeletion());
  }

  public int getMinutesToLive(AccountAction action) {
    switch (action) {
      case PASSWORD_RESET:
        return passwordReset;
      case CONFIRMATION:
        return confirmation;
      case DELETION:
        return deletion;
      default:
        throw new NotImplementedException("No configuration setting for " + action.toString());
    }
  }

  private int validateAndParse(String configValue) {
    try {
      var minutesToLive = Integer.parseInt(configValue.trim()
                                               .replaceAll("_", ""));
      if (minutesToLive <= 0) {
        throw new IllegalArgumentException("Invalid value for minutes to live: " + minutesToLive);
      }
      return minutesToLive;
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(ex);
    }
  }
}
