package de.kreuzenonline.kreuzen.question.types.assignment.requests;

import de.kreuzenonline.kreuzen.question.requests.UpdateQuestionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateAssignmentQuestionRequest extends UpdateQuestionRequest {

    private String[] identifiers;

    private String[] answers;

    private Integer[] correctAssignmentIds;
}
