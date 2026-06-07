package org.autostock.repositories;

import org.autostock.models.PasswordResetToken;
import org.autostock.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByUserAndCodeAndUsedFalseAndExpiresAtAfter(
            User user, String code, LocalDateTime now
    );

    void deleteByUser(User user);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
