package de.kreuzenonline.kreuzen.question.types.assignment;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AssignmentIdentifierRepo extends CrudRepository<AssignmentIdentifier, Integer> {

    List<AssignmentIdentifier> findAllByQuestionId(Integer questionId);
}
