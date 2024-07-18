package de.kreuzenonline.kreuzen.auth.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmEmailRequest {

    @NotNull(message = "ConfirmEmailRequest-token-not-null")
    private String token;
}
