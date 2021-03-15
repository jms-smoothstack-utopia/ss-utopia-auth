package com.ss.utopia.auth.security;

import com.ss.utopia.auth.repository.UserAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JpaUserDetailsService implements UserDetailsService {

  private final UserAccountRepository userAccountRepository;

  public JpaUserDetailsService(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) {
    log.debug("Load user: email=" + email);

    return userAccountRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException(
            "User account with email '" + email + "' not found."));
  }
}
