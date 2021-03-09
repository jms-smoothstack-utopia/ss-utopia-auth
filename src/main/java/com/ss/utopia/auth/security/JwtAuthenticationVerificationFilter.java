package com.ss.utopia.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtAuthenticationVerificationFilter extends BasicAuthenticationFilter {

  private final SecurityConstants securityConstants;

  public JwtAuthenticationVerificationFilter(AuthenticationManager authenticationManager,
                                             SecurityConstants securityConstants) {
    super(authenticationManager);
    this.securityConstants = securityConstants;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws IOException, ServletException {
    var header = request.getHeader(securityConstants.getJwtHeaderName());
    try {
      if (header != null && header.startsWith(securityConstants.getJwtHeaderPrefix())) {
        var authToken = getAuthenticationToken(request);
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
      chain.doFilter(request, response);
    } catch (TokenExpiredException ex) {
      log.debug("Expired token");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("{\"error\":\"token expired\"}");
    }
  }

  private UsernamePasswordAuthenticationToken getAuthenticationToken(HttpServletRequest request) {
    var token = request.getHeader(securityConstants.getJwtHeaderName());
    if (token == null) {
      return null;
    }

    var jwt = JWT.require(Algorithm.HMAC512(securityConstants.getJwtSecret()))
        .build()
        .verify(token.replace(securityConstants.getJwtHeaderPrefix(), ""));

    var subject = jwt.getSubject();

    if (subject == null) {
      return null;
    }

    var authorities = jwt.getClaim(securityConstants.getAuthorityClaimKey())
        .asList(String.class)
        .stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());

    var userId = jwt.getClaim(securityConstants.getUserIdClaimKey()).asString();

    var jwtPrincipal = JwtPrincipal.builder()
        .email(subject)
        .userId(UUID.fromString(userId))
        .build();

    return new UsernamePasswordAuthenticationToken(jwtPrincipal, null, authorities);
  }
}