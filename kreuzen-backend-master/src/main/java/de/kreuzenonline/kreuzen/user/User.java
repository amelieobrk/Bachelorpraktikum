package de.kreuzenonline.kreuzen.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("app_user")
public class User {
    @Id
    private Integer id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private String role;
    private boolean emailConfirmed;
    private boolean locked;
    private Integer universityId;
    private Instant createdAt;
    private Instant updatedAt;
}
