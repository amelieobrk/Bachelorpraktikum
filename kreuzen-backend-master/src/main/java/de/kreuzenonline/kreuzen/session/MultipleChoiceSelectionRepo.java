package de.kreuzenonline.kreuzen.session;

import de.kreuzenonline.kreuzen.session.selections.MultipleChoiceSelection;
import de.kreuzenonline.kreuzen.session.selections.SingleChoiceSelection;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MultipleChoiceSelectionRepo extends CrudRepository<MultipleChoiceSelection, Integer> {

    @Query("SELECT " +
            " smcs.id AS id, smcs.session_id as session_id, shq.local_id AS local_question_id, " +
            " qmca.local_id AS local_answer_id, smcs.is_checked AS is_checked, smcs.is_crossed AS is_crossed " +
            " FROM session_multiple_choice_selection smcs " +
            "    JOIN question_multiple_choice_answer qmca on smcs.answer_id = qmca.id " +
            "    JOIN session_has_question shq on qmca.question_id = shq.question_id AND smcs.session_id = shq.session_id " +
            " WHERE smcs.session_id = :sessionId AND shq.local_id = :localId")
    Iterable<MultipleChoiceSelection> findMultipleChoiceSelections(Integer sessionId, Integer localId);

}
