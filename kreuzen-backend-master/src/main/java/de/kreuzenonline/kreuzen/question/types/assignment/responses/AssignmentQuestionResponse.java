package de.kreuzenonline.kreuzen.question.types.assignment.responses;

import de.kreuzenonline.kreuzen.question.responses.BaseQuestionResponse;
import de.kreuzenonline.kreuzen.question.types.assignment.AssignmentQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentQuestionResponse extends BaseQuestionResponse {

    private List<AssignmentIdentifierResponse> identifiers;
    private List<AssignmentAnswerResponse> answers;

    public AssignmentQuestionResponse(AssignmentQuestion question) {
        super(question);
        this.identifiers = question.getIdentifiers().stream().map(AssignmentIdentifierResponse::new).collect(Collectors.toList());
        this.answers = question.getAnswers().stream().map(AssignmentAnswerResponse::new).collect(Collectors.toList());
    }
}
