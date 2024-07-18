package de.kreuzenonline.kreuzen.semester.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteSemesterRequest {
    private Integer id;

    @NotNull(message = "DeleteSemesterRequest-password-not-null")
    private String password;
}
