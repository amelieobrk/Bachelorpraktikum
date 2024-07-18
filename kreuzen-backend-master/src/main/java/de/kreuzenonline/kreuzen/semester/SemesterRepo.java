package de.kreuzenonline.kreuzen.semester;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SemesterRepo extends CrudRepository<Semester, Integer> {

    boolean existsByNameIgnoreCase(String name);
}
