package de.kreuzenonline.kreuzen.question.types.singleChoice;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SingleChoiceQuestionRepo extends CrudRepository<SingleChoiceQuestionEntry, Integer> {

    SingleChoiceQuestionEntry findByQuestionId(Integer questionId);

}
