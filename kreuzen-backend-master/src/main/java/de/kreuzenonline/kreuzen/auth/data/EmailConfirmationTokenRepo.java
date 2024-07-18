package de.kreuzenonline.kreuzen.auth.data;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailConfirmationTokenRepo extends CrudRepository<EmailConfirmationToken, Integer> {
    @Modifying
    @Query("INSERT INTO email_confirmation_token(user_id, token_hash) VALUES (:userId, :tokenHash) ON CONFLICT (user_id) DO UPDATE SET token_hash=:tokenHash")
    void upsert(@Param("userId") Integer userId, @Param("tokenHash") String tokenHash);
}
