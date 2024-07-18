package de.kreuzenonline.kreuzen.comment;


import java.util.List;

public interface CommentService {

    /**
     * Posts a new comment.
     *
     * @param questionId Id of the question the comment is posted under
     * @param creatorId  Id of the author of the comment
     * @param text       Content of the comment
     * @return Created comment
     */
    Comment create(Integer questionId, Integer creatorId, String text);

    /**
     * Get a specific comment by its id.
     *
     * @param id Id of comment
     * @return comment
     */
    Comment getById(Integer id);

    /**
     * Gets all comments for a specific question.
     *
     * @param questionId Question ID
     * @return List of comments for the question
     */
    List<Comment> getAllByQuestionId(Integer questionId);


    /**
     * Update a comment. All variables are necessary.
     *
     * @param id   Id of comment
     * @param text Edited comment
     * @return Updated comment
     */
    Comment update(Integer id, String text);



    /**
     * Deletes a comment by its given id.
     *
     * @param id id of the comment.
     */
    void delete(Integer id);


}
