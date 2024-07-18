package de.kreuzenonline.kreuzen.auth.responses;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponse {

    private Integer id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean emailConfirmed;
    private String role;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer universityId;

    public UserDetailsResponse(CustomUserDetails user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.emailConfirmed = user.isEmailConfirmed();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.universityId = user.getUniversityId();
    }
}
