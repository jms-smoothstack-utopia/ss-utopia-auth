package com.ss.utopia.auth.entity;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountActionToken {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "BINARY(16)")
  private UUID token;

  @NotNull
  @Column(columnDefinition = "BINARY(16)")
  private UUID ownerAccountId;

  @NotNull
  @Enumerated(EnumType.STRING)
  private AccountAction action;

  @Column(updatable = false)
  @CreationTimestamp
  private ZonedDateTime creation;

  @Builder.Default
  private boolean active = true;

}
