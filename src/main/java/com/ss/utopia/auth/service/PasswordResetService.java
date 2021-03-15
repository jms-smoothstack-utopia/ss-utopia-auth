package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;

public interface PasswordResetService {

  String addPasswordReset(ResetPasswordDto resetPasswordDto);

  void changePassword(NewPasswordDto newPasswordDto);

  boolean tokenCheck(String token);
}
