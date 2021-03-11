package com.ss.utopia.auth.service;

import com.ss.utopia.auth.entity.AccountAction;
import com.ss.utopia.auth.entity.AccountActionToken;
import com.ss.utopia.auth.exception.InvalidTokenException;
import java.util.UUID;

public interface AccountActionTokenService {

  AccountActionToken getToken(UUID token);

  default AccountActionToken getAndValidateToken(UUID token) {
    var retrieved = getToken(token);
    validateToken(retrieved);
    return retrieved;
  }

  void validateToken(AccountActionToken token) throws InvalidTokenException;

  AccountActionToken createToken(UUID ownerId, AccountAction action);

  void deleteToken(AccountActionToken token);

  void deleteToken(UUID tokenId);
}
