package de.kreuzenonline.kreuzen.auth.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    @NotNull(message = "RegistrationRequest-username-not-null")
    @Size(min = 3, message = "RegistrationRequest-username-min-length")
    @Size(max = 64, message = "RegistrationRequest-username-max-length")
    @javax.validation.constraints.Pattern(regexp = "^[A-Za-z0-9]*$", message = "RegistrationRequest-username-pattern")
    private String username;
    @NotNull(message = "RegistrationRequest-first-name-not-null")
    @Size(min = 1, message = "RegistrationRequest-first-name-min-length")
    @Size(max = 64, message = "RegistrationRequest-first-name-max-length")
    private String firstName;
    @NotNull(message = "RegistrationRequest-last-name-not-null")
    @Size(min = 1, message = "RegistrationRequest-last-name-min-length")
    @Size(max = 64, message = "RegistrationRequest-last-name-max-length")
    private String lastName;
    @Email(message = "RegistrationRequest-email-invalid")
    @NotNull(message = "RegistrationRequest-email-not-null")
    private String email;
    @NotNull(message = "RegistrationRequest-password-not-null")
    @Size(min = 8, message = "RegistrationRequest-password-min-length")
    @Size(max = 512, message = "RegistrationRequest-password-max-length")
    private String password;
    @NotNull(message = "RegistrationRequest-university-id-not-null")
    private Integer universityId;

    private Integer[] majors;
    private Integer[] majorSections;
}
