package com.ss.utopia.auth.client;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public interface EmailClient {

  void sendForgotPasswordEmail(String recipientEmail, UUID token);

  void sendConfirmAccountEmail(String recipientEmail, UUID token);

  void sendDeleteAccountEmail(String recipientEmail, UUID token);
}
