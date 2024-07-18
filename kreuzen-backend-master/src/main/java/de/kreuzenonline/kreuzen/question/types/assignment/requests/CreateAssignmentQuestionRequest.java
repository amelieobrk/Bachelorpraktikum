package de.kreuzenonline.kreuzen.question.types.assignment.requests;

import de.kreuzenonline.kreuzen.question.requests.CreateQuestionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateAssignmentQuestionRequest extends CreateQuestionRequest {

    private String[] identifiers;

    private String[] answers;

    private Integer[] correctAssignmentIds;
}
