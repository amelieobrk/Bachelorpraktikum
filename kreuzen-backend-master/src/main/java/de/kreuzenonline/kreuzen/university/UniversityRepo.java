package de.kreuzenonline.kreuzen.university;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UniversityRepo extends CrudRepository<University, Integer> {

    @Query("SELECT * FROM university WHERE allowed_mail_domains @> array_append(ARRAY[]::TEXT[], :domain::TEXT);")
    List<University> findByAllowedMailDomain(@Param("domain") String domain);
}
