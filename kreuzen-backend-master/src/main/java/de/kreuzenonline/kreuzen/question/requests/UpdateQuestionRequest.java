package de.kreuzenonline.kreuzen.question.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateQuestionRequest {

    private String text;

    private String additionalInformation;

    private Integer points;

    private Integer examId;

    private Integer courseId;

    private String origin;

    private Byte[] file;
}
