package de.kreuzenonline.kreuzen.question.types.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("question_assignment_identifier")
public class AssignmentIdentifier {

    @Id
    private Integer id;
    private Integer questionId;
    private Integer localId;
    private String identifier;
    private Integer correctAnswerLocalId;
}
