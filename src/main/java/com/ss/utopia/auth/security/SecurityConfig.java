package com.ss.utopia.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.utopia.auth.controller.EndpointConstants;
import java.util.Date;
import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .cors().and().csrf().disable()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, EndpointConstants.ACCOUNTS_ENDPOINT).permitAll()
        .anyRequest().authenticated()
        .and()
        .addFilter(new JwtAuthenticationFilter(authenticationManager(),
                                               new ObjectMapper(), securityConstants()))
        .addFilter(new JwtAuthenticationVerificationFilter(authenticationManager(),
                                                           securityConstants()))
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    ;
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  public SecurityConstants securityConstants() {
    return SecurityConstants.INSTANCE;
  }

  @Data
  public static class SecurityConstants {
    //todo load all from env

    private static final SecurityConstants INSTANCE = new SecurityConstants();

    private final String authEndpoint = "/authenticate";
    private final String jwtSecret = "superSecret";
    private final String jwtHeaderName = "Authorization";
    private final String jwtHeaderPrefix = "Bearer ";
    private final String jwtIssuer = "ss-utopia";
    //todo determine expiration time
    private final long jwtExpirationDuration = 86_400_000; // 24 hours

    //singleton that is only instantiated via parent
    private SecurityConstants() {
    }

    public Date getExpiresAt() {
      //todo possible concern with different timezones between services?
      return new Date(System.currentTimeMillis() + jwtExpirationDuration);
    }
  }
}
