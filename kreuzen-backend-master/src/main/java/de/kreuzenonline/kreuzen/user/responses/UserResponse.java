package de.kreuzenonline.kreuzen.user.responses;

import de.kreuzenonline.kreuzen.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponse {

    private Integer id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private int universityId;
    private boolean emailConfirmed;
    private boolean locked;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.emailConfirmed = user.isEmailConfirmed();
        this.locked = user.isLocked();
        this.firstName = user.getFirstName();
        this.universityId = user.getUniversityId();
        this.lastName = user.getLastName();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
