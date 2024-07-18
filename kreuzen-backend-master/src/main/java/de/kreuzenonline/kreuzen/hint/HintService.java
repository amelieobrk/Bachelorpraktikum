package de.kreuzenonline.kreuzen.hint;

public interface HintService {

    /**
     * Get all hints.
     *
     * @return list of hints.
     */
    Iterable<Hint> getAll();

    /**
     * Creates a new hint.
     *
     * @param text     the actual hint
     * @param isActive whether the hint is active (= visible for all users) or inactive
     * @return the new hint
     */
    Hint create(String text, Boolean isActive);

    /**
     * Gets a hint by its id.
     *
     * @param id id of the hint
     * @return the hint
     */
    Hint getById(Integer id);

    /**
     * Gets a random hint that is set on active.
     *
     * @return hint.
     */
    Hint getRandomHint();

    /**
     * Deletes a hint by its id.
     *
     * @param id id of the hint
     */
    void delete(Integer id);

    /**
     * Updates an existing hint.
     * Hints can only be updated by moderators and administrators.
     *
     * @param id       id of the hint
     * @param text     optional parameter, if the actual hint shall be updated.
     * @param isActive whether the hint is active (= visible for all users) or inactive
     * @return updated hint
     */
    Hint update(Integer id, String text, Boolean isActive);
}
