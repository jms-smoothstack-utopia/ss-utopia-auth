package com.ss.utopia.auth.dto;

import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteAccountDto {

  @NotNull
  private UUID id;

  @Email
  @NotNull
  private String email;

  @NotNull
  @NotBlank
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private String password;
}
