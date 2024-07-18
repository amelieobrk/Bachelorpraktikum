package de.kreuzenonline.kreuzen.question.types.singleChoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("question_single_choice")
public class SingleChoiceQuestionEntry {

    @Id
    private Integer id;
    private Integer questionId;
    private Integer correctAnswerLocalId;
}
