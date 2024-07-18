package de.kreuzenonline.kreuzen.session;

import de.kreuzenonline.kreuzen.session.selections.MultipleChoiceSelection;
import de.kreuzenonline.kreuzen.session.selections.SingleChoiceSelection;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SingleChoiceSelectionRepo extends CrudRepository<SingleChoiceSelection, Integer> {

    @Query("SELECT " +
            " sscs.id AS id, sscs.session_id as session_id, shq.local_id AS local_question_id, " +
            " qsca.local_id AS local_answer_id, sscs.is_checked AS is_checked, sscs.is_crossed AS is_crossed " +
            " FROM session_single_choice_selection sscs " +
            "    JOIN question_single_choice_answer qsca on sscs.answer_id = qsca.id " +
            "    JOIN session_has_question shq on qsca.question_id = shq.question_id AND sscs.session_id = shq.session_id " +
            " WHERE sscs.session_id = :sessionId AND shq.local_id = :localId")
    Iterable<SingleChoiceSelection> findSingleChoiceSelections(Integer sessionId, Integer localId);
}
