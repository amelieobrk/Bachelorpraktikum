package de.kreuzenonline.kreuzen.tag;

public interface TagService {
    /**
     * Creates a new tag that is linked to a specific module.
     *
     * @param name     name of the tag
     * @param moduleId id of the module to which the tag is linked to.
     * @return new Tag.
     */
    Tag create(String name, Integer moduleId);

    /**
     * Find a specific tag by its id.
     *
     * @param id id of the tag
     * @return Tag
     */
    Tag findById(Integer id);

    /**
     * Finds all tags that belong to a specific module.
     *
     * @param moduleId id of the module
     * @return List of Tags.
     */
    Iterable<Tag> findAllByModule(Integer moduleId);

    /**
     * Updates a tag with a new name.
     *
     * @param id   id of the tag.
     * @param name the new name for the tag.
     * @return the updated tag.
     */
    Tag update(Integer id, String name);

    /**
     * Deletes a tag by the given id.
     *
     * @param id id of the tag.
     */
    void delete(Integer id);

    /**
     * Get all tags that are linked to a specific question.
     *
     * @param questionId id of the question
     * @return list of tags.
     */
    Iterable<Tag> findAllByQuestion(Integer questionId);

    /**
     * adds a tag to a question.
     *
     * @param questionId id of the question
     * @param tagId      id of the tag
     */
    void addTagToQuestion(Integer questionId, Integer tagId);

    /**
     * removes a tag from a question.
     *
     * @param questionId id of the question
     * @param tagId      id of the tag
     */
    void removeTagFromQuestion(Integer questionId, Integer tagId);


}
