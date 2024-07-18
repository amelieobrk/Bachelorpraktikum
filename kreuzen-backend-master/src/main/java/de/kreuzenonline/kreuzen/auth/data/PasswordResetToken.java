package de.kreuzenonline.kreuzen.auth.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("password_reset_token")
public class PasswordResetToken {

    @Id
    private Integer userId;
    private String tokenHash;
    private Instant expiresAt;
}
