package com.ss.utopia.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ss.utopia.auth.client.EmailClient;
import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.entity.PasswordReset;
import com.ss.utopia.auth.exception.EmailNotSentException;
import com.ss.utopia.auth.repository.PasswordResetRepository;
import com.ss.utopia.auth.repository.UserAccountRepository;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class PasswordResetServiceImplTest {

  @Autowired
  PasswordResetService passwordResetService;

  @Autowired
  UserAccountService userAccountService;

  @Autowired
  UserAccountRepository userAccountRepository;

  @Autowired
  PasswordResetRepository passwordResetRepository;

  @MockBean
  EmailClient emailClient;

  @Test
  void test_addPasswordReset_noUserWithEmail(){
    var email = "test@test.com";
    userAccountRepository.findByEmail(email).ifPresent(userAccountRepository::delete);
    var response = passwordResetService.addPasswordReset(new ResetPasswordDto(email));
    assertNull(response);
  }

  @Test
  void test_addPasswordReset_EmailWasSuccessfullySent() throws JSONException {

    JSONObject emailJson = new JSONObject();
    JSONObject responseData = new JSONObject();
    responseData.put("RequestId", "af2ce33a-a5a4-4032-99d5-29c9cdc7cbea");
    emailJson.put("ResponseMetadata", responseData);
    emailJson.put("MessageId", "01000177e55ebe42-5162830d-629e-49c7-861f-37d8b5f14eab-000000" );

    var templateMessage = new ResponseEntity<>(emailJson.toString(),HttpStatus.CREATED);
    var email = "test@test.com";

    //cannot actually send emails to customer; must mock response
    when(emailClient.sendForgetPasswordEmail(any(String.class), any(String.class))).thenReturn(templateMessage);

    userAccountRepository.findByEmail(email).ifPresent(userAccountRepository::delete);
    userAccountService.createNewAccount(CreateUserAccountDto.builder()
        .email(email)
        .password("abCD1234!@")
        .build());
    var response = passwordResetService.addPasswordReset(new ResetPasswordDto(email));
    var passwordResetEntry = passwordResetRepository.findByEmail(email);
    assertTrue(response.containsKey("token"));
    assertTrue(response.containsKey("emailBody"));
    assertTrue(passwordResetEntry.isPresent());
  }

  @Test
  void test_addPasswordReset_EmailWasNotSent() throws JSONException {

    JSONObject emailJson = new JSONObject();
    JSONObject responseData = new JSONObject();
    responseData.put("RequestId", "af2ce33a-a5a4-4032-99d5-29c9cdc7cbea");
    emailJson.put("ResponseMetadata", responseData);
    emailJson.put("errorType", "MessageRejected" );

    var templateMessage = new ResponseEntity<>(emailJson.toString(),HttpStatus.CREATED);
    var email = "test@test.com";

    //cannot actually send emails to customer; must mock response
    when(emailClient.sendForgetPasswordEmail(any(String.class), any(String.class))).thenReturn(templateMessage);

    userAccountRepository.findByEmail(email).ifPresent(userAccountRepository::delete);
    passwordResetRepository.findByEmail(email).ifPresent(passwordResetRepository::delete);
    userAccountService.createNewAccount(CreateUserAccountDto.builder()
        .email(email)
        .password("abCD1234!@")
        .build());
    assertThrows(EmailNotSentException.class, () -> passwordResetService.addPasswordReset(new ResetPasswordDto(email)));
    var response = passwordResetRepository.findByEmail(email);
    assertTrue(response.isEmpty());
  }

  @Test
  void test_tokenCheck_EntryPreset(){
    passwordResetRepository.findByEmail("test@test.com").ifPresent(passwordResetRepository::delete);
    var token = UUID.randomUUID().toString();
    passwordResetRepository.save(PasswordReset.builder()
        .email("test@test.com")
        .userId(UUID.randomUUID())
        .token(token)
        .timestamp(new Timestamp(new Date().getTime()))
        .build());
    boolean response = passwordResetService.tokenCheck(token);
    assertTrue(response);
  }

  @Test
  void test_tokenCheck_EntryNotPreset(){
    passwordResetRepository.findByEmail("test@test.com").ifPresent(passwordResetRepository::delete);
    var token = UUID.randomUUID().toString();
    passwordResetRepository.save(PasswordReset.builder()
        .email("test@test.com")
        .userId(UUID.randomUUID())
        .token(token)
        .timestamp(new Timestamp(new Date().getTime()))
        .build());
    boolean response = passwordResetService.tokenCheck(UUID.randomUUID().toString());
    assertFalse(response);
  }

  @Test
  void test_changePassword_TokenNotValid(){
    passwordResetRepository.findByEmail("test@test.com").ifPresent(passwordResetRepository::delete);
    var token = UUID.randomUUID().toString();
    passwordResetRepository.save(PasswordReset.builder()
        .email("test@test.com")
        .userId(UUID.randomUUID())
        .token(token)
        .timestamp(new Timestamp(new Date().getTime()))
        .build());
    NewPasswordDto newPasswordDto = NewPasswordDto.builder().token(UUID.randomUUID().toString()).password("Qwerty123456!").build();
    Map<String, String> response = passwordResetService.changePassword(newPasswordDto);
    assertNull(response);
  }

  @Test
  void test_changePassword_TokenValid(){

    //customer email
    var email = "test@test.com";

    //delete things in passwordResetRepo and userAccountRepo
    passwordResetRepository.findByEmail(email).ifPresent(passwordResetRepository::delete);
    userAccountRepository.findByEmail(email).ifPresent(userAccountRepository::delete);
    userAccountService.createNewAccount(CreateUserAccountDto.builder().email(email).password("abCD1234!@").build());
    var oldAccount = userAccountRepository.findByEmail(email).get();
    var customerID = oldAccount.getId();

    //generate token then save in passwordResetRepo
    var token = UUID.randomUUID().toString();
    passwordResetRepository.save(PasswordReset.builder()
        .email(email)
        .userId(customerID)
        .token(token)
        .timestamp(new Timestamp(new Date().getTime()))
        .build());

    //Setup object to send passwordResetService
    NewPasswordDto newPasswordDto = NewPasswordDto.builder().token(token).password("Qwerty123456!").build();
    passwordResetService.changePassword(newPasswordDto);
    var newAccount = userAccountRepository.findByEmail(email).get();
    assertEquals(oldAccount.getPassword(), newAccount.getPassword());
  }

  @Test
  void test_changePassword_NotActiveToken(){

    //customer email
    var email = "test@test.com";

    //delete things in passwordResetRepo and userAccountRepo
    passwordResetRepository.findByEmail(email).ifPresent(passwordResetRepository::delete);
    userAccountRepository.findByEmail(email).ifPresent(userAccountRepository::delete);
    userAccountService.createNewAccount(CreateUserAccountDto.builder().email(email).password("abCD1234!@").build());
    var oldAccount = userAccountRepository.findByEmail(email).get();
    var customerID = oldAccount.getId();

    //generate token then save in passwordResetRepo
    var token = UUID.randomUUID().toString();
    passwordResetRepository.save(PasswordReset.builder()
        .email(email)
        .userId(customerID)
        .isActive(false)
        .token(token)
        .timestamp(new Timestamp(new Date().getTime()))
        .build());

    //Setup object to send passwordResetService
    NewPasswordDto newPasswordDto = NewPasswordDto.builder().token(token).password("Qwerty123456!").build();
    var response = passwordResetService.changePassword(newPasswordDto);
    assertNull(response);
  }
}