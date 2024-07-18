package de.kreuzenonline.kreuzen.question.types.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("question_assignment")
public class AssignmentQuestionEntry {

    @Id
    private Integer id;
    private Integer questionId;
}
