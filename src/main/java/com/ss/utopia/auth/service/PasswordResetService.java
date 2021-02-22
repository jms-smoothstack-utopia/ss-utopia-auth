package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.ResetPasswordDto;

public interface PasswordResetService {

  void addPasswordReset(ResetPasswordDto resetPasswordDto);

  void verifyToken(String token);
}
