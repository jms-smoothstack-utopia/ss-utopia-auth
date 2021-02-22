package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.entity.PasswordReset;
import com.ss.utopia.auth.repository.PasswordResetRepository;
import com.ss.utopia.auth.repository.UserAccountRepository;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetServiceImpl implements PasswordResetService{

  private final PasswordResetRepository passwordResetRepo;
  private final UserAccountRepository userAccountRepo;

  public PasswordResetServiceImpl(
      PasswordResetRepository passwordResetRepo, UserAccountRepository userAccountRepo) {
    this.passwordResetRepo = passwordResetRepo;
    this.userAccountRepo = userAccountRepo;
  }

  @Override
  public void addPasswordReset(ResetPasswordDto resetPasswordDto) {
    var email = resetPasswordDto.getEmail();
    var userAccount = userAccountRepo.findByEmail(email);
    if (userAccount.isPresent()){
      //Get customer ID
      UUID customerId = userAccount.get().getId();

      //Create UUID and PasswordReset object
      String customerToken = UUID.randomUUID().toString();
      PasswordReset passwordResetEntry = new PasswordReset();
      Optional<PasswordReset> passwordObject = passwordResetRepo.findByUserId(customerId);

      passwordObject.ifPresent(passwordReset -> passwordResetEntry.setId(passwordReset.getId()));

      //Set user parameters to PasswordReset object
      passwordResetEntry.setUserId(customerId);
      passwordResetEntry.setToken(customerToken);
      passwordResetEntry.setTimestamp(new Timestamp(new Date().getTime()));

      //Save object into PasswordResetRepo
      passwordResetRepo.save(passwordResetEntry);

      //Then need to send email here using SES
    }
  }

  @Override
  public void verifyToken(String token) {

  }
}
