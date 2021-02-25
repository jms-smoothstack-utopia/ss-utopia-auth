package com.ss.utopia.auth.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.utopia.auth.dto.CreateUserAccountDto;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.exception.DuplicateEmailException;
import com.ss.utopia.auth.repository.UserAccountRepository;
import com.ss.utopia.auth.service.UserAccountService;
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

@WebMvcTest(UserAccountController.class)
class UserAccountControllerTest {

  ObjectMapper jsonMapper = new ObjectMapper();

  @Autowired
  WebApplicationContext wac;

  @MockBean
  UserAccountRepository repository;
  @MockBean
  UserAccountService service;

  private MockMvc mvc;

  @BeforeEach
  void beforeEach() {
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
    var expectedHeaderVal = EndpointConstants.ACCOUNTS_ENDPOINT + "/" + uuid;

    when(service.createNewAccount(createDto))
        .thenReturn(UserAccount.builder().id(uuid).build());

    mvc.perform(
        post(EndpointConstants.ACCOUNTS_ENDPOINT)
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

    when(service.createNewAccount(createDto))
        .thenThrow(new DuplicateEmailException("test2@test.com"));

    var jsonDto = jsonMapper.writeValueAsString(createDto);

    mvc.perform(
        post(EndpointConstants.ACCOUNTS_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonDto))
        .andExpect(status().isConflict());
  }
}