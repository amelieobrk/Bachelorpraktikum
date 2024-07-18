package de.kreuzenonline.kreuzen.session.responses;

import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.responses.BaseQuestionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResultResponse {


    private Integer sessionId;
    private BaseQuestionResponse question;
    private Integer points;
    private Integer localId;

    public QuestionResultResponse(Integer sessionId, BaseQuestion question, Integer points, Integer localId) {
        this.sessionId = sessionId;
        this.question = question.toResponse();
        this.points = points;
        this.localId = localId;
    }
}
