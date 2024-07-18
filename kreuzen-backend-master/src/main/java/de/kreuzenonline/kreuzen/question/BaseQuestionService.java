package de.kreuzenonline.kreuzen.question;

import org.springframework.lang.Nullable;

import java.util.List;

public interface BaseQuestionService {

    /**
     * Gets a question by its id.
     *
     * @param id id of the question.
     * @return question.
     */
    BaseQuestion getById(Integer id);

    /**
     * Creates a question with the following information:
     *
     * @param text                  the question itself
     * @param type                  type of the question, e.g. single-choice
     * @param additionalInformation optional additional information, e.g. an URL or another source.
     * @param points                number of points that are granted for the correct answer.
     * @param examId                optional: id of an exam where the question belongs to.
     * @param courseId              id of the course that the question belongs to.
     * @param creatorId             id of the user that originally created the question.
     * @param origin                origin of the question, e.g. originally taken from an exam.
     * @param file                  optional: file, saved as a byte array
     * @return the created question.
     */
    BaseQuestion create(String text, String type, String additionalInformation, Integer points, Integer examId, Integer courseId, Integer creatorId, String origin, Byte[] file);

    /**
     * Updates a question with the following information:
     *
     * @param id                    the id of the question that shall be updated.
     * @param text                  the question itself.
     * @param additionalInformation optional additional information, e.g. an URL or another source.
     * @param points                number of points that are granted for the correct answer.
     * @param examId                optional: id of an exam where the question belongs to.
     * @param courseId              id of the course that the question belongs to.
     * @param origin                origin of the question, e.g. originally taken from an exam.
     * @param file                  optional: file, saved as a byte array
     * @param updaterId             id of the user that updates the question.
     * @return the updated question.
     */

    BaseQuestion update(Integer id, String text, String additionalInformation, Integer points, Integer examId, Integer courseId, String origin, Byte[] file, Integer updaterId);

    /**
     * A question has to be approved by moderators/administrators before it is visible for all users.
     *
     * @param id id of the question that shall be approved.
     * @return the approved question.
     */
    BaseQuestion approve(Integer id);

    /**
     * Gets called, when a question is updated by the creator. After that it has to be approved by moderators/administrators once again.
     *
     * @param id id of the question that shall be disapproved.
     */
    void disapprove(Integer id);

    /**
     * Deletes a question by its id.
     *
     * @param questionId id of the question that shall be deleted.
     */
    void delete(Integer questionId);

    /**
     * Finds all questions that are contained in one specific exam.
     *
     * @param examId  id of the exam.
     * @param isAdmin decides whether the user is eligible to see unapproved questions or not.
     * @return list of questions.
     */
    Iterable<BaseQuestion> findAllByExam(Integer examId, Boolean isAdmin);

    /**
     * Finds all questions that are linked to a specific course.
     *
     * @param courseId id of the course.
     * @param isAdmin  decides whether the user is eligible to see unapproved questions or not.
     * @return list of questions.
     */
    Iterable<BaseQuestion> findAllByCourse(Integer courseId, Boolean isAdmin);

    /**
     * Returns a list of questions using limit and skip ordered by question id. Only questions that contain the search term are listed.
     *
     * @param isAdmin    shows whether user is authorized to see non-approved questions or not.
     * @param searchTerm search term
     * @param limit      max length of returned list
     * @param skip       amount of questions to skip
     * @return list of questions
     */
    List<BaseQuestion> getByPagination(
            @Nullable Boolean onlyApproved,
            @Nullable String searchTerm,
            @Nullable Integer semesterId,
            @Nullable Integer moduleId,
            @Nullable Integer courseId,
            @Nullable Integer examId,
            @Nullable Integer tagId,
            int limit,
            int skip,
            Boolean isAdmin
    );

    /**
     * Count the amount of registered questions that contain the search term.
     *
     * @param searchTerm the search term
     * @param isAdmin    shows whether user is authorized to see non-approved questions or not.
     * @return amount of questions
     */
    long getCount(String searchTerm, Boolean isAdmin);

    /**
     * Adds a question to an exam.
     *
     * @param examId     id of the exam.
     * @param questionId id of the question.
     */
    void addQuestionToExam(Integer examId, Integer questionId);

    /**
     * Removes a question from an exam.
     *
     * @param examId     id of the exam.
     * @param questionId id of the question.
     */
    void removeQuestionFromExam(Integer examId, Integer questionId);

    /**
     * Gets all questions that need an approval.
     *
     * @return list of questions
     */
    Iterable<BaseQuestion> getAllUnapproved();

    /**
     * Adds a question to a session.
     *
     * @param sessionId  id of the session
     * @param questionId id of the base question
     */
    void addQuestionToSession(Integer sessionId, Integer questionId);

    /**
     * Removes a question from a session
     *
     * @param sessionId  id of the session
     * @param questionId id of the base question
     */
    void removeQuestionFromSession(Integer sessionId, Integer questionId);

    /**
     * Gets all questions that are contained in a specific session.
     *
     * @param sessionId id of the session
     * @return list of questions
     */
    Iterable<BaseQuestion> findAllBySession(Integer sessionId);

    /**
     * Gets a question within a session by its local id within the session.
     * @param sessionId id of the session.
     * @param localId local id of the question within the session.
     * @return question.
     */
    BaseQuestion findBySessionLocalId(Integer sessionId, Integer localId);

    long getCount(
            @Nullable Boolean onlyApproved,
            @Nullable String searchTerm,
            @Nullable Integer semesterId,
            @Nullable Integer moduleId,
            @Nullable Integer courseId,
            @Nullable Integer examId,
            @Nullable Integer tagId,
            Boolean isAdmin
    );
}