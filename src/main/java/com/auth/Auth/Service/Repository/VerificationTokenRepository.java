package com.auth.Auth.Service.Repository;

import com.auth.Auth.Service.Entity.RefreshToken;
import com.auth.Auth.Service.Entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserUsernameAndType(String username, String type);
}
