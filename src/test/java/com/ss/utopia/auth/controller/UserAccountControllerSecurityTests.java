package com.ss.utopia.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.utopia.auth.dto.DeleteAccountDto;
import com.ss.utopia.auth.security.SecurityConstants;
import com.ss.utopia.auth.service.PasswordResetService;
import com.ss.utopia.auth.service.UserAccountService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class UserAccountControllerSecurityTests {

  final Date expiresAt = Date.from(LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC));
  @Autowired
  WebApplicationContext wac;
  @MockBean
  SecurityConstants securityConstants;

  @MockBean
  UserAccountService userAccountService;
  @MockBean
  PasswordResetService passwordResetService;

  MockMvc mvc;

  @BeforeEach
  void beforeEach() {
    mvc = MockMvcBuilders
        .webAppContextSetup(wac)
        .apply(springSecurity())
        .build();

    when(securityConstants.getEndpoint()).thenReturn("/authenticate");
    when(securityConstants.getJwtIssuer()).thenReturn("test-issuer");
    when(securityConstants.getExpiresAt()).thenReturn(expiresAt);
    when(securityConstants.getJwtSecret()).thenReturn("superSecret");
    when(securityConstants.getUserIdClaimKey()).thenReturn("userId");
    when(securityConstants.getAuthorityClaimKey()).thenReturn("Authorities");
    when(securityConstants.getJwtHeaderName()).thenReturn("Authorization");
    when(securityConstants.getJwtHeaderPrefix()).thenReturn("Bearer ");
  }

  String getJwt(MockUser mockUser) {
    var jwt = JWT.create()
        .withSubject(mockUser.email)
        .withIssuer(securityConstants.getJwtIssuer())
        .withClaim(securityConstants.getUserIdClaimKey(), mockUser.id)
        .withClaim(securityConstants.getAuthorityClaimKey(), List.of(mockUser.getAuthority()))
        .withExpiresAt(expiresAt)
        .sign(Algorithm.HMAC512(securityConstants.getJwtSecret()));
    return "Bearer " + jwt;
  }

  @Test
  void test_getAllAccounts_OnlyAllowedByAdmin() throws Exception {
    var alwaysAuthed = List.of(MockUser.ADMIN);
    for (var user : alwaysAuthed) {
      var result = mvc
          .perform(
              get(EndpointConstants.API_V_0_1_ACCOUNTS)
                  .header("Authorization", getJwt(user)))
          .andReturn();
      assertNotEquals(403, result.getResponse().getStatus());
      assertNotEquals(401, result.getResponse().getStatus());
    }

    var notauthed = List.of(MockUser.SERVICE,
                            MockUser.EMPLOYEE,
                            MockUser.TRAVEL_AGENT,
                            MockUser.MATCH_CUSTOMER,
                            MockUser.DEFAULT,
                            MockUser.UNMATCH_CUSTOMER);
    for (var user : notauthed) {
      var result = mvc
          .perform(
              get(EndpointConstants.API_V_0_1_ACCOUNTS)
                  .header("Authorization", getJwt(user)))
          .andReturn();
      assertEquals(403, result.getResponse().getStatus(), "Failure on: " + user);
    }

    var result = mvc
        .perform(
            get(EndpointConstants.API_V_0_1_ACCOUNTS))
        .andReturn();
    assertEquals(403, result.getResponse().getStatus());
  }

  @Test
  void test_createNewAccount_AllowedByAny() throws Exception {
    var alwaysAuthed = List.of(MockUser.ADMIN,
                               MockUser.SERVICE,
                               MockUser.EMPLOYEE,
                               MockUser.TRAVEL_AGENT,
                               MockUser.MATCH_CUSTOMER,
                               MockUser.DEFAULT,
                               MockUser.UNMATCH_CUSTOMER);
    for (var user : alwaysAuthed) {
      var result = mvc
          .perform(
              post(EndpointConstants.API_V_0_1_ACCOUNTS)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("")
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();

      assertNotEquals(403, result, "Failed on " + user);
      assertNotEquals(401, result, "Failed on " + user);
    }

    var result = mvc
        .perform(
            post(EndpointConstants.API_V_0_1_ACCOUNTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
        .andReturn().getResponse().getStatus();

    assertNotEquals(403, result, "Failed on no authorization");
    assertNotEquals(401, result, "Failed on no authorization");
  }

  @Test
  void test_confirmAccountRegistration_AllowedByAny() throws Exception {
    var alwaysAuthed = List.of(MockUser.ADMIN,
                               MockUser.SERVICE,
                               MockUser.EMPLOYEE,
                               MockUser.TRAVEL_AGENT,
                               MockUser.MATCH_CUSTOMER,
                               MockUser.DEFAULT,
                               MockUser.UNMATCH_CUSTOMER);
    for (var user : alwaysAuthed) {
      var result = mvc
          .perform(
              put(EndpointConstants.API_V_0_1_ACCOUNTS + "/confirm/" + UUID.randomUUID())
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();

      assertNotEquals(403, result, "Failed on " + user);
      assertNotEquals(401, result, "Failed on " + user);
    }

    var result = mvc
        .perform(
            put(EndpointConstants.API_V_0_1_ACCOUNTS + "/confirm/" + UUID.randomUUID()))
        .andReturn().getResponse().getStatus();

    assertNotEquals(403, result, "Failed on no authorization");
    assertNotEquals(401, result, "Failed on no authorization");
  }

  @Test
  void test_addPasswordReset_AllowedByAny() throws Exception {
    var alwaysAuthed = List.of(MockUser.ADMIN,
                               MockUser.SERVICE,
                               MockUser.EMPLOYEE,
                               MockUser.TRAVEL_AGENT,
                               MockUser.MATCH_CUSTOMER,
                               MockUser.DEFAULT,
                               MockUser.UNMATCH_CUSTOMER);
    for (var user : alwaysAuthed) {
      var result = mvc
          .perform(
              post(EndpointConstants.API_V_0_1_ACCOUNTS + "/password-reset")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("")
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();

      assertNotEquals(403, result, "Failed on " + user);
      assertNotEquals(401, result, "Failed on " + user);
    }

    var result = mvc
        .perform(
            post(EndpointConstants.API_V_0_1_ACCOUNTS + "/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
        .andReturn().getResponse().getStatus();

    assertNotEquals(403, result, "Failed on no authorization");
    assertNotEquals(401, result, "Failed on no authorization");
  }

  @Test
  void test_tokenCheck_AllowedByAny() throws Exception {
    var alwaysAuthed = List.of(MockUser.ADMIN,
                               MockUser.SERVICE,
                               MockUser.EMPLOYEE,
                               MockUser.TRAVEL_AGENT,
                               MockUser.MATCH_CUSTOMER,
                               MockUser.DEFAULT,
                               MockUser.UNMATCH_CUSTOMER);
    for (var user : alwaysAuthed) {
      var result = mvc
          .perform(
              get(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password/potato")
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();

      assertNotEquals(403, result, "Failed on " + user);
      assertNotEquals(401, result, "Failed on " + user);
    }

    var result = mvc
        .perform(
            get(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password/potato"))
        .andReturn().getResponse().getStatus();

    assertNotEquals(403, result, "Failed on no authorization");
    assertNotEquals(401, result, "Failed on no authorization");
  }

  @Test
  void test_changePassword_AllowedByAny() throws Exception {
    var alwaysAuthed = List.of(MockUser.ADMIN,
                               MockUser.SERVICE,
                               MockUser.EMPLOYEE,
                               MockUser.TRAVEL_AGENT,
                               MockUser.MATCH_CUSTOMER,
                               MockUser.DEFAULT,
                               MockUser.UNMATCH_CUSTOMER);
    for (var user : alwaysAuthed) {
      var result = mvc
          .perform(
              post(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("")
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();

      assertNotEquals(403, result, "Failed on " + user);
      assertNotEquals(401, result, "Failed on " + user);
    }

    var result = mvc
        .perform(
            get(EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password/potato")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
        .andReturn().getResponse().getStatus();

    assertNotEquals(403, result, "Failed on no authorization");
    assertNotEquals(401, result, "Failed on no authorization");
  }

  @Test
  void test_deleteAccount_OnlyAllowedByAdmin() throws Exception {
    var alwaysAuthed = List.of(MockUser.ADMIN);

    for (var user : alwaysAuthed) {
      var result = mvc
          .perform(
              delete(EndpointConstants.API_V_0_1_ACCOUNTS + "/" + UUID.randomUUID())
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();
      assertNotEquals(403, result, "Failed on " + user);
      assertNotEquals(401, result, "Failed on " + user);
    }

    var notauthed = List.of(MockUser.SERVICE,
                            MockUser.EMPLOYEE,
                            MockUser.TRAVEL_AGENT,
                            MockUser.MATCH_CUSTOMER,
                            MockUser.DEFAULT,
                            MockUser.UNMATCH_CUSTOMER);
    for (var user : notauthed) {
      var result = mvc
          .perform(
              delete(EndpointConstants.API_V_0_1_ACCOUNTS + "/" + UUID.randomUUID())
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();

      assertEquals(403, result, "Failed on " + user);
    }
    var result = mvc
        .perform(
            delete(EndpointConstants.API_V_0_1_ACCOUNTS + "/" + UUID.randomUUID()))
        .andReturn().getResponse().getStatus();

    assertEquals(403, result, "Failed on no authorization");
  }

  @Test
  void test_initiateCustomerDeletion_OnlyAllowedByServiceOrAdmin() throws Exception {
    var alwaysAuthed = List.of(MockUser.SERVICE, MockUser.ADMIN);

    var content = new ObjectMapper().writeValueAsString(DeleteAccountDto.builder()
                                                            .id(UUID.randomUUID())
                                                            .email("whatever@test.com")
                                                            .password("abCD1234!@")
                                                            .build());

    for (var user : alwaysAuthed) {
      var result = mvc
          .perform(
              delete(EndpointConstants.API_V_0_1_ACCOUNTS + "/customer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(content)
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();

      assertEquals(204, result, "Failed on " + user);
    }

    var notauthed = List.of(MockUser.EMPLOYEE,
                            MockUser.TRAVEL_AGENT,
                            MockUser.MATCH_CUSTOMER,
                            MockUser.DEFAULT,
                            MockUser.UNMATCH_CUSTOMER);
    for (var user : notauthed) {
      var result = mvc
          .perform(
              delete(EndpointConstants.API_V_0_1_ACCOUNTS + "/customer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(content)
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();

      assertEquals(403, result, "Failed on " + user);
    }
    var result = mvc
        .perform(
            delete(EndpointConstants.API_V_0_1_ACCOUNTS + "/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
        .andReturn().getResponse().getStatus();

    assertEquals(403, result, "Failed on no authorization");
  }

  @Test
  void test_completeCustomerDeletion_OnlyAllowedByServiceOrAdmin() throws Exception {
    var alwaysAuthed = List.of(MockUser.SERVICE, MockUser.ADMIN);

    var content = new ObjectMapper().writeValueAsString(DeleteAccountDto.builder()
                                                            .id(UUID.randomUUID())
                                                            .email("whatever@test.com")
                                                            .password("abCD1234!@")
                                                            .build());

    var url = EndpointConstants.API_V_0_1_ACCOUNTS + "/customer/" + UUID.randomUUID();

    for (var user : alwaysAuthed) {
      var result = mvc
          .perform(
              delete(url)
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();
      assertNotEquals(403, result, "Failed on " + user);
      assertNotEquals(401, result, "Failed on " + user);
    }

    var notauthed = List.of(MockUser.EMPLOYEE,
                            MockUser.TRAVEL_AGENT,
                            MockUser.MATCH_CUSTOMER,
                            MockUser.DEFAULT,
                            MockUser.UNMATCH_CUSTOMER);
    for (var user : notauthed) {
      var result = mvc
          .perform(
              delete(url)
                  .header("Authorization", getJwt(user)))
          .andReturn().getResponse().getStatus();

      assertEquals(403, result, "Failed on " + user);
    }
    var result = mvc
        .perform(
            delete(url))
        .andReturn().getResponse().getStatus();

    assertEquals(403, result, "Failed on no authorization");
  }

  enum MockUser {
    DEFAULT("default@test.com", "ROLE_DEFAULT", UUID.randomUUID().toString()),
    MATCH_CUSTOMER("eddy_grant@test.com", "ROLE_CUSTOMER", "a4a9feca-bfe7-4c45-8319-7cb6cdd359db"),
    UNMATCH_CUSTOMER("someOtherCustomer@test.com", "ROLE_CUSTOMER", UUID.randomUUID().toString()),
    EMPLOYEE("employee@test.com", "ROLE_EMPLOYEE", UUID.randomUUID().toString()),
    TRAVEL_AGENT("travel_agent@test.com", "ROLE_TRAVEL_AGENT", UUID.randomUUID().toString()),
    SERVICE("service@test.com", "ROLE_SERVICE", UUID.randomUUID().toString()),
    ADMIN("admin@test.com", "ROLE_ADMIN", UUID.randomUUID().toString());


    final String email;
    final GrantedAuthority grantedAuthority;
    final String id;

    MockUser(String email, String grantedAuthority, String id) {
      this.email = email;
      this.grantedAuthority = new SimpleGrantedAuthority(grantedAuthority);
      this.id = id;
    }

    public String getAuthority() {
      return grantedAuthority.getAuthority();
    }
  }
}
