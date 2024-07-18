package de.kreuzenonline.kreuzen.question.types.multipleChoice.responses;

import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceAnswer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultipleChoiceAnswerResponse {

    private String text;
    private Integer localId;
    private Integer id;

    public MultipleChoiceAnswerResponse(MultipleChoiceAnswer answer) {
        this.text = answer.getText();
        this.localId = answer.getLocalId();
        this.id = answer.getId();
    }
}
