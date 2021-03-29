package com.ss.utopia.auth.dto;

import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewPasswordDto {

  public static final String REGEX =
      "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*-_=+,.?])[A-Za-z\\d!@#$%^&*-_=+,.?]{10,128}$";
  public static final String REGEX_MSG = "Password must be between 10 and 128 characters,"
      + " contain at least one lowercase letter,"
      + " at least one uppercase letter,"
      + " at least one number,"
      + " and at least one special character from the following: !@#$%^&*-_=+,.?";

  @NotNull
  private UUID token;

  @ToString.Exclude
  @NotNull
  @NotBlank(message = "Password cannot be blank.")
  @Size(min = 10, max = 128, message = "Length must be between 10 and 128 characters.")
  @Pattern(regexp = REGEX, message = REGEX_MSG)
  private String password;
}
