package com.ss.utopia.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.entity.AccountActionToken;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.exception.DuplicateEmailException;
import com.ss.utopia.auth.exception.EmailNotSentException;
import com.ss.utopia.auth.exception.InvalidTokenException;
import com.ss.utopia.auth.exception.NoSuchAccountActionToken;
import com.ss.utopia.auth.exception.NoSuchUserAccountException;
import com.ss.utopia.auth.repository.UserAccountRepository;
import com.ss.utopia.auth.security.SecurityConstants;
import com.ss.utopia.auth.service.AccountActionTokenService;
import com.ss.utopia.auth.service.UserAccountService;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(UserAccountController.class)
class UserAccountControllerTest {

  ObjectMapper jsonMapper = new ObjectMapper();

  @Autowired
  WebApplicationContext wac;

  @MockBean
  UserAccountRepository repository;
  @MockBean
  UserAccountService userAccountService;

  @MockBean
  AccountActionTokenService accountActionTokenService;

  @MockBean
  SecurityConstants securityConstants;

  private MockMvc mvc;

  @BeforeEach
  void beforeEach() {
    when(securityConstants.getEndpoint()).thenReturn("/login");
    when(securityConstants.getJwtSecret()).thenReturn("superSecret");
    when(securityConstants.getJwtHeaderName()).thenReturn("Authorization");
    when(securityConstants.getJwtHeaderPrefix()).thenReturn("Bearer ");
    when(securityConstants.getJwtIssuer()).thenReturn("ss-utopia");
    when(securityConstants.getJwtExpirationDuration()).thenReturn(100L);
    when(securityConstants.getAuthorityClaimKey()).thenReturn("Authorities");
    when(securityConstants.getUserIdClaimKey()).thenReturn("userId");
    when(securityConstants.getExpiresAt()).thenReturn(new Date());

    mvc = MockMvcBuilders
        .webAppContextSetup(wac)
        .apply(springSecurity())
        .build();
  }

  @Test
  void test_createAccount_StatusIsCreatedAndHeaderContainsLocation() throws Exception {
    var createDto = CreateUserAccountDto.builder()
        .email("test1@test.com")
        .password("abCD1234!@")
        .build();
    var jsonDto = jsonMapper.writeValueAsString(createDto);

    var uuid = UUID.randomUUID();

    var headerName = "Location";
    var expectedHeaderVal = EndpointConstants.API_V_0_1_ACCOUNTS + "/" + uuid;

    when(userAccountService.createNewAccount(createDto))
        .thenReturn(UserAccount.builder().id(uuid).build());

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonDto))
        .andExpect(status().isCreated())
        .andExpect(header().string(headerName, expectedHeaderVal));
  }

  @Test
  void test_createAccount_OnDuplicateEmailStatusIsConflict() throws Exception {
    var createDto = CreateUserAccountDto.builder()
        .email("test2@test.com")
        .password("abCD1234!@")
        .build();

    when(userAccountService.createNewAccount(createDto))
        .thenThrow(new DuplicateEmailException("test2@test.com"));

    var jsonDto = jsonMapper.writeValueAsString(createDto);

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonDto))
        .andExpect(status().isConflict());
  }

  @Test
  void test_confirmAccountRegistration_ReturnsNoContent() throws Exception {
    var uuid = UUID.randomUUID();

    mvc.perform(
        put(EndpointConstants.API_V_0_1_ACCOUNTS + "/confirm/" + uuid))
        .andExpect(status().isNoContent());
  }

  @Test
  void test_confirmAccountRegistration_ReturnsNotFoundOnTokenNotFound() throws Exception {
    doThrow(NoSuchAccountActionToken.class)
        .when(userAccountService)
        .confirmAccountRegistration(any(UUID.class));

    mvc.perform(
        put(EndpointConstants.API_V_0_1_ACCOUNTS + "/confirm/" + UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }

  @Test
  void test_confirmAccountRegistration_ReturnsBadRequestOnInvalidToken() throws Exception {
    mvc.perform(
        put(EndpointConstants.API_V_0_1_ACCOUNTS + "/confirm/1"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void test_initiatePasswordReset_StatusIs200MeaningEntryCreatedAndEmailSent() throws Exception {
    ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder()
        .email("test@test.com")
        .build();

    String jsonDto = jsonMapper.writeValueAsString(resetPasswordDto);

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/password-reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonDto))
        .andExpect(status().isOk());
  }

  @Test
  void test_initiatePasswordReset_StatusIs404MeaningEmailDoesNotExistInUserAccounts()
      throws Exception {
    ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder()
        .email("doesnotexist@email.com")
        .build();

    String jsonDto = jsonMapper.writeValueAsString(resetPasswordDto);

    doThrow(NoSuchUserAccountException.class)
        .when(userAccountService)
        .initiatePasswordReset(any());

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/password-reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonDto))
        .andExpect(status().isNotFound());
  }

  @Test
  void test_initiatePasswordReset_StatusIs500MeaningEmailWasNotSent() throws Exception {
    ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder()
        .email("test@email.com")
        .build();

    String jsonDto = jsonMapper.writeValueAsString(resetPasswordDto);

    doThrow(new EmailNotSentException(null, null))
        .when(userAccountService)
        .initiatePasswordReset(any());

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/password-reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonDto))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void test_tokenCheck_StatusIs200MeaningTokenIsValid() throws Exception {
    var token = UUID.randomUUID();

    mvc.perform(
        get(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password/" + token)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void test_tokenCheck_StatusIs404MeaningTokenIsNotValid() throws Exception {
    var token = UUID.randomUUID();

    when(accountActionTokenService.getAndValidateToken(any()))
        .thenThrow(new InvalidTokenException(AccountActionToken.builder().build()));

    mvc.perform(
        get(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password/" + token)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void test_completePasswordReset_StatusIs200MeaningPasswordChanged() throws Exception {
    var token = UUID.randomUUID();

    NewPasswordDto newPasswordDto = new NewPasswordDto();
    newPasswordDto.setToken(token);
    newPasswordDto.setPassword("Qwerty123456!");

    String jsonDto = jsonMapper.writeValueAsString(newPasswordDto);

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonDto)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void test_completePasswordReset_ReturnsBadRequestOnInvalidToken() throws Exception {
    var token = UUID.randomUUID();

    NewPasswordDto newPasswordDto = new NewPasswordDto();
    newPasswordDto.setToken(token);
    newPasswordDto.setPassword("Qwerty123456!");

    String jsonDto = jsonMapper.writeValueAsString(newPasswordDto);

    doThrow(new InvalidTokenException(AccountActionToken.builder().build()))
        .when(userAccountService).completePasswordReset(any());

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonDto)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void test_completePasswordReset_ReturnsNotFoundOnUserAccountNotFound() throws Exception {
    var token = UUID.randomUUID();

    NewPasswordDto newPasswordDto = new NewPasswordDto();
    newPasswordDto.setToken(token);
    newPasswordDto.setPassword("Qwerty123456!");

    String jsonDto = jsonMapper.writeValueAsString(newPasswordDto);

    doThrow(NoSuchUserAccountException.class)
        .when(userAccountService).completePasswordReset(any());

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonDto)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}