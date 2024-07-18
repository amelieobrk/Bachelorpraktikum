package de.kreuzenonline.kreuzen.section;

import java.util.List;

public interface SectionService {

    /**
     * Creates a new section like Bachelor/Master onto an existing major.
     * Every connection between major and section is unique.
     *
     * @param majorId Id of the major where the section will be added.
     * @param name    name of the added section.
     * @return created section.
     */
    Section create(Integer majorId, String name);

    /**
     * Adds a section onto a User.
     * Ids are gathered by the URI path.
     *
     * @param userId    id of the user
     * @param majorId   id of the major
     * @param sectionId id of the section
     */
    void addSectionToUser(Integer userId, Integer majorId, Integer sectionId);

    /**
     * Adds a section onto a User.
     * Ids are gathered by the URI path.
     *
     * @param userId    id of the user
     * @param sectionId id of the section
     */
    void addSectionToUser(Integer userId, Integer sectionId);

    /**
     * Finds all sections that are linked to a certain major.
     *
     * @param majorId id of the major
     * @return List of the sections that belong to the major.
     */
    Iterable<Section> findAllByMajor(Integer majorId);

    /**
     * Finds all sections that are linked to a certain module.
     *
     * @param moduleId id of the module
     * @return List of the sections that are assigned to the module.
     */
    Iterable<Section> findAllByModule(Integer moduleId);

    /**
     * Finds and returns a section by its id.
     * If section is not found, an Exception gets thrown.
     *
     * @param id id of the section.
     * @return the section with the overmitted id.
     */
    Section findById(Integer id);

    /**
     * Returns a list of all sections that a user is currently enrolled in, e.g. simultaneous enrollment in the Bachelor and the Master of a major.
     *
     * @param userId  id of the user.
     * @param majorId id of the major.
     * @return a list of sections.
     */
    List<Section> getSectionsByUserAndMajor(Integer userId, Integer majorId);

    /**
     * Updates the name of a given section.
     * It will be checked whether the major-section-combination is still unique.
     *
     * @param id   the id of the section.
     * @param name the new name of the section.
     * @return updated section.
     */
    Section update(Integer id, String name);

    /**
     * Deletes a section by the given id.
     *
     * @param id id of the section
     */
    void delete(Integer id);

    /**
     * Removes a section from a given user and major.
     *
     * @param userId    id of the user.
     * @param majorId   id of the major.
     * @param sectionId id of the section.
     */
    void removeSectionFromUser(Integer userId, Integer majorId, Integer sectionId);

}
