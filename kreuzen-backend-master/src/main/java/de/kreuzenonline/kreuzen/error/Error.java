package de.kreuzenonline.kreuzen.error;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("question_has_error")
public class Error {

    @Id
    private Integer id;
    private Integer questionId;
    private Integer creatorId;
    private String comment;
    private String source;
    private Boolean isResolved;
    private Integer lastAssignedModeratorId;


}
