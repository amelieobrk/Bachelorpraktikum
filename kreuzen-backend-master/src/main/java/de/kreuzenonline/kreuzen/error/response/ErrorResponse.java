package de.kreuzenonline.kreuzen.error.response;

import de.kreuzenonline.kreuzen.error.Error;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private Integer id;
    private Integer questionId;
    private Integer creatorId;
    private String comment;
    private String source;
    private Boolean isResolved;
    private Integer lastAssignedModeratorId;

    public ErrorResponse(Error error) {
        this.id = error.getId();
        this.questionId = error.getQuestionId();
        this.creatorId = error.getCreatorId();
        this.comment = error.getComment();
        this.source = error.getSource();
        this.isResolved = error.getIsResolved();
        this.lastAssignedModeratorId = error.getLastAssignedModeratorId();
    }


}
