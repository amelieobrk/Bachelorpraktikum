package de.kreuzenonline.kreuzen.semester;

public interface SemesterService {
    /**
     * Get all semesters.
     *
     * @return List of all semesters.
     */
    Iterable<Semester> getAll();

    /**
     * Creates a new semester.
     *
     * @param name      Name of the semester.
     * @param startYear Year, in which the semester starts.
     * @param endYear   Year, in which the semester ends.
     * @return Created semester.
     */
    Semester create(String name, Integer startYear, Integer endYear);

    /**
     * Find a specific semester by its Id.
     *
     * @param id Id of the semester
     * @return Semester
     */
    Semester getById(Integer id);

    /**
     * Deletes a semester by its given id.
     * Only users with the role Administrator can delete semesters.
     * Before the semester is deleted, the password needs to be entered for security purposes.
     *
     * @param id id of the semester.
     */
    void delete(Integer id);

    /**
     * Updates a semester with the information included in the overmitted request.
     * Only users with the role Administrator can update semesters.
     * Before the semester is updated, there will be a check, if the parameters still fit the requirements that a Semester should meet, e.g. whether start year is in a certain range.
     *
     * @param id           id of the semester
     * @param newName      new name of the semester
     * @param newStartYear new start year of the semester
     * @param newEndYear   new end year of the semester
     * @return the updated Semester
     */
    Semester update(Integer id,
                    String newName,
                    Integer newStartYear,
                    Integer newEndYear);

}
