package com.ss.utopia.auth.client;

import com.ss.utopia.auth.entity.UserAccount;
import org.springframework.stereotype.Service;


public interface EmailClient {

  void getAccountConfirmation(UserAccount account);
  void setAccountConfirmed(UserAccount account);
}
