package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.entity.UserRole;
import com.ss.utopia.auth.exception.DuplicateEmailException;
import com.ss.utopia.auth.repository.UserAccountRepository;
import java.time.ZonedDateTime;
import javax.validation.Validation;
import javax.validation.Validator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAccountServiceImpl implements UserAccountService {

  private final UserAccountRepository repository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  public UserAccountServiceImpl(UserAccountRepository repository,
                                BCryptPasswordEncoder passwordEncoder) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserAccount createNewAccount(CreateUserAccountDto createUserAccountDto) {
    validateDto(createUserAccountDto);

    repository.findByEmail(createUserAccountDto.getEmail())
        .ifPresent(userAccount -> {
          throw new DuplicateEmailException(createUserAccountDto.getEmail());
        });

    var account = UserAccount.builder()
        .email(createUserAccountDto.getEmail())
        .hashedPassword(passwordEncoder.encode(createUserAccountDto.getPassword()))
        .creationDateTime(ZonedDateTime.now())
        .isConfirmed(false)
        .userRole(UserRole.DEFAULT)
        .build();

    return repository.save(account);
  }

  private void validateDto(CreateUserAccountDto dto) {
    var violations = validator.validate(dto);

    if (!violations.isEmpty()) {
      throw new IllegalArgumentException("Invalid DTO " + dto);
    }
  }
}