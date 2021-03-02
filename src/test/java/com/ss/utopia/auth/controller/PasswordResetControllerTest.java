package com.ss.utopia.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.exception.EmailNotSentException;
import com.ss.utopia.auth.repository.PasswordResetRepository;
import com.ss.utopia.auth.repository.UserAccountRepository;
import com.ss.utopia.auth.service.PasswordResetService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(PasswordResetController.class)
public class PasswordResetControllerTest {

  ObjectMapper jsonMapper = new ObjectMapper();

  @Autowired
  WebApplicationContext wac;

  @MockBean
  UserAccountRepository userAccountRepository;

  @MockBean
  PasswordResetRepository passwordResetRepository;

  @MockBean
  PasswordResetService passwordResetService;

  private MockMvc mvc;

  @BeforeEach
  void beforeEach(){
    mvc = MockMvcBuilders
        .webAppContextSetup(wac)
        .build();
  }

  @Test
  void test_addPasswordReset_StatusIs200MeaningEntryCreatedAndEmailSent() throws Exception{
    ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder()
        .email("test@test.com")
        .build();

    String jsonDto = jsonMapper.writeValueAsString(resetPasswordDto);
    String token = UUID.randomUUID().toString();

    when(passwordResetService.addPasswordReset(any(resetPasswordDto.getClass()))).thenReturn(Map.of("token", token));

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/password-reset")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonDto))
        .andExpect(status().isOk());
  }

  @Test
  void test_addPasswordReset_StatusIs404MeaningEmailDoesNotExistInUserAccounts() throws Exception{
    ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder()
        .email("doesnotexist@email.com")
        .build();

    String jsonDto = jsonMapper.writeValueAsString(resetPasswordDto);

    when(passwordResetService.addPasswordReset(any(resetPasswordDto.getClass()))).thenReturn(null);

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/password-reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonDto))
        .andExpect(status().isNotFound());
  }

  @Test
  void test_addPasswordReset_StatusIs500MeaningEmailWasNotSent() throws Exception{
    ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder()
        .email("test@email.com")
        .build();

    String jsonDto = jsonMapper.writeValueAsString(resetPasswordDto);

    when(passwordResetService.addPasswordReset(any(resetPasswordDto.getClass())))
        .thenThrow(new EmailNotSentException());

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/password-reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonDto))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void test_tokenCheck_StatusIs200MeaningTokenIsValid() throws Exception{

    String token = UUID.randomUUID().toString();

    when(passwordResetService.tokenCheck(token)).thenReturn(Boolean.TRUE);

    mvc.perform(
        get(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password/" + token)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void test_tokenCheck_StatusIs404MeaningTokenIsNotValid() throws Exception{

    String token = UUID.randomUUID().toString();

    when(passwordResetService.tokenCheck(token)).thenReturn(Boolean.FALSE);

    mvc.perform(
        get(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password/" + token)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void test_changePassword_StatusIs200MeaningPasswordChanged() throws Exception{

    String token = UUID.randomUUID().toString();

    NewPasswordDto newPasswordDto = new NewPasswordDto();
    newPasswordDto.setToken(token);
    newPasswordDto.setPassword("Qwerty123456!");

    String jsonDto = jsonMapper.writeValueAsString(newPasswordDto);

    Map<String, String> success = Map.of("message", "Password reset successful");

    when(passwordResetService.changePassword(any(NewPasswordDto.class))).thenReturn(success);

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonDto)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void test_changePassword_StatusIs404MeaningTokenWasInvalid() throws Exception{

    String token = UUID.randomUUID().toString();

    NewPasswordDto newPasswordDto = new NewPasswordDto();
    newPasswordDto.setToken(token);
    newPasswordDto.setPassword("Qwerty123456!");

    String jsonDto = jsonMapper.writeValueAsString(newPasswordDto);

    when(passwordResetService.changePassword(any(NewPasswordDto.class))).thenReturn(
        null);

    mvc.perform(
        post(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonDto)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
