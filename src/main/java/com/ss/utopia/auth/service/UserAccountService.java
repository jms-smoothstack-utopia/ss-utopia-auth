package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.CreateUserAccountDto;

public interface UserAccountService {

  Long create(CreateUserAccountDto createUserAccountDto);
}
