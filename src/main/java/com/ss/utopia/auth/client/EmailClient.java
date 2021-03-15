package com.ss.utopia.auth.client;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public interface EmailClient {

  void sendForgetPasswordEmail(String token, String email);

  void sendConfirmAccountEmail(String recipient, UUID confirmationToken);

  void sendDeleteAccountEmail(String recipient, UUID token);
}
