package com.ss.utopia.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.utopia.auth.controller.EndpointConstants;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final SecurityConstants securityConstants;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .cors().and().csrf().disable()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, EndpointConstants.API_V_0_1_ACCOUNTS).permitAll()
        .antMatchers(HttpMethod.PUT, EndpointConstants.API_V_0_1_ACCOUNTS + "/confirm/**").permitAll()
        .antMatchers(HttpMethod.POST, EndpointConstants.API_V_0_1_ACCOUNTS + "/password-reset").permitAll()
        .antMatchers(HttpMethod.POST, EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password").permitAll()
        .antMatchers(HttpMethod.GET, EndpointConstants.API_V_0_1_ACCOUNTS + "/new-password/{token}").permitAll()
        .antMatchers("/api-docs/**").permitAll()
//        .antMatchers("/h2-console/**").permitAll()
        .anyRequest().authenticated()
        .and()
        .addFilter(new JwtAuthenticationFilter(authenticationManager(),
                                               new ObjectMapper(), securityConstants))
        .addFilter(new JwtAuthenticationVerificationFilter(authenticationManager(),
                                                           securityConstants))
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    ;
//    http.headers().frameOptions().disable();
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }



}
