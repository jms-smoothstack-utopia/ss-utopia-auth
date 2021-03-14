package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.dto.DeleteAccountDto;
import com.ss.utopia.auth.entity.UserAccount;
import java.util.List;
import java.util.UUID;

public interface UserAccountService {

  List<UserAccount> getAll();

  UserAccount getById(UUID id);

  UserAccount getByEmail(String email);

  UserAccount createNewAccount(CreateUserAccountDto createUserAccountDto);

  void updateAccount(UserAccount userAccount);

  void sendAccountConfirmation(UserAccount userAccount);

  void confirmAccountRegistration(UUID confirmationTokenId);

  void deleteAccountById(UUID accountId);

  void updateEmail(UUID accountId, String newEmail);

  void initiateCustomerDeletion(DeleteAccountDto deleteAccountDto);

  UUID completeCustomerDeletion(UUID confirmationToken);
}
