package de.kreuzenonline.kreuzen.session.responses;

import de.kreuzenonline.kreuzen.session.SessionQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionQuestionResponse {

    private Integer sessionId;
    private Integer questionId;
    private Integer localId;
    private Integer time;
    private Boolean isSubmitted;

    public SessionQuestionResponse(SessionQuestion sessionQuestion) {
        this.sessionId = sessionQuestion.getSessionId();
        this.questionId = sessionQuestion.getQuestionId();
        this.localId = sessionQuestion.getLocalId();
        this.time = sessionQuestion.getTime();
        this.isSubmitted = sessionQuestion.getIsSubmitted();
    }

}
