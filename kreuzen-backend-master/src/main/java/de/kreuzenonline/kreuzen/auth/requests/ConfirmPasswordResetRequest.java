package de.kreuzenonline.kreuzen.auth.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmPasswordResetRequest {

    @NotNull(message = "ConfirmPasswordResetRequest-token-not-null")
    private String token;
    @NotNull(message = "ConfirmPasswordResetRequest-password-not-null")
    @Size(min = 8, message = "ConfirmPasswordResetRequest-password-min-length")
    @Size(max = 512, message = "ConfirmPasswordResetRequest-password-max-length")
    private String newPassword;
}
