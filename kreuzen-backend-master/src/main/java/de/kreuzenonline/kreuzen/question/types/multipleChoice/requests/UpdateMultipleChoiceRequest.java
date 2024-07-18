package de.kreuzenonline.kreuzen.question.types.multipleChoice.requests;

import de.kreuzenonline.kreuzen.question.requests.UpdateQuestionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateMultipleChoiceRequest extends UpdateQuestionRequest {

    @Size(min = 3, message = "CreateMultipleChoiceRequest-answers-not-enough")
    @Size(max = 10, message = "CreateMultipleChoiceRequest-answers-too-many")
    private String[] answers;

    @Size(min = 2, message = "CreateMultipleChoiceRequest-correct-answers-not-enough")
    private Integer[] correctAnswerLocalIds;
}
