package de.kreuzenonline.kreuzen.question.types.multipleChoice;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MultipleChoiceQuestionRepo extends CrudRepository<MultipleChoiceQuestionEntry, Integer> {

    MultipleChoiceQuestionEntry findByQuestionId(Integer questionId);
}
