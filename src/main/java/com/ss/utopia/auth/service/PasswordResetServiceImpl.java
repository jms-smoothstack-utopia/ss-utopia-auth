package com.ss.utopia.auth.service;

import com.ss.utopia.auth.client.EmailClient;
import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.entity.PasswordReset;
import com.ss.utopia.auth.exception.InvalidTokenException;
import com.ss.utopia.auth.repository.PasswordResetRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

  private final PasswordResetRepository passwordResetRepository;
  private final UserAccountService userAccountService;
  private final BCryptPasswordEncoder passwordEncoder;
  private final EmailClient emailClient;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public String addPasswordReset(ResetPasswordDto resetPasswordDto) {
    var email = resetPasswordDto.getEmail();
    var userAccount = userAccountService.getByEmail(email);

    //Get customer ID
    UUID customerId = userAccount.getId();

    //Create UUID and PasswordReset object
    String customerToken = UUID.randomUUID().toString();
    PasswordReset passwordResetEntry = new PasswordReset();
    Optional<PasswordReset> passwordObject = passwordResetRepository.findByEmail(email);

    passwordObject.ifPresent(passwordReset -> passwordResetEntry.setId(passwordReset.getId()));

    //Set user parameters to PasswordReset object
    passwordResetEntry.setUserId(customerId);
    passwordResetEntry.setToken(customerToken);
    passwordResetEntry.setEmail(email);
    passwordResetEntry.setActive(true);

    //Save object into PasswordResetRepo
    passwordResetRepository.save(passwordResetEntry);

    //Send email using client
    emailClient.sendForgetPasswordEmail(customerToken, email);

    return customerToken;
  }

  @Override
  public void changePassword(NewPasswordDto newPasswordDto) {
    String token = newPasswordDto.getToken();
    var passwordResetRecord = passwordResetRepository.findByToken(token).orElseThrow();

    if (!passwordResetRecord.isActive()) {
      throw new InvalidTokenException(token);
    }

    var userAccount = userAccountService.getByEmail(passwordResetRecord.getEmail());

    userAccount.setPassword(passwordEncoder.encode(newPasswordDto.getPassword()));
    userAccountService.updateAccount(userAccount);

    //update password reset
    passwordResetRecord.setActive(false);
    passwordResetRepository.save(passwordResetRecord);
  }

  @Override
  public boolean tokenCheck(String token) {
    var record = passwordResetRepository.findByToken(token).orElseThrow();
    return record.isActive();
  }
}
