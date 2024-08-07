package de.kreuzenonline.kreuzen.auth.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResendEmailConfirmationRequest {

    @Email(message = "ResendEmailConfirmationRequest-email-invalid")
    @NotNull(message = "ResendEmailConfirmationRequest-email-not-null")
    private String email;
}
