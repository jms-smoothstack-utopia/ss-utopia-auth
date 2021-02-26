package com.ss.utopia.auth.service;

import com.ss.utopia.auth.client.EmailClient;
import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.entity.PasswordReset;
import com.ss.utopia.auth.exception.EmailNotSentException;
import com.ss.utopia.auth.repository.PasswordResetRepository;
import com.ss.utopia.auth.repository.UserAccountRepository;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService{

  private final PasswordResetRepository passwordResetRepo;
  private final UserAccountRepository userAccountRepo;
  private final BCryptPasswordEncoder passwordEncoder;
  private final EmailClient emailClient;

  public PasswordResetServiceImpl(
      PasswordResetRepository passwordResetRepo,
      UserAccountRepository userAccountRepo,
      BCryptPasswordEncoder passwordEncoder,
      EmailClient emailClient
      ) {
    this.passwordResetRepo = passwordResetRepo;
    this.userAccountRepo = userAccountRepo;
    this.passwordEncoder = passwordEncoder;
    this.emailClient = emailClient;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Map<String, String> addPasswordReset(ResetPasswordDto resetPasswordDto) {
    var email = resetPasswordDto.getEmail();
    var userAccount = userAccountRepo.findByEmail(email);
    if (userAccount.isPresent()){

      //Get customer ID
      UUID customerId = userAccount.get().getId();

      //Create UUID and PasswordReset object
      String customerToken = UUID.randomUUID().toString();
      PasswordReset passwordResetEntry = new PasswordReset();
      Optional<PasswordReset> passwordObject = passwordResetRepo.findByEmail(email);

      passwordObject.ifPresent(passwordReset -> passwordResetEntry.setId(passwordReset.getId()));

      //Set user parameters to PasswordReset object
      passwordResetEntry.setUserId(customerId);
      passwordResetEntry.setToken(customerToken);
      passwordResetEntry.setTimestamp(new Timestamp(new Date().getTime()));
      passwordResetEntry.setEmail(email);
      passwordResetEntry.setActive(true);

      //Save object into PasswordResetRepo
      passwordResetRepo.save(passwordResetEntry);

      //Send email using client
      var emailResponse  = emailClient.sendForgetPasswordEmail(customerToken, email);
      if (emailResponse.getStatusCodeValue() == 201){

        //Need to check the errorResponse
        System.out.println(emailResponse);

        return Map.of("token", customerToken, "emailBody",
            Objects.requireNonNull(emailResponse.getBody()));
      }
    }
    return null;
  }

  @Override
  public  Map<String, String> changePassword(NewPasswordDto newPasswordDto) {
    String token = newPasswordDto.getToken();
    var passwordResetRecord = passwordResetRepo.findByToken(token);
    if (passwordResetRecord.isPresent()){

      var currentEntry = passwordResetRecord.get();
      var userAccount = userAccountRepo.findByEmail(currentEntry.getEmail());

      if (userAccount.isEmpty() || !currentEntry.isActive()){
        return null;
      }

      var account = userAccount.get();
      account.setHashedPassword(passwordEncoder.encode(newPasswordDto.getPassword()));
      account.setLastModifiedDateTime(ZonedDateTime.now());
      userAccountRepo.save(account);

      //update password reset
      currentEntry.setActive(false);
      passwordResetRepo.save(currentEntry);

      return Map.of("message", "Password reset successful");
    }
      return null;
  }

  @Override
  public boolean tokenCheck(String token) {
    var passwordResetRecord = passwordResetRepo.findByToken(token);
    return passwordResetRecord.isPresent();
  }
}
