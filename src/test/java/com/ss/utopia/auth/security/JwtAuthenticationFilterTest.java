package com.ss.utopia.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.utopia.auth.dto.AuthDto;
import com.ss.utopia.auth.dto.AuthResponse;
import com.ss.utopia.auth.entity.UserAccount;
import com.ss.utopia.auth.entity.UserRole;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class JwtAuthenticationFilterTest {

  static SecurityConstants mockSecurityConstants = Mockito.mock(SecurityConstants.class);

  static Date mockJwtExpireDate = new Date(System.currentTimeMillis() + 1_000L);

  AuthenticationManager mockAuthManager = Mockito.mock(AuthenticationManager.class);
  HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
  HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
  ObjectMapper mockObjectMapper = Mockito.mock(ObjectMapper.class);
  JwtAuthenticationFilter filterToTest = new JwtAuthenticationFilter(mockAuthManager,
                                                                     mockObjectMapper,
                                                                     mockSecurityConstants);

  @BeforeAll
  static void beforeAll() {
    when(mockSecurityConstants.getEndpoint()).thenReturn("/login");
    when(mockSecurityConstants.getJwtSecret()).thenReturn("superSecret");
    when(mockSecurityConstants.getJwtHeaderName()).thenReturn("Authorization");
    when(mockSecurityConstants.getJwtHeaderPrefix()).thenReturn("Bearer ");
    when(mockSecurityConstants.getJwtIssuer()).thenReturn("ss-utopia");
    when(mockSecurityConstants.getJwtExpirationDuration()).thenReturn(100L);
    when(mockSecurityConstants.getAuthorityClaimKey()).thenReturn("Authorities");
    when(mockSecurityConstants.getUserIdClaimKey()).thenReturn("userId");
    when(mockSecurityConstants.getExpiresAt()).thenReturn(mockJwtExpireDate);
  }

  @BeforeEach
  void beforeEach() {
    Mockito.reset(mockAuthManager);
    Mockito.reset(mockRequest);
    Mockito.reset(mockResponse);
    Mockito.reset(mockObjectMapper);
  }

  /**
   * Attempting authentication should return a {@link Authentication}. In this test, we verify that
   * we receive back the mocked item.
   */
  @Test
  void test_attemptAuthentication_ReturnsAuthenticationWithCredentialsWhenSuppliedInRequest()
      throws Exception {
    var mockAutDto = AuthDto.builder().email("test@test.com").password("abCD1234!@").build();

    var mockRequestInputStream = Mockito.mock(ServletInputStream.class);

    when(mockRequest.getInputStream()).thenReturn(mockRequestInputStream);

    when(mockObjectMapper.readValue(mockRequestInputStream, AuthDto.class)).
        thenReturn(mockAutDto);

    var expectedToken = new UsernamePasswordAuthenticationToken(mockAutDto.getEmail(),
                                                                mockAutDto.getPassword(),
                                                                Collections.emptySet());

    var mockAuthentication = Mockito.mock(Authentication.class);

    when(mockAuthentication.getPrincipal()).thenReturn(mockAutDto.getEmail());

    when(mockAuthManager.authenticate(expectedToken))
        .thenReturn(mockAuthentication);

    var result = filterToTest.attemptAuthentication(mockRequest, mockResponse);

    assertEquals(mockAuthentication, result);
    assertEquals(mockAuthentication.getPrincipal(), mockAutDto.getEmail());
  }

  @Test
  void test_attemptAuthentication_ReturnsEmptyAuthenticationWhenCredentialsNotSupplied()
      throws Exception {
    var mockEmptyAuth = Mockito.mock(Authentication.class);
    var mockFilledAuth = Mockito.mock(Authentication.class);

    var mockEmptyToken = Mockito.mock(UsernamePasswordAuthenticationToken.class);
    var mockFilledToken = Mockito.mock(UsernamePasswordAuthenticationToken.class);

    //sanity
    assertNotEquals(mockEmptyAuth, mockFilledAuth);
    assertNotEquals(mockEmptyToken, mockFilledToken);

    when(mockAuthManager.authenticate(any())).thenReturn(mockEmptyAuth);

    when(mockRequest.getInputStream())
        .thenThrow(new IOException());

    var result = filterToTest.attemptAuthentication(mockRequest, mockResponse);

    assertEquals(mockEmptyAuth, result);
    assertNotEquals(mockFilledAuth, result);

    Mockito.reset(mockRequest);

    var mockInputStream = Mockito.mock(ServletInputStream.class);

    when(mockRequest.getInputStream()).thenReturn(mockInputStream);
    when(mockObjectMapper.readValue(mockInputStream, AuthDto.class))
        .thenThrow(new IOException());

    result = filterToTest.attemptAuthentication(mockRequest, mockResponse);

    assertEquals(mockEmptyAuth, result);
    assertNotEquals(mockFilledAuth, result);
  }

  /**
   * Validate that the JWT is written to the response headers as required and that we return the JWT
   * as a response body.
   */
  @Test
  void test_successfulAuthentication_ResponseContainsJWTInHeader() throws IOException {
    var mockAuthResult = Mockito.mock(Authentication.class);
    var mockAuthDto = AuthDto.builder().email("test@test.com").password("abCD1234!@").build();

    var mockUserAccount = UserAccount.builder()
        .id(UUID.randomUUID())
        .email(mockAuthDto.getEmail())
        .password("some password")
        .userRole(UserRole.DEFAULT)
        .build();

    when(mockAuthResult.getName()).thenReturn(mockAuthDto.getEmail());

    when(mockAuthResult.getPrincipal()).thenReturn(mockUserAccount);

    var mockAuthorities = mockUserAccount.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    var expectedJwt = JWT.create()
        .withSubject(mockAuthDto.getEmail())
        .withIssuer(mockSecurityConstants.getJwtIssuer())
        .withClaim(mockSecurityConstants.getUserIdClaimKey(), mockUserAccount.getId().toString())
        .withClaim(mockSecurityConstants.getAuthorityClaimKey(), mockAuthorities)
        .withExpiresAt(mockJwtExpireDate)
        .sign(Algorithm.HMAC512(mockSecurityConstants.getJwtSecret()));

    var spyResponse = Mockito.spy(mockResponse);

    var expectedAuthResponse = new AuthResponse(mockUserAccount.getId(),
                                                "Bearer " + expectedJwt,
                                                mockSecurityConstants.getExpiresAt().getTime());

    var expectedAuthResponseAsJson = new ObjectMapper()
        .writeValueAsString(expectedAuthResponse);

    class SpyWriter extends PrintWriter {

      public SpyWriter(OutputStream out) {
        super(out);
      }

      @Override
      public void write(String s) {
        assertEquals(expectedAuthResponseAsJson, s);
      }
    }

    // spy on the response body
    var spyWriter = new SpyWriter(System.out);
    when(spyResponse.getWriter()).thenReturn(spyWriter);

    // expect that we get the JSON of the auth response values when that is put in to be written as string
    // written to the response body
    when(mockObjectMapper.writeValueAsString(expectedAuthResponse))
        .thenReturn(expectedAuthResponseAsJson);

    filterToTest.successfulAuthentication(mockRequest,
                                          spyResponse,
                                          Mockito.mock(FilterChain.class),
                                          mockAuthResult);

    //expect the bearer token to be added as a header
    Mockito.verify(spyResponse).addHeader("Authorization", "Bearer " + expectedJwt);
  }

}