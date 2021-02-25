package com.ss.utopia.auth.service;

import com.ss.utopia.auth.dto.EmailDto;
import com.ss.utopia.auth.dto.NewPasswordDto;
import com.ss.utopia.auth.dto.ResetPasswordDto;
import com.ss.utopia.auth.entity.PasswordReset;
import com.ss.utopia.auth.repository.PasswordResetRepository;
import com.ss.utopia.auth.repository.UserAccountRepository;
import java.time.ZonedDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetServiceImpl implements PasswordResetService{

  private final PasswordResetRepository passwordResetRepo;
  private final UserAccountRepository userAccountRepo;
  private final BCryptPasswordEncoder passwordEncoder;

  public PasswordResetServiceImpl(
      PasswordResetRepository passwordResetRepo,
      UserAccountRepository userAccountRepo,
      BCryptPasswordEncoder passwordEncoder
      ) {
    this.passwordResetRepo = passwordResetRepo;
    this.userAccountRepo = userAccountRepo;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public ResponseEntity<String> addPasswordReset(ResetPasswordDto resetPasswordDto) {
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

      //Then need to send email here using SES
      String mailUrl = "http://localhost:6900/api/v0.1/email";
      EmailDto emailToSend = new EmailDto();
      emailToSend.setEmail(email);
      emailToSend.setSubject("Reset Utopia password");
      String customerUrl = "http://localhost:4200";
      emailToSend.setContent("<h1>Utopia password change</h1>"
          + "<span>Please use this link to change your password (Link will expire in one day) </span>"
          + "<h2><a href='" + customerUrl + "/login/password/resetform/" + customerToken + "'>Change email</a></h2>"
          + "<h3><span>Thanks</span></h3><h3>The Utopia team</h3>");
      RestTemplate emailRestTemplate = new RestTemplate();
      return emailRestTemplate.postForEntity(mailUrl, emailToSend, String.class);
    }
    return ResponseEntity.notFound().build();
  }

  @Override
  public ResponseEntity<Map<String, String>> verifyToken(NewPasswordDto newPasswordDto) {
    String token = newPasswordDto.getToken();
    var passwordResetRecord = passwordResetRepo.findByToken(token);
    if (passwordResetRecord.isPresent()){

      var currentEntry = passwordResetRecord.get();
      var userAccount = userAccountRepo.findByEmail(currentEntry.getEmail());

      if (userAccount.isEmpty()){
        String errorMsg = "Server error with token:  " + token;
        Map<String, String> jsonReturn = Map.of("message", errorMsg);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonReturn);
      }

      var account = userAccount.get();
      System.out.println(account);
      account.setHashedPassword(passwordEncoder.encode(newPasswordDto.getPassword()));
      account.setLastModifiedDateTime(ZonedDateTime.now());
      System.out.println(account);
      userAccountRepo.save(account);

      //update password reset
      currentEntry.setActive(false);
      passwordResetRepo.save(currentEntry);

      Map<String, String> jsonReturn = Map.of("message", "Password reset successful");
      return ResponseEntity.ok().body(jsonReturn);
    }
    else{
      String errorMsg = "Server error with token:  " + token;
      Map<String, String> jsonReturn = Map.of("message", errorMsg);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonReturn);
    }
  }
}
