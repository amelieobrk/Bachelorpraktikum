package de.kreuzenonline.kreuzen.question.types.multipleChoice.requests;

import de.kreuzenonline.kreuzen.question.requests.CreateQuestionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateMultipleChoiceRequest extends CreateQuestionRequest {

    @NotNull(message = "CreateMultipleChoiceRequest-answers-not-null")
    @Size(min = 3, message = "CreateMultipleChoiceRequest-answers-not-enough")
    @Size(max = 10, message = "CreateMultipleChoiceRequest-answers-too-many")
    private String[] answers;

    @NotNull(message = "CreateMultipleChoiceRequest-correct-answers-not-null")
    @Size(min = 1, message = "CreateMultipleChoiceRequest-correct-answers-not-enough")
    private Integer[] correctAnswerLocalIds;
}
