package de.kreuzenonline.kreuzen.session;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionQuestionRepo extends CrudRepository<SessionQuestion, Integer> {

    SessionQuestion findBySessionIdAndLocalId(Integer sessionId, Integer localId);

    List<SessionQuestion> findAllBySessionId(Integer sessionId);
}
