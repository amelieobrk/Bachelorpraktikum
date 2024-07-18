package de.kreuzenonline.kreuzen.comment.responses;


import de.kreuzenonline.kreuzen.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {

    private Integer id;
    private Integer questionId;
    private Integer creatorId;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.questionId= comment.getQuestionId();
        this.creatorId = comment.getCreatorId();
        this.comment = comment.getComment();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}
