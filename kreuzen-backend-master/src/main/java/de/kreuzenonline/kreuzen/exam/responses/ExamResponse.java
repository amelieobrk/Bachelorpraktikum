package de.kreuzenonline.kreuzen.exam.responses;

import de.kreuzenonline.kreuzen.exam.Exam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamResponse {

    private Integer id;
    private String name;
    private Integer courseId;
    private LocalDate date;
    private Boolean isComplete;
    private Boolean isRetry;

    public ExamResponse(Exam exam) {
        this.id = exam.getId();
        this.name = exam.getName();
        this.courseId = exam.getCourseId();
        this.date = exam.getDate();
        this.isComplete = exam.getIsComplete();
        this.isRetry=exam.getIsRetry();
    }

}
