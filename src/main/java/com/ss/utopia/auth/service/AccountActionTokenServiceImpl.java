package com.ss.utopia.auth.service;

import com.ss.utopia.auth.entity.AccountAction;
import com.ss.utopia.auth.entity.AccountActionToken;
import com.ss.utopia.auth.exception.InvalidTokenException;
import com.ss.utopia.auth.exception.NoSuchAccountActionToken;
import com.ss.utopia.auth.repository.AccountActionTokenRepository;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountActionTokenServiceImpl implements AccountActionTokenService {

  private final AccountActionTokenRepository accountActionTokenRepository;

  @Override
  public AccountActionToken getToken(UUID token) {
    return accountActionTokenRepository.findById(token)
        .orElseThrow(() -> new NoSuchAccountActionToken(token));
  }

  @Override
  public void validateToken(AccountActionToken token) throws InvalidTokenException {
    var now = ZonedDateTime.now();
    var creation = token.getCreation();
    var expiration = creation.plusMinutes(token.getAction().getMinutesToLive());

    if (!token.isActive() || now.isAfter(expiration)) {
      throw new InvalidTokenException(token);
    }
  }

  @Override
  public AccountActionToken createToken(UUID ownerId, AccountAction action) {
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
    accountActionTokenRepository.findById(tokenId)
        .ifPresent(accountActionTokenRepository::delete);
  }
}
