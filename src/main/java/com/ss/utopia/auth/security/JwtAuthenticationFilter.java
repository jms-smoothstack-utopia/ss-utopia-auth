package com.ss.utopia.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.utopia.auth.dto.AuthDto;
import com.ss.utopia.auth.dto.AuthResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final ObjectMapper objectMapper;
  private final SecurityConstants securityConstants;

  public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                 ObjectMapper objectMapper,
                                 SecurityConstants securityConstants) {
    super(authenticationManager);
    this.authenticationManager = authenticationManager;
    this.objectMapper = objectMapper;
    this.securityConstants = securityConstants;
    setFilterProcessesUrl(securityConstants.getEndpoint());
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
                                              HttpServletResponse response)
      throws AuthenticationException {
    log.debug("Attempt Authentication");
    try (var inputStream = request.getInputStream()) {
      var credentials = objectMapper.readValue(inputStream, AuthDto.class);
      var token = new UsernamePasswordAuthenticationToken(credentials.getEmail(),
                                                          credentials.getPassword(),
                                                          Collections.emptySet());
      return authenticationManager.authenticate(token);
    } catch (IOException ex) {
      // results from poorly formed request body, log and cause failure
      log.debug(ex.getMessage());
      var emptyToken = new UsernamePasswordAuthenticationToken(null, null, null);
      return authenticationManager.authenticate(emptyToken);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request,
                                          HttpServletResponse response,
                                          FilterChain chain,
                                          Authentication authResult) throws IOException {
    log.debug("Successful authentication.");

    var email = authResult.getName();
    var authorities = authResult.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    var expiresAt = securityConstants.getExpiresAt();

    var jwt = JWT.create()
        .withSubject(email)
        .withIssuer(securityConstants.getJwtIssuer())
        .withClaim("Authorities", authorities)
        .withExpiresAt(expiresAt)
        .sign(Algorithm.HMAC512(securityConstants.getJwtSecret()));

    var headerVal = securityConstants.getJwtHeaderPrefix() + jwt;

    log.debug("Created JWT: " + jwt);

    var respBody = objectMapper
        .writeValueAsString(new AuthResponse(headerVal, expiresAt.getTime()));

    response.addHeader(securityConstants.getJwtHeaderName(), headerVal);
    response.getWriter().write(respBody);
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            AuthenticationException failed)
      throws IOException, ServletException {
    log.debug("Auth failure");
    super.unsuccessfulAuthentication(request, response, failed);
  }

}
