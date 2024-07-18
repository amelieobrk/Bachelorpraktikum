package de.kreuzenonline.kreuzen.question.types.assignment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentQuestionRepo extends CrudRepository<AssignmentQuestionEntry, Integer> {

    AssignmentQuestionEntry findByQuestionId(Integer questionId);

    Boolean existsByQuestionId(Integer questionId);

}
