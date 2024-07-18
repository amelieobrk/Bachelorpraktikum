package de.kreuzenonline.kreuzen.question;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseQuestionRepo extends CrudRepository<BaseQuestion, Integer> {

    Iterable<BaseQuestion> findAllByExamId(Integer examId);

    Iterable<BaseQuestion> findAllByExamIdAndIsApprovedTrue(Integer examId);

    Iterable<BaseQuestion> findAllByCourseId(Integer courseId);

    Iterable<BaseQuestion> findAllByCourseIdAndIsApprovedTrue(Integer courseId);

    @Query("SELECT q.* " +
            "FROM question_search s " +
            "JOIN question_base q ON s.id = q.id " +
            "JOIN course c on q.course_id = c.id " +
            "WHERE CASE WHEN :searchTerm IS NOT NULL THEN s.document @@ to_tsquery('german', :searchTerm) ELSE TRUE END " +
            "AND (:semesterId IS NULL OR c.semester_id = :semesterId) " +
            "AND (:moduleId IS NULL OR c.module_id = :moduleId) " +
            "AND (:courseId IS NULL OR q.course_id = :courseId) " +
            "AND (:tagId IS NULL OR exists(SELECT * FROM question_has_tag t WHERE t.tag_id = :tagId AND t.question_id = q.id)) " +
            "AND (:examId IS NULL OR q.exam_id = :examId) " +
            "AND (:isApproved IS NULL OR q.is_approved = :isApproved) " +
            "ORDER BY CASE WHEN :searchTerm IS NOT NULL THEN ts_rank(s.document, to_tsquery('german', :searchTerm)) ELSE q.id END DESC " +
            "OFFSET :skip LIMIT :limit")
    List<BaseQuestion> findBySearchTerm(String searchTerm, Integer semesterId, Integer moduleId, Integer courseId, Integer examId, Integer tagId, Boolean isApproved, int limit, int skip);

    @Query("SELECT COUNT(q.id) " +
            "FROM question_search s " +
            "JOIN question_base q ON s.id = q.id " +
            "JOIN course c on q.course_id = c.id " +
            "WHERE CASE WHEN :searchTerm IS NOT NULL THEN s.document @@ to_tsquery('german', :searchTerm) ELSE TRUE END " +
            "AND (:semesterId IS NULL OR c.semester_id = :semesterId) " +
            "AND (:moduleId IS NULL OR c.module_id = :moduleId) " +
            "AND (:courseId IS NULL OR q.course_id = :courseId) " +
            "AND (:tagId IS NULL OR exists(SELECT * FROM question_has_tag t WHERE t.tag_id = :tagId AND t.question_id = q.id)) " +
            "AND (:examId IS NULL OR q.exam_id = :examId) " +
            "AND (:isApproved IS NULL OR q.is_approved = :isApproved)")
    int countBySearchTerm(String searchTerm, Integer semesterId, Integer moduleId, Integer courseId, Integer examId, Integer tagId, Boolean isApproved);

    @Query("SELECT * FROM question_base OFFSET :skip LIMIT :limit")
    List<BaseQuestion> findAllPagination(int limit, int skip);

    @Query("SELECT * FROM question_base WHERE text LIKE CONCAT ('%', :term, '%') OFFSET :skip LIMIT :limit")
    List<BaseQuestion> findBySearchTerm(String term, int limit, int skip);

    @Query("SELECT * FROM question_base WHERE is_approved = TRUE OFFSET :skip LIMIT :limit")
    List<BaseQuestion> findAllPaginationAndIsApprovedTrue(int limit, int skip);

    @Query("SELECT * FROM question_base WHERE text LIKE CONCAT ('%', :term, '%') AND is_approved = TRUE OFFSET :skip LIMIT :limit")
    List<BaseQuestion> findBySearchTermAndIsApprovedTrue(String term, int limit, int skip);

    @Query("SELECT COUNT (*) FROM question_base WHERE is_approved = TRUE")
    int countAndIsApprovedTrue();

    @Query("SELECT COUNT (*) FROM question_base WHERE text LIKE CONCAT ('%', :term, '%')")
    int countBySearchTerm(String term);

    @Query("SELECT COUNT (*) FROM question_base WHERE text LIKE CONCAT ('%', :term, '%')")
    int countBySearchTermAndIsApprovedTrue(String term);

    @Modifying
    @Query("UPDATE question_base SET exam_id = :examId WHERE id = :questionId")
    void addQuestionToExam(Integer examId, Integer questionId);

    @Modifying
    @Query("UPDATE question_base SET exam_id = null WHERE id = :questionId")
    void removeQuestionFromExam(Integer examId, Integer questionId);

    @Modifying
    @Query("INSERT INTO session_has_question (session_id, question_id) VALUES (:sessionId, :questionId) ON CONFLICT DO NOTHING")
    void addQuestionToSession(Integer sessionId, Integer questionId);

    @Modifying
    @Query("DELETE FROM session_has_question WHERE session_id = :sessionId AND question_id = :questionId")
    void removeQuestionFromSession(Integer sessionId, Integer questionId);

    @Query("SELECT q.* FROM session_has_question shq JOIN question_base q on q.id = shq.question_id WHERE shq.session_id = :sessionId")
    List<BaseQuestion> findAllBySession(Integer sessionId);

    @Query("SELECT q.* FROM session_has_question shq JOIN question_base q on q.id = shq.question_id WHERE shq.session_id = :sessionId AND shq.local_id = :localId")
    BaseQuestion findBySessionAndLocalId(Integer sessionId, Integer localId);
}
