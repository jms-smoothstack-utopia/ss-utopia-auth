package com.ss.utopia.auth.exception;

import com.ss.utopia.auth.entity.UserAccount;
import lombok.Getter;

public class IllegalCustomerAccountDeletionException extends IllegalArgumentException {

  @Getter
  private final UserAccount userAccount;

  public IllegalCustomerAccountDeletionException(UserAccount userAccount) {
    super("Unable to initiate deletion, account does not belong to a customer. AccountId="
              + userAccount.getId());
    this.userAccount = userAccount;
  }
}
