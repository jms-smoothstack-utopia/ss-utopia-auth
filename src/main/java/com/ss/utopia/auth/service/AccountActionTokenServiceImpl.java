package com.ss.utopia.auth.service;

import com.ss.utopia.auth.config.AccountActionMinutesToLiveConfig;
import com.ss.utopia.auth.entity.AccountAction;
import com.ss.utopia.auth.entity.AccountActionToken;
import com.ss.utopia.auth.exception.InvalidTokenException;
import com.ss.utopia.auth.exception.NoSuchAccountActionToken;
import com.ss.utopia.auth.repository.AccountActionTokenRepository;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountActionTokenServiceImpl implements AccountActionTokenService {

  private final AccountActionTokenRepository accountActionTokenRepository;
  private final AccountActionMinutesToLiveConfig ttlConfig;

  @Override
  public AccountActionToken getToken(UUID token) {
    log.debug("Get token=" + token);
    return accountActionTokenRepository.findById(token)
        .orElseThrow(() -> new NoSuchAccountActionToken(token));
  }

  /**
   * Validates that a token is neither inactive nor expired.
   *
   * @param token a token to validate.
   * @throws InvalidTokenException if the given token is expired or inactive.
   */
  @Override
  public void validateToken(AccountActionToken token) {
    var now = ZonedDateTime.now();
    var creation = token.getCreation();
    var expiration = creation.plusMinutes(ttlConfig.getMinutesToLive(token.getAction()));

    if (!token.isActive() || now.isAfter(expiration)) {
      throw new InvalidTokenException(token);
    }
  }

  @Override
  public AccountActionToken createToken(UUID ownerId, AccountAction action) {
    log.debug("Create token, owner=" + ownerId + " action=" + action);
    return accountActionTokenRepository.save(AccountActionToken.builder()
                                                 .ownerAccountId(ownerId)
                                                 .action(action)
                                                 .build());
  }

  @Override
  public void deleteToken(AccountActionToken token) {
    deleteToken(token.getToken());
  }

  @Override
  public void deleteToken(UUID tokenId) {
    log.debug("Delete token=" + tokenId);
    accountActionTokenRepository.findById(tokenId)
        .ifPresent(accountActionTokenRepository::delete);
  }
}
