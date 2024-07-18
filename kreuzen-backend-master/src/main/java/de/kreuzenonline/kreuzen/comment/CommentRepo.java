package de.kreuzenonline.kreuzen.comment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepo extends CrudRepository<Comment, Integer> {

    List<Comment> getAllByQuestionId(Integer questionId);
}
