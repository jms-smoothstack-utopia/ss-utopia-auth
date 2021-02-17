package com.ss.utopia.auth.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.entity.UserRole;
import com.ss.utopia.auth.exception.DuplicateEmailException;
import com.ss.utopia.auth.repository.UserAccountRepository;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserAccountServiceImplTest {


  UserAccountRepository repository = Mockito.mock(UserAccountRepository.class);
  BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
  UserAccountService service = new UserAccountServiceImpl(repository, passwordEncoder);

  @BeforeEach
  void beforeEach() {
    Mockito.reset(repository);

  }

  @Test
  void test_createNewAccount_ReturnsCreatedAccountOnSuccess() {
    var now = ZonedDateTime.now();

    var expectedId = 1L;
    var email = "test@test.com";
    var unhashedPassword = "abCD1234!@";
    var hashedPassword = "ImFiQ0QxMjM0IUAi";

    when(passwordEncoder.encode(unhashedPassword))
        .thenReturn(hashedPassword);

    var accountWithId = UserAccount.builder()
        .id(expectedId)
        .email(email)
        .hashedPassword(hashedPassword)
        .creationDateTime(now)
        .userRole(UserRole.DEFAULT)
        .build();

    var accountWithoutId = UserAccount.builder()
        .email(email)
        .hashedPassword(hashedPassword)
        .creationDateTime(now)
        .userRole(UserRole.DEFAULT)
        .build();

    when(repository.save(accountWithoutId)).thenReturn(accountWithId);

    var dto = CreateUserAccountDto.builder()
        .email(email)
        .password(unhashedPassword)
        .build();

    var result = service.createNewAccount(dto);

    assertEquals(accountWithId, result);
  }

  @Test
  void test_createNewAccount_ThrowsDuplicateEmailExceptionOnDuplicateEmailAccount() {
    var email = "test@test.com";
    when(repository.findByEmail(anyString()))
        .thenReturn(Optional.of(UserAccount.builder().build()));

    var dto = CreateUserAccountDto.builder()
        .email(email)
        .password("abCD1234!@")
        .build();

    assertThrows(DuplicateEmailException.class, () -> service.createNewAccount(dto));

    try {
      service.createNewAccount(dto);
    } catch (DuplicateEmailException ex) {
      var emailInEx = ex.getEmail();
      assertEquals(email, emailInEx);
    }
  }

  @Test
  void test_createNewAccount_ThrowsIllegalArgumentExceptionOnInvalidDTO() {
    when(repository.findByEmail(anyString()))
        .thenReturn(Optional.empty());

    var validEmail = "test@test.com";
    var validPassword = "abCD1234!@";

    var dto = new CreateUserAccountDto();

    dto.setEmail(validEmail);
    dto.setPassword(validPassword);

    // sanity
    assertDoesNotThrow(() -> service.createNewAccount(dto));

    dto.setEmail(null);
    assertThrows(IllegalArgumentException.class, () -> service.createNewAccount(dto));

    dto.setEmail("");
    assertThrows(IllegalArgumentException.class, () -> service.createNewAccount(dto));

    dto.setEmail("Definitely not an email");
    assertThrows(IllegalArgumentException.class, () -> service.createNewAccount(dto));

    dto.setEmail(validEmail);
    dto.setPassword(null);
    assertThrows(IllegalArgumentException.class, () -> service.createNewAccount(dto));

    dto.setPassword("");
    assertThrows(IllegalArgumentException.class, () -> service.createNewAccount(dto));

    dto.setPassword("asdfasdfasdf");
    assertThrows(IllegalArgumentException.class, () -> service.createNewAccount(dto));

    assertThrows(IllegalArgumentException.class, () -> service.createNewAccount(null));
  }
}