package de.kreuzenonline.kreuzen.session;

import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceQuestion;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceQuestion;
import de.kreuzenonline.kreuzen.session.responses.QuestionResultResponse;
import de.kreuzenonline.kreuzen.session.selections.MultipleChoiceSelection;
import de.kreuzenonline.kreuzen.session.selections.SingleChoiceSelection;

import java.util.List;

public interface SessionService {

    /**
     * Get a specific session by its id.
     *
     * @param id id of the session
     * @return session
     */
    Session getById(Integer id);

    /**
     * Get the session-question by session and local id.
     *
     * @param sessionId id of the session
     * @param localId   local id of the question within the session
     * @return SessionQuestion, contains basic information like answer time and whether the question is answered or not.
     */
    SessionQuestion getQuestionBySessionAndLocalId(Integer sessionId, Integer localId);

    /**
     * Creates a new session.
     *
     * @param name        name of the session
     * @param sessionType defines the type of the session, e.g. a practice or an exam session
     * @param isRandom    defines whether the questions that are contained in the session, will be displayed randomly or not
     * @param notes       optional space for text annotations of the session creator
     * @param moduleIds       id(s) of modules, to which questions may belong
     * @param semesterIds     id(s) of semesters, to which questions may belong
     * @param tagIds          id(s) of tags, to which questions may belong
     * @param questionTypes   question type(s) that may define the questions
     * @param questionOrigins question origin(s) that may appear in questions
     * @param textFilter      term that may be contained in questions
     * @return session
     */
    Session create(
            Integer creatorId,
            String name,
            String sessionType,
            Boolean isRandom,
            String notes,
            Integer[] moduleIds,
            Integer[] semesterIds,
            Integer[] tagIds,
            String[] questionTypes,
            String[] questionOrigins,
            String textFilter
    );

    /**
     * Updates a given session. All parameters are optional.
     *
     * @param id          id of the session
     * @param name        name of the session
     * @param sessionType defines the type of the session, e.g. a practice or an exam session
     * @param isRandom    defines whether the questions that are contained in the session, will be displayed randomly or not
     * @param notes       optional space for text annotations of the session creator
     * @return updated session
     */
    Session update(Integer id, String name, String sessionType, Boolean isRandom, String notes);

    /**
     * Deletes a session by its id.
     * Sessions can only be deleted by the creator and by administrators.
     *
     * @param id id of the session
     */
    void delete(Integer id);

    /**
     * Gets a single choice selection by its id.
     *
     * @param sessionId       id of the session
     * @param localQuestionId id of the question within the session
     * @return list of single choice selections.
     */
    Iterable<SingleChoiceSelection> findAllSingleChoiceSelections(Integer sessionId, Integer localQuestionId);

    /**
     * Gets a multiple choice selection by its id.
     *
     * @param sessionId       id of the session
     * @param localQuestionId id of the question within the session
     * @return list of multiple choice selections.
     */
    Iterable<MultipleChoiceSelection> findAllMultipleChoiceSelections(Integer sessionId, Integer localQuestionId);

    /**
     * Evaluates single choice question.
     *
     * @param sessionId id of the session
     * @param question  single choice question to evaluate
     * @param localId   id of question within session
     * @return question result response
     */
    QuestionResultResponse singleChoiceResult(Integer sessionId, SingleChoiceQuestion question, Integer localId);

    /**
     * Evaluates multiple choice question.
     *
     * @param sessionId id of the session
     * @param question  multiple choice question to evaluate
     * @param localId   id of question within session
     * @return question result response
     */
    QuestionResultResponse multipleChoiceResult(Integer sessionId, MultipleChoiceQuestion question, Integer localId);

    /**
     * Get all session questions that belong to a session.
     *
     * @param sessionId id of the session
     * @return List of questions
     */
    List<SessionQuestion> getAllSessionQuestions(Integer sessionId);

    /**
     * Resets answers of a session
     *
     * @param sessionId id of the session
     * @return updated session
     */
    Session resetSelection(Integer sessionId);

    /**
     * Marks a session finished.
     *
     * @param sessionId id of the session
     * @return updated session
     */
    Session finishSession(Integer sessionId);

    /**
     * Returns a list of session created by user
     *
     * @param userId id of the user
     * @param limit  max length of returned list
     * @param skip   amount of questions to skip
     * @return list of sessions
     */
    List<Session> getByPagination(Integer userId, Integer limit, Integer skip);

    /**
     * Counts amount of sessions created by user
     *
     * @param userId Id of user
     * @return Amount of sessions
     */
    Integer getCountByUser(Integer userId);

    /**
     * Adds answer of single choice question from user
     *
     * @param sessionId             Id of session
     * @param localId               local id of the question within the session
     * @param checkedLocalAnswerId  Contains id of checked answer
     * @param crossedLocalAnswerIds Contains id's of crossed answers
     */
    void addSingleChoiceSelection(Integer sessionId, Integer localId, Integer checkedLocalAnswerId, Integer[] crossedLocalAnswerIds);

    /**
     * Adds answer of multiple choice question from user
     *
     * @param sessionId             Id of session
     * @param localId               local id of the question within the session
     * @param checkedLocalAnswerIds Contains id's of checked answer
     * @param crossedLocalAnswerIds Contains id's of crossed answers
     */
    void addMultipleChoiceSelection(Integer sessionId, Integer localId, Integer[] checkedLocalAnswerIds, Integer[] crossedLocalAnswerIds);

    /**
     * Adds time to session
     *
     * @param sessionId  Id of session
     * @param localId    Id of question in session
     * @param answerTime Time the user needed to answer the question
     */
    SessionQuestion addTime(Integer sessionId, Integer localId, Integer answerTime);

    /**
     * Counts amount of questions that match certain criteria. All of them are optional.
     *
     * @param moduleIds       id(s) of modules, to which questions may belong
     * @param semesterIds     id(s) of semesters, to which questions may belong
     * @param tagIds          id(s) of tags, to which questions may belong
     * @param questionTypes   question type(s) that may define the questions
     * @param questionOrigins question origin(s) that may appear in questions
     * @param textFilter      term that may be contained in questions
     * @return count of matching questions
     */
    Integer getCountByParameters(Integer[] moduleIds, Integer[] semesterIds, Integer[] tagIds, String[] questionTypes, String[] questionOrigins, String textFilter);

    /**
     * Counts amount of questions in a session.
     *
     * @param id Session id
     * @return count of questions
     */
    Integer getCount(Integer id);

    /**
     * Submit a question. After that it is marked as answered.
     *
     * @param sessionId id of the session
     * @param localId   local id of the question within the session
     * @return the submitted question
     */
    SessionQuestion submitQuestion(Integer sessionId, Integer localId);
}
