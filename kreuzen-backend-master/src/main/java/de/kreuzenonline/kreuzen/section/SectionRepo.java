package de.kreuzenonline.kreuzen.section;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepo extends CrudRepository<Section, Integer> {

    List<Section> getAllSectionsByMajorId(Integer majorId);

    @Query("SELECT s.* FROM app_user_has_major_section uhms JOIN major_section s on s.id = uhms.section_id WHERE uhms.major_id=:majorId AND uhms.user_id=:userId")
    List<Section> getSectionsByUserIdAndMajorId(@Param("userId") Integer userId, @Param("majorId") Integer majorId);

    @Modifying
    @Query("INSERT INTO app_user_has_major_section(user_id, major_id, section_id) VALUES (:userId, :majorId, :sectionId) ON CONFLICT DO NOTHING")
    void addUserToSection(@Param("userId") Integer userId, @Param("majorId") Integer majorId, @Param("sectionId") Integer sectionId);

    @Modifying
    @Query("DELETE FROM app_user_has_major_section WHERE user_id=:userId AND major_id=:majorId AND section_id=:sectionId")
    void removeUserFromSection(@Param("userId") Integer userId, @Param("majorId") Integer majorId, @Param("sectionId") Integer sectionId);

    @Query("SELECT s.* FROM module m " +
            " JOIN major_section_has_module shm ON m.id = shm.module_id " +
            " JOIN major_section s ON shm.section_id = s.id " +
            " WHERE m.id = :moduleId")
    List<Section> findAllByModuleId(Integer moduleId);
}
