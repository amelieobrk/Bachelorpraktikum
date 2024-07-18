package de.kreuzenonline.kreuzen.exam.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateExamRequest {


    @Size(min = 3, message = "CreateExamRequest-name-too-short")
    @Size(max = 64, message = "CreateExamRequest-name-too-long")
    private String name;


    private Integer courseId;


    @PastOrPresent(message = "CreateExamRequest-date-not-past")
    private LocalDate date;

    private Boolean isComplete;

    private Boolean isRetry;
}
