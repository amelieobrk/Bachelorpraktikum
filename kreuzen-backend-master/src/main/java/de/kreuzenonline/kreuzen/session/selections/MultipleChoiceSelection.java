package de.kreuzenonline.kreuzen.session.selections;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("session_multiple_choice_selection")
public class MultipleChoiceSelection {

    @Id
    private Integer id;
    private Integer sessionId;
    private Integer localQuestionId;
    private Integer localAnswerId;
    private Boolean isChecked;
    private Boolean isCrossed;
}
