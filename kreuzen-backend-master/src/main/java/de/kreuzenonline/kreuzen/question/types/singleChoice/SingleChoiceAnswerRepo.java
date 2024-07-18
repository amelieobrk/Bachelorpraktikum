package de.kreuzenonline.kreuzen.question.types.singleChoice;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SingleChoiceAnswerRepo extends CrudRepository<SingleChoiceAnswer, Integer> {

    List<SingleChoiceAnswer> findAllByQuestionId(Integer questionId);
}
