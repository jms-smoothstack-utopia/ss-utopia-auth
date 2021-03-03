package com.ss.utopia.auth.client;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public interface EmailClient {

  void sendForgetPasswordEmail(String token, String email);

  void sendConfirmAccountEmail(String recipient, UUID confirmationToken);
}
