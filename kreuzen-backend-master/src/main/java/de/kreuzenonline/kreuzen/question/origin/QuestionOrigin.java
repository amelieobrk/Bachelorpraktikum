package de.kreuzenonline.kreuzen.question.origin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("question_origins")

public class QuestionOrigin {

    /**
     * A question origin is used to show what's a question's origin, e.g. whether it's from an actual exam or a student's own question.
     * Goal is to make sure to use only a specified set of question origins when new questions are created.
     **/

    @Id
    private String name;
    private String displayName;
}
