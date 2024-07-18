package de.kreuzenonline.kreuzen.auth.data;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PasswordResetTokenRepo extends CrudRepository<PasswordResetToken, Integer> {

    @Modifying
    @Query("INSERT INTO password_reset_token(user_id, token_hash, expires_at) VALUES (:userId, :tokenHash, :expiresAt) ON CONFLICT (user_id) DO UPDATE SET token_hash=:tokenHash, expires_at=:expiresAt")
    void upsert(@Param("userId") Integer userId, @Param("tokenHash") String tokenHash, @Param("expiresAt") Instant expiresAt);
}
