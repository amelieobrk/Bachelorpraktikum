package de.kreuzenonline.kreuzen.session;


import de.kreuzenonline.kreuzen.session.selections.MultipleChoiceSelection;
import de.kreuzenonline.kreuzen.session.selections.SingleChoiceSelection;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepo extends CrudRepository<Session, Integer> {

    @Query("SELECT * FROM session WHERE creator_id = :userId ORDER BY created_at DESC OFFSET :skip LIMIT :limit")
    List<Session> findAllPagination(Integer userId, Integer limit, Integer skip);

    @Query("SELECT COUNT(*) FROM session WHERE creator_id = :userId")
    Integer getCountByUser(Integer userId);

    @Query("SELECT COUNT(qsca.id) " +
            " FROM session_has_question shq " +
            " JOIN question_single_choice_answer qsca ON qsca.question_id = shq.question_id " +
            " WHERE shq.session_id = :sessionId AND shq.local_id = :questionLocalId")
    Integer getSingleChoiceAnswerCount(Integer sessionId, Integer questionLocalId);

    @Query("INSERT INTO session_single_choice_selection (session_id, answer_id, is_crossed, is_checked) " +
            " SELECT :sessionId AS session_id, qsca.id AS answer_id, :isCrossed AS is_crossed, :isChecked AS is_checked " +
            " FROM session_has_question shq " +
            " JOIN question_single_choice_answer qsca ON qsca.question_id = shq.question_id " +
            " WHERE shq.session_id = :sessionId AND shq.local_id = :questionLocalId AND qsca.local_id = :answerId" +
            " ON CONFLICT (session_id, answer_id) DO UPDATE SET is_checked = :isChecked, is_crossed = :isCrossed")
    @Modifying
    void addSingleChoiceSelection(Integer sessionId, Integer questionLocalId, Integer answerId, Boolean isChecked, Boolean isCrossed);

    @Query("SELECT COUNT(qmca.id) " +
            " FROM session_has_question shq " +
            " JOIN question_multiple_choice_answer qmca ON qmca.question_id = shq.question_id " +
            " WHERE shq.session_id = :sessionId AND shq.local_id = :questionLocalId")
    Integer getMultipleChoiceAnswerCount(Integer sessionId, Integer questionLocalId);

    @Modifying
    @Query("INSERT INTO session_multiple_choice_selection (session_id, answer_id, is_crossed, is_checked) " +
            " SELECT :sessionId AS session_id, qmca.id AS answer_id, :isCrossed AS is_crossed, :isChecked AS is_checked " +
            " FROM session_has_question shq " +
            " JOIN question_multiple_choice_answer qmca ON qmca.question_id = shq.question_id " +
            " WHERE shq.session_id = :sessionId AND shq.local_id = :questionLocalId AND qmca.local_id = :answerId" +
            " ON CONFLICT (session_id, answer_id) DO UPDATE SET is_checked = :isChecked, is_crossed = :isCrossed")
    void addMultipleChoiceSelection(Integer sessionId, Integer questionLocalId, Integer answerId, Boolean isChecked, Boolean isCrossed);

    @Query("UPDATE session_has_question SET time = :answerTime WHERE session_id = :sessionId AND local_id = :localId")
    @Modifying
    void addTime(Integer sessionId, Integer localId, Integer answerTime);

    @Modifying
    @Query("UPDATE session_has_question SET is_submitted = TRUE WHERE session_id = :sessionId AND local_id = :localId")
    void submitQuestion(Integer sessionId, Integer localId);

    @Query("SELECT COUNT(*)  FROM (SELECT DISTINCT q.id FROM question_base q " +
            "    LEFT JOIN question_has_tag qht on q.id = qht.question_id " +
            "    JOIN course c on q.course_id = c.id " +
            "    JOIN module m on c.module_id = m.id " +
            "    JOIN question_search s ON s.id = q.id " +
            "    WHERE (array_length(:moduleIds, 1) IS NULL OR m.id = ANY(:moduleIds)) " +
            "    AND (array_length(:semesterIds, 1) IS NULL OR c.semester_id = ANY(:semesterIds)) " +
            "    AND (array_length(:tagIds, 1) IS NULL OR (qht.tag_id IS NOT NULL AND qht.tag_id = ANY(:tagIds))) " +
            "    AND (array_length(:questionTypes, 1) IS NULL OR q.type = ANY(CAST(:questionTypes AS question_type[]))) " +
            "    AND (array_length(:questionOrigins, 1) IS NULL OR q.origin = ANY(:questionOrigins)) " +
            "    AND (:textFilter IS NULL OR :textFilter = '' OR CASE WHEN :textFilter IS NOT NULL THEN s.document @@ to_tsquery('german', concat(:textFilter, ':')) ELSE TRUE END)) x")
    Integer getCountByParameters(Integer[] moduleIds, Integer[] semesterIds, Integer[] tagIds, String[] questionTypes, String[] questionOrigins, String textFilter);

    @Query("SELECT COUNT(*) FROM session_has_question WHERE session_id = :sessionId")
    Integer getQuestionCountBySessionId(Integer sessionId);

    @Modifying
    @Query("INSERT INTO session_has_question (session_id, question_id, local_id) " +
            "SELECT :sessionId AS session_id, x.question_id, rank() OVER (ORDER BY random()) as local_id " +
            "FROM (" +
            "    SELECT DISTINCT q.id AS question_id FROM question_base q " +
            "    LEFT JOIN question_has_tag qht on q.id = qht.question_id " +
            "    JOIN course c on q.course_id = c.id " +
            "    JOIN module m on c.module_id = m.id " +
            "    JOIN question_search s ON s.id = q.id " +
            "    WHERE (array_length(:moduleIds, 1) IS NULL OR m.id = ANY(:moduleIds)) " +
            "    AND (array_length(:semesterIds, 1) IS NULL OR c.semester_id = ANY(:semesterIds)) " +
            "    AND (array_length(:tagIds, 1) IS NULL OR (qht.tag_id IS NOT NULL AND qht.tag_id = ANY(:tagIds))) " +
            "    AND (array_length(:questionTypes, 1) IS NULL OR q.type = ANY(CAST(:questionTypes AS question_type[]))) " +
            "    AND (array_length(:questionOrigins, 1) IS NULL OR q.origin = ANY(:questionOrigins)) " +
            "    AND (:textFilter IS NULL OR :textFilter = '' OR CASE WHEN :textFilter IS NOT NULL THEN s.document @@ to_tsquery('german', concat(:textFilter, ':')) ELSE TRUE END)" +
            ") x")
    void assignQuestionsRandom(Integer sessionId, Integer[] moduleIds, Integer[] semesterIds, Integer[] tagIds, String[] questionTypes, String[] questionOrigins, String textFilter);

    @Modifying
    @Query("INSERT INTO session_has_question (session_id, question_id, local_id) " +
            "SELECT :sessionId AS session_id, x.question_id, rank() OVER (ORDER BY x.question_id) as local_id " +
            "FROM (" +
            "    SELECT DISTINCT q.id AS question_id FROM question_base q " +
            "    LEFT JOIN question_has_tag qht on q.id = qht.question_id " +
            "    JOIN course c on q.course_id = c.id " +
            "    JOIN module m on c.module_id = m.id " +
            "    JOIN question_search s ON s.id = q.id " +
            "    WHERE (array_length(:moduleIds, 1) IS NULL OR m.id = ANY(:moduleIds)) " +
            "    AND (array_length(:semesterIds, 1) IS NULL OR c.semester_id = ANY(:semesterIds)) " +
            "    AND (array_length(:tagIds, 1) IS NULL OR (qht.tag_id IS NOT NULL AND qht.tag_id = ANY(:tagIds))) " +
            "    AND (array_length(:questionTypes, 1) IS NULL OR q.type = ANY(CAST(:questionTypes AS question_type[]))) " +
            "    AND (array_length(:questionOrigins, 1) IS NULL OR q.origin = ANY(:questionOrigins)) " +
            "    AND (:textFilter IS NULL OR :textFilter = '' OR CASE WHEN :textFilter IS NOT NULL THEN s.document @@ to_tsquery('german', concat(:textFilter, ':')) ELSE TRUE END)" +
            ") x")
    void assignQuestions(Integer sessionId, Integer[] moduleIds, Integer[] semesterIds, Integer[] tagIds, String[] questionTypes, String[] questionOrigins, String textFilter);

    @Modifying
    @Query("UPDATE session SET is_finished = TRUE WHERE id = :sessionId")
    void submitSession(Integer sessionId);

    @Modifying
    @Query("UPDATE session_has_question SET is_submitted = TRUE WHERE session_id = :sessionId")
    void submitAllQuestions(Integer sessionId);

    @Modifying
    @Query("UPDATE session SET is_finished = FALSE WHERE id = :sessionId")
    void resetSession(Integer sessionId);

    @Modifying
    @Query("UPDATE session_has_question SET is_submitted = FALSE, time = 0 WHERE session_id = :sessionId")
    void resetSessionQuestions(Integer sessionId);

    @Modifying
    @Query("DELETE FROM session_single_choice_selection WHERE session_id = :sessionId")
    void resetSessionSingleChoiceSelections(Integer sessionId);

    @Modifying
    @Query("DELETE FROM session_multiple_choice_selection WHERE session_id = :sessionId")
    void resetSessionMultipleChoiceSelections(Integer sessionId);
}
