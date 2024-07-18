package de.kreuzenonline.kreuzen.question.types.singleChoice.responses;

import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceAnswer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleChoiceAnswerResponse {

    private String text;
    private Integer localId;
    private Integer id;

    public SingleChoiceAnswerResponse(SingleChoiceAnswer answer) {
        this.text = answer.getText();
        this.localId = answer.getLocalId();
        this.id = answer.getId();
    }
}
