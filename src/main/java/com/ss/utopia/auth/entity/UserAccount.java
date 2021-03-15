package com.ss.utopia.auth.entity;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAccount implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @NotBlank
  @Email
  private String email;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @NotBlank
  private String password;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private UserRole userRole = UserRole.DEFAULT;

  @Column(updatable = false)
  @CreationTimestamp
  private ZonedDateTime creationDateTime;

  @UpdateTimestamp
  private ZonedDateTime lastModifiedDateTime;

  @Builder.Default
  private boolean accountNonExpired = true;
  @Builder.Default
  private boolean accountNonLocked = true;
  @Builder.Default
  private boolean credentialsNonExpired = true;
  @Builder.Default
  private boolean enabled = true;
  @Builder.Default
  private boolean confirmed = false;

  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (userRole == null) {
      return Collections.emptySet();
    }
    return Set.of(new SimpleGrantedAuthority(userRole.getRole()));
  }

  @Override
  public String getUsername() {
    return email;
  }

}
