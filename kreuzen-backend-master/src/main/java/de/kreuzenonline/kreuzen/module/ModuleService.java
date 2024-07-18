package de.kreuzenonline.kreuzen.module;


import java.util.List;

public interface ModuleService {

    /**
     * Get a specific module by its id.
     * Only users with the role Administrator can view a module in detail.
     * Before the module can be viewed, the password needs to be entered for security purposes.
     *
     * @param id Id of module
     * @return module
     */
    Module getById(Integer id);


    /**
     * Create a new module.
     *
     * @param name             name of module
     * @param universityId     id of corresponding university
     * @param isUniversityWide declares if module is available for whole university or just specific sections
     * @return New Module
     */
    Module create(String name, Integer universityId, Boolean isUniversityWide);

    /**
     * Update a module. Except for the id, all values that should not be updated can be set to null.
     *
     * @param id               Id of the module
     * @param name             Name of the module
     * @param universityId     id of corresponding university
     * @param isUniversityWide declares if module is available for whole university or just specific sections
     * @return Updated university
     */
    Module update(Integer id, String name, Integer universityId, Boolean isUniversityWide);

    /**
     * Deletes a module by its given id.
     * Only users with the role Administrator can delete module.
     * Before the module is deleted, the password needs to be entered for security purposes.
     *
     * @param id id of the module.
     */
    void delete(Integer id);

    /**
     * Get all modules.
     *
     * @return List of all modules.
     */
    Iterable<Module> getAll();

    /**
     * Get all modules an university offers.
     *
     * @param universityId University Id
     * @return List of offered modules
     */
    Iterable<Module> getModulesByUniversity(Integer universityId);


    /**
     * Get all modules a user participates in.
     *
     * @param userId User Id
     * @return List of modules
     */
    List<Module> getModulesByUser(Integer userId);

    /**
     * Adds a module to a major.
     *
     * @param moduleId id of the module
     * @param majorId  id of the major
     */
    void addModuleToMajor(Integer moduleId, Integer majorId);

    /**
     * Remove a module from a major.
     *
     * @param moduleId Id of the module
     * @param majorId  Id of the major
     */
    void removeModuleFromMajor(Integer moduleId, Integer majorId);

    /**
     * Adds a module to a section.
     *
     * @param moduleId  id of the module
     * @param sectionId id of the section
     */
    void addModuleToSection(Integer moduleId, Integer sectionId);

    /**
     * Remove a module from a section.
     *
     * @param moduleId  Id of the module
     * @param sectionId Id of the section
     */
    void removeModuleFromSection(Integer moduleId, Integer sectionId);


}
