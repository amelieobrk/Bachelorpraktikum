package de.kreuzenonline.kreuzen.question.types.multipleChoice.responses;

import de.kreuzenonline.kreuzen.question.responses.BaseQuestionResponse;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultipleChoiceQuestionResponse extends BaseQuestionResponse {

    private List<MultipleChoiceAnswerResponse> answers;
    private Integer[] correctAnswerLocalIds;

    public MultipleChoiceQuestionResponse(MultipleChoiceQuestion question) {
        super(question);
        this.answers = question.getAnswers().stream().map(MultipleChoiceAnswerResponse::new).collect(Collectors.toList());
        this.correctAnswerLocalIds = question.getCorrectAnswerLocalIds();
    }
}
