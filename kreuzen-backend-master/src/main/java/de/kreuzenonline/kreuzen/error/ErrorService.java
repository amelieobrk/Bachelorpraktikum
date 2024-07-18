package de.kreuzenonline.kreuzen.error;

public interface ErrorService {

    /**
     * Get a specific error report by its id.
     *
     * @param id Id of error report
     * @return error report
     */
    Error getById(Integer id);


    /**
     * Create a new error report.
     *
     * @param comment    Content of the error report
     * @param source     Listed source. Optional variable.
     * @param questionId Id of corresponding question
     * @return New ErrorReport
     */
    Error create(String comment, String source, Integer questionId, Integer creatorId);

    /**
     * Update an error report. Except for the id, all values that should not be updated can be set to null.
     *
     * @param id                      Id of the error report.
     * @param comment                 Error reports content
     * @param source                  Listed source.
     * @param isResolved              Shows if reported error is corrected.
     * @param lastAssignedModeratorId Id of last assigned moderator
     * @return Updated error report
     */
    Error update(Integer id, String comment, String source, Boolean isResolved, Integer lastAssignedModeratorId);

    /**
     * Deletes an error report by its given id.
     * Only users with the role Administrator or Moderator can delete an error report.
     *
     * @param id id of the error report.
     */
    void delete(Integer id);

    /**
     * Get all unsolved errors
     * @return List of unsolved errors
     */
    Iterable<Error> getUnsolved();
}
