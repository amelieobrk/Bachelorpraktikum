package de.kreuzenonline.kreuzen.question.types;

import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.requests.CreateQuestionRequest;
import de.kreuzenonline.kreuzen.question.requests.UpdateQuestionRequest;

import java.util.Optional;

public interface QuestionTypeMapperService {

    /**
     * Starts the respective question type service that is needed. type is set as a string in each QuestionTypeServiceImpl-class.
     * Every question type extends the base question by specific parameters.
     * To fulfill the needed functionality the respective question type service has to use its extended version of create- and update request as well.
     *
     * @param type type of the question.
     * @return the question type service.
     */
    Optional<QuestionTypeService<? extends BaseQuestion, ? extends CreateQuestionRequest, ? extends UpdateQuestionRequest>> getServiceByType(String type);
}
