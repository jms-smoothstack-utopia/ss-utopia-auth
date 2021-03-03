package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.entity.UserAccount;
import java.util.UUID;

public interface UserAccountService {

  UserAccount getById(UUID id);

  UserAccount getByEmail(String email);

  UserAccount createNewAccount(CreateUserAccountDto createUserAccountDto);

  void updateAccount(UserAccount userAccount);

  void sendAccountConfirmation(UserAccount userAccount);

  void confirmAccountRegistration(UUID confirmationTokenId);
}
