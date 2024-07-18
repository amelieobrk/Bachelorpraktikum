package de.kreuzenonline.kreuzen.question.types.assignment;

import de.kreuzenonline.kreuzen.question.types.assignment.requests.CreateAssignmentQuestionRequest;
import de.kreuzenonline.kreuzen.question.types.assignment.requests.UpdateAssignmentQuestionRequest;

public interface AssignmentQuestionService {

    /**
     * Gets an assignment question by the id of the base question.
     *
     * @param questionId id of the base question.
     * @return assignment question.
     */
    AssignmentQuestion getByQuestionId(Integer questionId);

    /**
     * Creates a new assignment question.
     *
     * @param request    contains information about identifiers and their assignments.
     * @param questionId id of the base question.
     * @return assignment question.
     */
    AssignmentQuestion create(CreateAssignmentQuestionRequest request, Integer questionId);

    /**
     * Updates an assignment question.
     *
     * @param request    contains information about identifiers and their assignments, all information is optional.
     * @param questionId id of the base question.
     * @return updated assignment question.
     */
    AssignmentQuestion update(UpdateAssignmentQuestionRequest request, Integer questionId);
}
