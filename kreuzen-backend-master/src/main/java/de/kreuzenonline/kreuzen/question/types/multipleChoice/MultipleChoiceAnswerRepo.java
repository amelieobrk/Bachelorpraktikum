package de.kreuzenonline.kreuzen.question.types.multipleChoice;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MultipleChoiceAnswerRepo extends CrudRepository<MultipleChoiceAnswer, Integer> {

    List<MultipleChoiceAnswer> findAllByQuestionId(Integer questionId);
}
