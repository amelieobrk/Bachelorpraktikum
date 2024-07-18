package de.kreuzenonline.kreuzen.question.types.singleChoice;

import de.kreuzenonline.kreuzen.question.types.singleChoice.requests.CreateSingleChoiceRequest;
import de.kreuzenonline.kreuzen.question.types.singleChoice.requests.UpdateSingleChoiceRequest;

public interface SingleChoiceService {

    /**
     * Gets a single-choice question by the id of the base question.
     *
     * @param questionId id of the base question.
     * @return single-choice question.
     */
    SingleChoiceQuestion getByQuestionId(Integer questionId);

    /**
     * Creates a new single-choice question.
     *
     * @param request    contains information about answers and the position of the correct answer.
     * @param questionId id of the base question.
     * @return single-choice question.
     */
    SingleChoiceQuestion create(CreateSingleChoiceRequest request, Integer questionId);

    /**
     * Updates a single-choice question.
     *
     * @param request    contains information about answers and the position of the correct answer, all information is optional.
     * @param questionId id of the base question.
     * @return updated single-choice question.
     */
    SingleChoiceQuestion update(UpdateSingleChoiceRequest request, Integer questionId);


}
