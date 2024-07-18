package de.kreuzenonline.kreuzen.major;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MajorRepo extends CrudRepository<Major, Integer> {
    Iterable<Major> findAllByUniversityId(Integer universityId);

    @Query("SELECT m.* FROM app_user u JOIN app_user_has_major uhm ON uhm.user_id=u.id JOIN major m on uhm.major_id = m.id WHERE u.id=:userId")
    List<Major> findAllByUser(@Param("userId") Integer userId);

    @Modifying
    @Query("INSERT INTO app_user_has_major (user_id, major_id) VALUES (:userId, :majorId) ON CONFLICT DO NOTHING")
    void addMajorToUser(@Param("userId") Integer userId, @Param("majorId") Integer majorId);

    @Modifying
    @Query("DELETE FROM app_user_has_major WHERE user_id=:userId AND major_id=:majorId")
    void removeMajorFromUser(@Param("userId") Integer userId, @Param("majorId") Integer majorId);

    @Query("SELECT m.* FROM module mod " +
            " JOIN major_has_module mhm ON mod.id = mhm.module_id " +
            " JOIN major m ON m.id = mhm.major_id " +
            " WHERE mod.id = :moduleId")
    List<Major> findAllByModuleId(Integer moduleId);
}
