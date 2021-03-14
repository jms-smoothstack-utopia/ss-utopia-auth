package com.ss.utopia.auth.exception;

import com.ss.utopia.auth.entity.UserAccount;
import java.util.Optional;

public class IllegalAccountModificationException extends IllegalStateException {

  private final UserAccount userAccount;

  public IllegalAccountModificationException(UserAccount account) {
    super("Unable to modify account, id=" + account.getId() + " role=" + account.getUserRole());
    this.userAccount = account;
  }

  public Optional<UserAccount> getUserAccount() {
    return Optional.ofNullable(userAccount);
  }
}
