package de.kreuzenonline.kreuzen.question.types.multipleChoice;


import de.kreuzenonline.kreuzen.question.types.multipleChoice.requests.CreateMultipleChoiceRequest;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.requests.UpdateMultipleChoiceRequest;

public interface MultipleChoiceService {

    /**
     * Gets a multiple-choice question by the id of the base question.
     *
     * @param questionId id of the base question.
     * @return multiple-choice question.
     */
    MultipleChoiceQuestion getByQuestionId(Integer questionId);

    /**
     * Creates a new multiple-choice question.
     *
     * @param request    contains information about answers and the positions of the correct answers.
     * @param questionId id of the base question.
     * @return multiple-choice question.
     */
    MultipleChoiceQuestion create(CreateMultipleChoiceRequest request, Integer questionId);

    /**
     * Updates a multiple-choice question.
     *
     * @param request    contains information about answers and the positions of the correct answers, all information is optional.
     * @param questionId id of the base question.
     * @return updated multiple-choice question.
     */
    MultipleChoiceQuestion update(UpdateMultipleChoiceRequest request, Integer questionId);


}
