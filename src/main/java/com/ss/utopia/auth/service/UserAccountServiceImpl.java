package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.exception.DuplicateEmailException;
import com.ss.utopia.auth.repository.UserAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAccountServiceImpl implements UserAccountService {

  private final UserAccountRepository repository;
  private final BCryptPasswordEncoder passwordEncoder;

  public UserAccountServiceImpl(UserAccountRepository repository,
                                BCryptPasswordEncoder passwordEncoder) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Long create(CreateUserAccountDto createUserAccountDto) {
    repository.findByEmail(createUserAccountDto.getEmail())
        .ifPresent(userAccount -> {
          throw new DuplicateEmailException(createUserAccountDto.getEmail());
        });

    var account = UserAccount.builder()
        .email(createUserAccountDto.getEmail())
        .hashedPassword(passwordEncoder.encode(createUserAccountDto.getPassword()))
        .build();

    var createdAccount = repository.save(account);
    return createdAccount.getId();
  }
}
