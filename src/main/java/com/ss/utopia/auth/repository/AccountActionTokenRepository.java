package com.ss.utopia.auth.repository;

import com.ss.utopia.auth.entity.AccountActionToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountActionTokenRepository extends JpaRepository<AccountActionToken, UUID> {

}
