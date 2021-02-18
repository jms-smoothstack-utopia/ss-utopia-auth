package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.entity.UserAccount;

public interface UserAccountService {

  UserAccount createNewAccount(CreateUserAccountDto createUserAccountDto);
}
