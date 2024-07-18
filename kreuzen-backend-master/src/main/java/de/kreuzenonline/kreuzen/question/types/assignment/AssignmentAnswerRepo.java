package de.kreuzenonline.kreuzen.question.types.assignment;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AssignmentAnswerRepo extends CrudRepository<AssignmentAnswer, Integer> {

    List<AssignmentAnswer> findAllByQuestionId(Integer questionId);
}
