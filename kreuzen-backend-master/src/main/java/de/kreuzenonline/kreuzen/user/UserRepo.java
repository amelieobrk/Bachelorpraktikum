package de.kreuzenonline.kreuzen.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends CrudRepository<User, Integer> {
    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT * FROM app_user ORDER BY email_confirmed ASC, LOWER(first_name), LOWER(last_name) OFFSET :skip LIMIT :limit")
    List<User> findAllPagination(int limit, int skip);

    @Query("SELECT * FROM app_user WHERE " +
            " username LIKE CONCAT('%',:term,'%') " +
            " OR email LIKE CONCAT('%',:term,'%') " +
            " OR first_name LIKE CONCAT('%',:term,'%') " +
            " OR last_name LIKE CONCAT('%',:term,'%') " +
            " ORDER BY email_confirmed ASC, LOWER(first_name), LOWER(last_name) OFFSET :skip LIMIT :limit")
    List<User> findBySearchTerm(String term, int limit, int skip);

    @Query("SELECT COUNT(*) FROM app_user WHERE " +
            " username LIKE CONCAT('%',:term,'%') " +
            " OR email LIKE CONCAT('%',:term,'%') " +
            " OR first_name LIKE CONCAT('%',:term,'%') " +
            " OR last_name LIKE CONCAT('%',:term,'%')")
    int countBySearchTerm(String term);
}
