package de.kreuzenonline.kreuzen.module.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteModuleRequest {

    private Integer id;

    @NotNull(message = "DeleteModuleRequest-password-not-null")
    private String password;
}
