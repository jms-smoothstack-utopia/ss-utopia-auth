package com.ss.utopia.auth.entity;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordReset {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private UUID userId;

  @Column(unique = true)
  private String token;

  @Basic
  private Timestamp timestamp;

  private boolean isActive = true;
}
