package de.kreuzenonline.kreuzen.hint;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HintRepo extends CrudRepository<Hint, Integer> {

    Boolean existsByTextIgnoreCase(String text);

    List<Hint> findAllByIsActiveTrue();
}
