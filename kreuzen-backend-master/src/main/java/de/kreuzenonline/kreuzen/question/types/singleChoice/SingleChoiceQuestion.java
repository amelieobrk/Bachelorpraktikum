package de.kreuzenonline.kreuzen.question.types.singleChoice;

import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.responses.BaseQuestionResponse;
import de.kreuzenonline.kreuzen.question.types.singleChoice.responses.SingleChoiceQuestionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleChoiceQuestion extends BaseQuestion {

    /**
     * A single choice question wants the user to exactly one correct answer from a set of possible answers.
     * It extends the base question by the set of answers and an integer that points at the local id of the correct answer.
     *
     * SingleQuestion contains the whole question whereas SingleQuestionEntry just contains the information that extend the base question.
     * SingleQuestionEntry is the object that will be saved to the database.
     */

    private List<SingleChoiceAnswer> answers;
    private Integer correctAnswerLocalId;

    public SingleChoiceQuestion(Integer id, String text, String type, String additionalInformation, Integer points, Integer examId, Integer courseId, Integer creatorId, Integer updaterId, String origin, Boolean isApproved, Integer correctAnswerLocalId, List<SingleChoiceAnswer> answers) {
        super(id, text, type, additionalInformation, points, examId, courseId, creatorId, updaterId, origin, isApproved);

        this.answers = answers;
        this.correctAnswerLocalId = correctAnswerLocalId;
    }

    @Override
    public BaseQuestionResponse toResponse() {
        return new SingleChoiceQuestionResponse(this);
    }
}
