package de.kreuzenonline.kreuzen.auth.data;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomUserDetailsRepo extends org.springframework.data.repository.Repository<CustomUserDetails, Integer> {

    @Query(value = "SELECT u.* FROM app_user u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<CustomUserDetails> findByUsername(@Param("username") String username);

    @Query(value = "SELECT u.* FROM app_user u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<CustomUserDetails> findByEmail(@Param("email") String email);

    @Query(value = "SELECT u.* FROM app_user u WHERE u.id = :id GROUP BY u.id")
    Optional<CustomUserDetails> findById(@Param("id") Integer id);
}
