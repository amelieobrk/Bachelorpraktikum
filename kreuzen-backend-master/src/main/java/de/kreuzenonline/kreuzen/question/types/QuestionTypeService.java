package de.kreuzenonline.kreuzen.question.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.exceptions.BadRequestException;
import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.requests.CreateQuestionRequest;
import de.kreuzenonline.kreuzen.question.requests.UpdateQuestionRequest;

import java.util.ResourceBundle;

public abstract class QuestionTypeService<T extends BaseQuestion, C extends CreateQuestionRequest, U extends UpdateQuestionRequest> {

    private final Class<C> createQuestionRequestType;
    private final Class<U> updateQuestionRequestType;
    private final ResourceBundle resourceBundle;

    public QuestionTypeService(Class<C> createQuestionRequestType, Class<U> updateQuestionRequestType, ResourceBundle resourceBundle) {
        this.createQuestionRequestType = createQuestionRequestType;
        this.updateQuestionRequestType = updateQuestionRequestType;
        this.resourceBundle = resourceBundle;
    }

    /**
     * Get a specific question by its id.
     * T is for question type so the function will deliver the full question and not just the base question.
     *
     * @param questionId id of the base question.
     * @return type question.
     */
    abstract public T getByQuestionId(Integer questionId);

    /**
     * Create a new question of any type.
     * In a first step the base question will be created.
     * In a second step the genericCreate-function below will map the universal HTTP request body onto the respective question type request.
     * In this step the QuestionTypeService will save the type-specific information for each question type.
     *
     * @param request the respective request that extends the CreateQuestionRequest, e.g. a CreateSingleChoiceRequest.
     * @param id      id of the question that will be set by the BaseQuestionService.
     * @return type question.
     */
    abstract public T create(C request, Integer id);

    /**
     * This function identifies which question type is needed.
     * It maps the universal HTTP request body to the respective question type request and calls the create-function with it.
     *
     * @param m    objectMapper
     * @param body full HTTP request, that has to be mapped.
     * @param id   id of the base question that will be set by the BaseQuestionService.
     * @return type question.
     */
    public T genericCreate(ObjectMapper m, String body, Integer id) {

        try {
            C request = m.readValue(body, createQuestionRequestType);
            return this.create(request, id);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(resourceBundle.getString("QuestionTypeService-bad-create-request"));
        }
    }

    /**
     * Update a question of any type.
     * In a first step the existing base question will be called.
     * In a second step the genericUpdate-function below will map the universal HTTP request body onto the respective question type request.
     * In this step the QuestionTypeService will update the type-specific information for each question type.
     *
     * @param request the respective request thant extends the UpdateQuestionRequest, e.g. an UpdateSingleChoiceRequest.
     * @param id      id of the question that will be set by the BaseQuestionService.
     * @return updated type question
     */
    abstract public T update(U request, Integer id);

    /**
     * This function identifies which question type is needed.
     * It maps the universal HTTP request body to the respective question type request and calls the update-function with it.
     *
     * @param m    objectMapper
     * @param body full HTTP request, that has to be mapped.
     * @param id   id of the base question that will be set by the BaseQuestionService.
     * @return updated type question
     */
    public T genericUpdate(ObjectMapper m, String body, Integer id) {

        try {
            U request = m.readValue(body, updateQuestionRequestType);
            return this.update(request, id);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(resourceBundle.getString("QuestionTypeService-bad-update-request"));
        }
    }
}
