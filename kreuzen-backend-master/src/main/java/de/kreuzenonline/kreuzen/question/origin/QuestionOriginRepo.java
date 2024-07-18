package de.kreuzenonline.kreuzen.question.origin;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionOriginRepo extends CrudRepository<QuestionOrigin, Integer> {

    Boolean existsByName(String name);

}
