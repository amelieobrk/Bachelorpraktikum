package de.kreuzenonline.kreuzen.major;

import java.util.List;

public interface MajorService {

    /**
     * Creates a new major.
     *
     * @param universityId University Id
     * @param name         Name of the major
     * @return Created major
     */
    Major create(Integer universityId, String name);

    /**
     * Find all majors offered by a university.
     *
     * @param universityId University Id
     * @return List of offered majors
     */
    Iterable<Major> findAllByUniversity(Integer universityId);

    /**
     * Find all majors assigned to a module.
     *
     * @param moduleId Module Id
     * @return List of assigned majors
     */
    Iterable<Major> findAllByModule(Integer moduleId);

    /**
     * Find a specific major by its Id.
     *
     * @param id Id of the major
     * @return Major
     */
    Major findById(Integer id);

    /**
     * Updates a major.
     * Except for the id, all values can be set to null if no update is wanted.
     *
     * @param id   Id of the major
     * @param name New Name
     * @return updated major
     */
    Major update(Integer id, String name);

    /**
     * Deletes a major by its id.
     *
     * @param id Id of the major
     */
    void delete(Integer id);

    /**
     * Get all majors a user pursues.
     *
     * @param userId User Id
     * @return List of pursued majors
     */
    List<Major> getMajorsByUser(Integer userId);

    /**
     * Add a major to a user.
     *
     * @param majorId Id of the major
     * @param userId  User Id
     */
    void addMajorToUser(Integer userId, Integer majorId);

    /**
     * Remove a major from a user.
     *
     * @param majorId Id of the major
     * @param userId  Id of the user
     */
    void removeMajorFromUser(Integer userId, Integer majorId);
}
