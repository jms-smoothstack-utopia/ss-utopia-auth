package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.exception.DuplicateEmailException;
import com.ss.utopia.auth.repository.UserAccountRepository;
import javax.validation.Validation;
import javax.validation.Validator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAccountServiceImpl implements UserAccountService {

  private final UserAccountRepository userAccountRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  public UserAccountServiceImpl(UserAccountRepository userAccountRepository,
                                BCryptPasswordEncoder passwordEncoder) {
    this.userAccountRepository = userAccountRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserAccount createNewAccount(CreateUserAccountDto createUserAccountDto) {
    validateDto(createUserAccountDto);

    userAccountRepository.findByEmail(createUserAccountDto.getEmail())
        .ifPresent(userAccount -> {
          throw new DuplicateEmailException(createUserAccountDto.getEmail());
        });

    var account = UserAccount.builder()
        .email(createUserAccountDto.getEmail())
        .hashedPassword(passwordEncoder.encode(createUserAccountDto.getPassword()))
        .build();

    return userAccountRepository.save(account);
  }

  private void validateDto(CreateUserAccountDto dto) {
    var violations = validator.validate(dto);

    if (!violations.isEmpty()) {
      throw new IllegalArgumentException("Invalid DTO " + dto);
    }
  }
}