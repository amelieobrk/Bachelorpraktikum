package de.kreuzenonline.kreuzen.user.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UpdateUserRequest {
    @Size(min = 3, message = "UpdateUserRequest-username-too-short")
    @Size(max = 64, message = "UpdateUserRequest-username-too-long")
    @javax.validation.constraints.Pattern(regexp = "^[A-Za-z0-9]*$", message = "Only alpha-numerical names are allowed in the username.")
    private String newUsername;

    @Size(min = 1, message = "UpdateUserRequest-firstname-too-short")
    @Size(max = 64, message = "UpdateUserRequest-firstname-too-long")
    private String newFirstName;
    @Size(min = 1, message = "UpdateUserRequest-lastname-too-short")
    @Size(max = 64, message = "UpdateUserRequest-lastname-too-long")
    private String newLastName;

    private int id;

    private Integer newUniversityId;

    private String oldPassword;

    @Size(min = 8, message = "UpdateUserRequest-password-too-short")
    private String newPassword;

    @Email
    private String newEmail;

    private String newRole;

    private Boolean newLocked;
}
