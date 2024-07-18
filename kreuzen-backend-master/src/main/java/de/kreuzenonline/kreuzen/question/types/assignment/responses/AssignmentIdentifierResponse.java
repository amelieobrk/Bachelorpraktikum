package de.kreuzenonline.kreuzen.question.types.assignment.responses;

import de.kreuzenonline.kreuzen.question.types.assignment.AssignmentIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentIdentifierResponse {

    private Integer localId;
    private String identifier;
    private Integer correctAnswerLocalId;

    public AssignmentIdentifierResponse(AssignmentIdentifier identifier) {
        this.localId = identifier.getLocalId();
        this.identifier = identifier.getIdentifier();
        this.correctAnswerLocalId = identifier.getCorrectAnswerLocalId();
    }
}
