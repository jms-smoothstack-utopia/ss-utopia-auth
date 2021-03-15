package com.ss.utopia.auth.entity;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordReset {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(columnDefinition = "BINARY(16)")
  private UUID userId;

  private String email;

  @Column(unique = true)
  private String token;

  @Column(updatable = false)
  @CreationTimestamp
  private ZonedDateTime creation;

  @Builder.Default
  private boolean isActive = true;
}
