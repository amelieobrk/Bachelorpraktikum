package de.kreuzenonline.kreuzen.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("question_has_comment")
public class Comment {

    @Id
    private Integer id;
    private Integer questionId;
    private Integer creatorId;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;

}