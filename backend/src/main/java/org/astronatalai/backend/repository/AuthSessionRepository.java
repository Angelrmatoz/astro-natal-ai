package org.astronatalai.backend.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.astronatalai.backend.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByTokenId(String tokenId);

    boolean existsByTokenIdAndRevokedAtIsNullAndExpiresAtAfter(String tokenId, Instant now);
}
