package de.kreuzenonline.kreuzen.auth.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetRequest {

    @Email(message = "PasswordResetRequest-email-invalid")
    @NotNull(message = "PasswordResetRequest-email-not-null")
    private String email;
}
