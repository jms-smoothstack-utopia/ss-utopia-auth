package com.ss.utopia.auth.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PasswordResetServiceImplTest {

  @Autowired
  PasswordResetService passwordResetService;

  @Autowired
  UserAccountService userAccountService;

  @Autowired
  UserAccountRepository userAccountRepository;

  @Test
  void test_entireProcess_PasswordChanges() {
    var email = "test@test.com";
    userAccountRepository.findByEmail(email).ifPresent(userAccountRepository::delete);
    var account = userAccountService.createNewAccount(CreateUserAccountDto.builder()
        .email(email)
        .password("abCD1234!@")
        .build());
    var token = passwordResetService.addPasswordReset(new ResetPasswordDto(email));
    var resetDto = new NewPasswordDto(token.get("token"), "ABcd1234!@");
    assertDoesNotThrow(() -> passwordResetService.changePassword(resetDto));
    var updatedAccount = userAccountRepository.findByEmail(account.getEmail());
    assertTrue(updatedAccount.isPresent());
    assertNotEquals(account.getHashedPassword(), updatedAccount.get().getHashedPassword());
  }
}