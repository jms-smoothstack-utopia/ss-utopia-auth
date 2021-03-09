package com.ss.utopia.auth.security;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@Builder
@RequiredArgsConstructor
public class JwtPrincipal {

  private final UUID userId;
  private final String email;
}
