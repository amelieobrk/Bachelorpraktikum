package de.kreuzenonline.kreuzen.university;

import java.util.List;

public interface UniversityService {

    /**
     * Get all registered universities.
     *
     * @return List of all universities
     */
    Iterable<University> getAll();

    /**
     * Get a specific university by its id.
     *
     * @param id Id of university
     * @return University
     */
    University getById(Integer id);

    /**
     * Find one or more universities by their allowed email domains.
     * Multiple universities is a corner case that occurs when multiple universities share a domain.
     *
     * @param domain Domain by which universities are searched
     * @return List of universities that use this domain
     */
    List<University> getByDomain(String domain);

    /**
     * Create a new university.
     *
     * @param name           Name of the university
     * @param allowedDomains List of allowed email address domains
     * @return New university
     */
    University create(String name, String[] allowedDomains);

    /**
     * Delete a university based on its id
     *
     * @param id Id of the university
     */
    void delete(Integer id);

    /**
     * Update a university. Except for the id, all values that should not be updated can be set to null.
     *
     * @param id             Id of the university
     * @param name           Name of the university
     * @param allowedDomains List of allowed email domains by the university
     * @return Updated university
     */
    University update(Integer id, String name, String[] allowedDomains);

    /**
     * Checks whether the domain of the email address is allowed by the given university.
     * If it is not allowed, an exception is thrown.
     *
     * @param universityId Id of the university
     * @param email Email Address to be validated
     */
    void checkEmailAllowed(Integer universityId, String email);
}
