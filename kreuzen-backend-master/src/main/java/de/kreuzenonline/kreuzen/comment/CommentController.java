package de.kreuzenonline.kreuzen.comment;


import de.kreuzenonline.kreuzen.comment.requests.CreateCommentRequest;
import de.kreuzenonline.kreuzen.comment.requests.UpdateCommentRequest;
import de.kreuzenonline.kreuzen.comment.responses.CommentResponse;
import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.role.Roles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


@RestController
@Api(tags = "Comment")
public class CommentController {


    private final CommentService commentService;
    private final ResourceBundle resourceBundle;


    public CommentController(CommentService commentService, ResourceBundle resourceBundle, PasswordEncoder passwordEncoder) {
        this.commentService = commentService;
        this.resourceBundle = resourceBundle;

    }


    @PostMapping("/question/{questionId}/comment")
    @ApiOperation(value = "Post a comment.",
            notes = "Inserts a new comment."
    )
    public CommentResponse createComment(@Valid @RequestBody CreateCommentRequest request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         @PathVariable Integer questionId) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Comment comment = commentService.create(questionId, userDetails.getId(), request.getText());

        return new CommentResponse(comment);
    }

    @GetMapping("/comment/{id}")
    @ApiOperation(
            value = "Get comment",
            notes = "Get a specific comment by its id."
    )
    public CommentResponse getComment(@PathVariable Integer id,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Comment comment = commentService.getById(id);

        return new CommentResponse(comment);
    }

    @GetMapping("/question/{questionId}/comment")
    @ApiOperation(
            value = "Get all comments for a specific question"
    )
    public List<CommentResponse> getCommentsByQuestion(@PathVariable Integer questionId,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        List<Comment> comment = commentService.getAllByQuestionId(questionId);

        return comment.stream().map(CommentResponse::new).collect(Collectors.toList());
    }

    @PatchMapping("/comment/{commentId}")
    @ApiOperation(
            value = "Edit a comment."
    )
    public CommentResponse updateComment(@PathVariable Integer commentId, @Valid @RequestBody UpdateCommentRequest request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }


        boolean isCreator = userDetails.getId().equals(commentService.getById(commentId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));
        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("update-comment-forbidden"));
        }

        Comment comment = commentService.update(commentId, request.getText());

        return new CommentResponse(comment);
    }


    @DeleteMapping("/comment/{id}")
    @ApiOperation(
            value = "Delete comment",
            notes = "Deletes a specific comment by its id"
    )
    public ResponseEntity<Void> deleteComment(@PathVariable Integer id,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isCreator = userDetails.getId().equals(commentService.getById(id).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));
        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("delete-comment-forbidden"));
        }

        commentService.delete(id);

        return ResponseEntity.noContent().build();

    }


}
