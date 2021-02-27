package com.ss.utopia.auth.repository;

import com.ss.utopia.auth.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
  Optional<PasswordReset> findByEmail(String email);
  Optional<PasswordReset> findByToken(String token);
}
