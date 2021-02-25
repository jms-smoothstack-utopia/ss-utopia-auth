package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import java.util.Map;
import org.springframework.http.ResponseEntity;

public interface PasswordResetService {

  ResponseEntity<String> addPasswordReset(ResetPasswordDto resetPasswordDto);

  ResponseEntity<Map<String, String>> verifyToken(NewPasswordDto newPasswordDto);
}
