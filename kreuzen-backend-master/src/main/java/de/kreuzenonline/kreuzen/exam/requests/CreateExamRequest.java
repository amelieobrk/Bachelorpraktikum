package de.kreuzenonline.kreuzen.exam.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.LocalDate;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateExamRequest {

    @NotNull(message="CreateExamRequest-name-not-null")
    @Size(min = 3, message = "CreateExamRequest-name-too-short")
    @Size(max = 64, message = "CreateExamRequest-name-too-long")
    private String name;



    @NotNull(message = "CreateExamRequest-date-not-null")
    @PastOrPresent(message = "CreateExamRequest-date-not-past")
    private LocalDate date;

    @NotNull(message="CreateExamRequest-isRetry-not-null")
    private Boolean isRetry;



}
