package com.ss.utopia.auth.security;

import java.util.Date;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "com.ss.utopia.auth", ignoreUnknownFields = false)
public class SecurityConstants {

  private String endpoint;
  private String jwtSecret;
  private String jwtHeaderName;
  private String jwtHeaderPrefix;
  private String jwtIssuer;
  private long jwtExpirationDuration;

  public void setJwtExpirationDuration(String jwtExpirationDuration) {
    this.jwtExpirationDuration = Long.parseLong(jwtExpirationDuration.replaceAll("_", ""));
  }

  public Date getExpiresAt() {
    //todo possible concern with different timezones between services?
    return new Date(System.currentTimeMillis() + jwtExpirationDuration);
  }

}
