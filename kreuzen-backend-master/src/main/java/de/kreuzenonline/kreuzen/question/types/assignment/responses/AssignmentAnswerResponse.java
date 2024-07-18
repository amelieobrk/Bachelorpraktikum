package de.kreuzenonline.kreuzen.question.types.assignment.responses;

import de.kreuzenonline.kreuzen.question.types.assignment.AssignmentAnswer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentAnswerResponse {

    private Integer localId;
    private String answer;

    public AssignmentAnswerResponse(AssignmentAnswer answer) {
        this.localId = answer.getLocalId();
        this.answer = answer.getAnswer();
    }
}
