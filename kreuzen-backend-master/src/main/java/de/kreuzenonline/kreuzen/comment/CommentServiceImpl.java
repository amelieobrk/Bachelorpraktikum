package de.kreuzenonline.kreuzen.comment;


import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepo commentRepo;
    private final ResourceBundle resourceBundle;

    public CommentServiceImpl(CommentRepo commentRepo, ResourceBundle resourceBundle) {
        this.commentRepo = commentRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Comment create(Integer questionId, Integer creatorId, String text) {

        Comment comment = new Comment();
        comment.setQuestionId(questionId);
        comment.setCreatorId(creatorId);
        comment.setComment(text);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        return commentRepo.save(comment);
    }

    @Override
    public Comment getById(Integer id) {

        Optional<Comment> comment = commentRepo.findById(id);

        if (comment.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("comment-not-found"));
        }

        return comment.get();
    }

    @Override
    public List<Comment> getAllByQuestionId(Integer questionId) {
        return commentRepo.getAllByQuestionId(questionId);
    }

    @Override
    public Comment update(Integer id, String text) {

        Comment comment = this.getById(id);


        if (text != null) {
            comment.setComment(text);
        }

        return commentRepo.save(comment);
    }



    @Override
    public void delete(Integer id) {
        commentRepo.deleteById(id);
    }


}
