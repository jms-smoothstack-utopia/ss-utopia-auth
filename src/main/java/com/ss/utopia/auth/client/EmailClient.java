package com.ss.utopia.auth.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public interface EmailClient {

  ResponseEntity<String> sendForgetPasswordEmail(String token, String  email);
}
