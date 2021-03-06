package com.ss.utopia.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.utopia.auth.controller.EndpointConstants;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final Environment environment;
  private final SecurityConstants securityConstants;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // enable h2 console if using that profile
    if (Arrays.asList(environment.getActiveProfiles()).contains("local-h2")) {
      http.authorizeRequests().antMatchers("/h2-console/**").permitAll();
      http.headers().frameOptions().disable();
    }

    // fixme: this is duplicated across a few places due to a weird bug with mocking during tests.
    var authEndpoint = securityConstants.getEndpoint();
    if (authEndpoint == null || authEndpoint.isEmpty() || authEndpoint.isBlank()) {
      authEndpoint = "/authenticate";
      log.warn("Authentication endpoint is null. Setting default endpoint of '/authenticate'");
    }

    http
        .cors().and().csrf().disable()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, authEndpoint).permitAll()
        .antMatchers(HttpMethod.POST, EndpointConstants.API_V_0_1_ACCOUNTS).permitAll()
        .antMatchers(HttpMethod.PUT, EndpointConstants.API_V_0_1_ACCOUNTS + "/confirm/**").permitAll()
        .antMatchers(HttpMethod.POST, EndpointConstants.API_V_0_1_ACCOUNTS + "/password-reset").permitAll()
        .antMatchers(HttpMethod.POST, EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password").permitAll()
        .antMatchers(HttpMethod.GET, EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password/{token}").permitAll()
        .antMatchers("/api-docs/**").permitAll()
        .anyRequest().authenticated()
        .and()
        .addFilter(new JwtAuthenticationFilter(authenticationManager(),
                                               new ObjectMapper(), securityConstants))
        .addFilter(new JwtAuthenticationVerificationFilter(authenticationManager(),
                                                           securityConstants))
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    ;
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }
}
