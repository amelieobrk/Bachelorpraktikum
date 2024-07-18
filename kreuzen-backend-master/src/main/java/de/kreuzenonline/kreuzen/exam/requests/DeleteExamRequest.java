package de.kreuzenonline.kreuzen.exam.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteExamRequest {


    private Integer id;

    @NotNull(message = "DeleteExamRequest-password-not-null")
    private String password;
}
