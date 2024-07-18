package de.kreuzenonline.kreuzen.auth.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminConfirmEmailRequest {

    @NotNull(message = "AdminConfirmEmailRequest-user-id-not-null")
    private Integer userId;
}
