package de.kreuzenonline.kreuzen.question.types.assignment;

import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.responses.BaseQuestionResponse;
import de.kreuzenonline.kreuzen.question.types.assignment.responses.AssignmentQuestionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentQuestion extends BaseQuestion {

    /**
     * An assignment question wants the user to assign a set of answers (e.g. different molecules) to a set of identifiers (e.g. the letters A - F).
     * Both identifiers and answers are linked to the base question and have an own local id which makes it possible to store the order of the identifiers/answers.
     * AssignmentQuestion contains the whole question whereas AssignmentQuestionEntry just contains the information that extend the base question.
     * AssignmentQuestionEntry is the object that will be saved to the database.
     **/

    private List<AssignmentIdentifier> identifiers;
    private List<AssignmentAnswer> answers;

    public AssignmentQuestion(Integer id, String text, String type, String additionalInformation, Integer points, Integer examId, Integer courseId, Integer creatorId, Integer updaterId, String origin, Boolean isApproved, List<AssignmentIdentifier> identifiers, List<AssignmentAnswer> answers) {
        super(id, text, type, additionalInformation, points, examId, courseId, creatorId, updaterId, origin, isApproved);

        this.identifiers = identifiers;
        this.answers = answers;
    }

    @Override
    public BaseQuestionResponse toResponse() {
        return new AssignmentQuestionResponse(this);
    }
}
