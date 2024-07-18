package de.kreuzenonline.kreuzen.tag;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepo extends CrudRepository<Tag, Integer> {

    Iterable<Tag> findAllByModuleId(Integer moduleId);

    @Query("SELECT t.* FROM question_has_tag qht JOIN tag t on t.id = qht.tag_id WHERE qht.question_id = :questionId")
    Iterable<Tag> findAllByQuestionId(Integer questionId);

    @Modifying
    @Query("INSERT INTO question_has_tag (question_id, tag_id) VALUES (:questionId, :tagId) ON CONFLICT DO NOTHING")
    void addTagToQuestion(Integer questionId, Integer tagId);

    @Modifying
    @Query("DELETE FROM question_has_tag WHERE question_id = :questionId AND tag_id = :tagId")
    void removeTagFromQuestion(Integer questionId, Integer tagId);

}

