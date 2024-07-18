package de.kreuzenonline.kreuzen.question.types.singleChoice.responses;

import de.kreuzenonline.kreuzen.question.responses.BaseQuestionResponse;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceQuestion;
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
public class SingleChoiceQuestionResponse extends BaseQuestionResponse {

    private List<SingleChoiceAnswerResponse> answers;
    private Integer correctAnswerLocalId;

    public SingleChoiceQuestionResponse(SingleChoiceQuestion question) {
        super(question);
        this.answers = question.getAnswers().stream().map(SingleChoiceAnswerResponse::new).collect(Collectors.toList());
        this.correctAnswerLocalId = question.getCorrectAnswerLocalId();
    }
}
