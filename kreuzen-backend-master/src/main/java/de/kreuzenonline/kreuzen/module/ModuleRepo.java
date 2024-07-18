package de.kreuzenonline.kreuzen.module;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepo extends CrudRepository<Module, Integer> {

    @Query("SELECT m.* FROM major_has_module mhm JOIN module m on m.id = mhm.module_id WHERE mhm.major_id = :majorId")
    List<Module> findAllByMajorId(Integer majorId);

    @Query("SELECT m.* FROM major_section_has_module mshm JOIN module m on mshm.module_id = m.id WHERE mshm.section_id = :majorSectionId")
    List<Module> findAllByMajorSectionId(Integer majorSectionId);

    @Query("SELECT m.* FROM app_user u JOIN university du on u.university_id = du.id JOIN module m on du.id = m.university_id WHERE u.id = :userId AND m.is_university_wide" +
            " UNION" +
            " SELECT m.* FROM app_user u JOIN app_user_has_major uhm on u.id = uhm.user_id JOIN major_has_module mhm on uhm.major_id = mhm.major_id JOIN module m on m.id = mhm.module_id WHERE u.id = :userId" +
            " UNION" +
            " SELECT m.* FROM app_user u JOIN app_user_has_major_section uhms on u.id = uhms.user_id JOIN major_section_has_module mshm on uhms.section_id = mshm.section_id JOIN module m on m.id = mshm.module_id WHERE u.id = :userId")
    List<Module> findAllByUserId(Integer userId);

    @Modifying
    @Query("INSERT INTO major_has_module (module_id, major_id) VALUES (:moduleId, :majorId) ON CONFLICT DO NOTHING")
    void addModuleToMajor(Integer moduleId, Integer majorId);

    @Modifying
    @Query("DELETE FROM major_has_module WHERE module_id = :moduleId AND major_id = :majorId")
    void removeModuleFromMajor(Integer moduleId, Integer majorId);

    @Modifying
    @Query("INSERT INTO major_section_has_module (module_id, section_id) VALUES (:moduleId, :sectionId) ON CONFLICT DO NOTHING")
    void addModuleToMajorSection(Integer moduleId, Integer sectionId);

    @Modifying
    @Query("DELETE FROM major_section_has_module WHERE module_id = moduleId AND section_id = :sectionId")
    void removeModuleFromMajorSection(Integer moduleId, Integer sectionId);

    Iterable<Module> findAllByUniversityId(Integer universityId);

    boolean existsByNameIgnoreCase(String name);
}
