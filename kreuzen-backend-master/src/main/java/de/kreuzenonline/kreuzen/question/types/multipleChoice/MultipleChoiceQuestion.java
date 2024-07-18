package de.kreuzenonline.kreuzen.question.types.multipleChoice;

import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.responses.BaseQuestionResponse;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.responses.MultipleChoiceQuestionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultipleChoiceQuestion extends BaseQuestion {

    /**
     * A multiple choice question wants the user to mark at least one, but most probably more than one correct answer from a set of possible answers.
     * It extends the base question by the set of answers and a set of integers that points at the local ids of the correct answers.
     *
     * MultipleQuestion contains the whole question whereas MultipleQuestionEntry just contains the information that extend the base question.
     * MultipleQuestionEntry is the object that will be saved to the database.
     */

    private List<MultipleChoiceAnswer> answers;
    private Integer[] correctAnswerLocalIds;

    public MultipleChoiceQuestion(Integer id, String text, String type, String additionalInformation, Integer points, Integer examId, Integer courseId, Integer creatorId, Integer updaterId, String origin, Boolean isApproved, Integer[] correctAnswerLocalIds, List<MultipleChoiceAnswer> answers) {
        super(id, text, type, additionalInformation, points, examId, courseId, creatorId, updaterId, origin, isApproved);

        this.answers = answers;
        this.correctAnswerLocalIds = correctAnswerLocalIds;
    }

    @Override
    public BaseQuestionResponse toResponse() {
        return new MultipleChoiceQuestionResponse(this);
    }
}
