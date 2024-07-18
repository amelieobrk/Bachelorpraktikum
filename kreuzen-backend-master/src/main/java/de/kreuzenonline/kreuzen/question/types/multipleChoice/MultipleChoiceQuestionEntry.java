package de.kreuzenonline.kreuzen.question.types.multipleChoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("question_multiple_choice")
public class MultipleChoiceQuestionEntry {

    @Id
    private Integer id;
    private Integer questionId;
    private Integer[] correctAnswerLocalIds;
}
