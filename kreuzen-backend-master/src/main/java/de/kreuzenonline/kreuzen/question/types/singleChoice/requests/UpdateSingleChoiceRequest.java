package de.kreuzenonline.kreuzen.question.types.singleChoice.requests;

import de.kreuzenonline.kreuzen.question.requests.UpdateQuestionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateSingleChoiceRequest extends UpdateQuestionRequest {

    @Size(min = 2, message = "CreateSingleChoiceRequest-answers-not-enough")
    @Size(max = 10, message = "CreateSingleChoiceRequest-answers-too-many")
    private String[] answers;

    @Min(value = 1, message = "CreateSingleChoiceRequest-correct-answer-not-so-low")
    private Integer correctAnswerLocalId;
}
