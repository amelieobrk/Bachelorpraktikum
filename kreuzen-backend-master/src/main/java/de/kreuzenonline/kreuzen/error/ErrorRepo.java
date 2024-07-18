package de.kreuzenonline.kreuzen.error;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorRepo extends CrudRepository<Error, Integer> {


    @Modifying
    @Query("INSERT INTO question_has_error (error_id, question_id) VALUES (:errorId, :questionId) ON CONFLICT DO NOTHING")
    void addErrorToQuestion(Integer errorId, Integer questionId);

    Iterable<Error> findAllByIsResolved(Boolean isResolved);

}
