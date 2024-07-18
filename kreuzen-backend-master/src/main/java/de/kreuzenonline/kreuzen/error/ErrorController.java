package de.kreuzenonline.kreuzen.error;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.comment.CommentService;
import de.kreuzenonline.kreuzen.email.EmailService;
import de.kreuzenonline.kreuzen.error.requests.CreateErrorRequest;
import de.kreuzenonline.kreuzen.error.requests.UpdateErrorRequest;
import de.kreuzenonline.kreuzen.error.response.ErrorResponse;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.module.Module;
import de.kreuzenonline.kreuzen.module.responses.ModuleResponse;
import de.kreuzenonline.kreuzen.role.Roles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


@RestController
@Api(tags = "Error")
public class ErrorController {

    private final ErrorService errorService;
    private final ResourceBundle resourceBundle;
    private final CommentService commentService;
    private final EmailService emailService;


    public ErrorController(ErrorService errorService, ResourceBundle resourceBundle, CommentService commentService, EmailService emailService) {
        this.errorService = errorService;
        this.resourceBundle = resourceBundle;
        this.commentService = commentService;
        this.emailService = emailService;
    }

    @GetMapping("/error/{id}")
    @ApiOperation(
            value = "Get error report",
            notes = "Get a specific error report by its id."
    )
    public ErrorResponse getError(@PathVariable Integer id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {


        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-error-report-forbidden"));
        }
        Error error = errorService.getById(id);
        return new ErrorResponse(error);
    }


    @PostMapping("/question/{questionId}/error")
    @ApiOperation(
            value = "Create an error report",
            notes = "A comment that an error was reported will be automatically posted."
    )
    public ErrorResponse createError(@PathVariable Integer questionId, @Valid @RequestBody CreateErrorRequest request,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Error error = errorService.create(request.getComment(), request.getSource(), questionId, userDetails.getId());
        commentService.create(questionId, userDetails.getId(), (resourceBundle.getString("post-create-error")));
        return new ErrorResponse(error);
    }


    @PatchMapping("/error/{id}")
    @ApiOperation(
            value = "Update error report",
            notes = "Update the information of an error report. All variables are optional. Automatically send email to error report's creator."
    )
    public ErrorResponse updateError(@PathVariable Integer id, @Valid @RequestBody UpdateErrorRequest request,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {


        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-error-report-forbidden"));
        }

        Error error = errorService.getById(id);

        if (request.getIsResolved().equals(true)) {
            commentService.create(error.getQuestionId(), userDetails.getId(), (resourceBundle.getString("post-error-is-resolved")));
            emailService.sendErrorResolvedMessage(userDetails.getEmail(), userDetails.getFirstName());
        }
        error = errorService.update(id, request.getComment(), request.getSource(), request.getIsResolved(), userDetails.getId());

        return new ErrorResponse(error);
    }

    @DeleteMapping("error/{id}")
    @ApiOperation(
            value = "Delete error report",
            notes = "Deletes a specific error report by its id. Automatically send email to the error report's creator to inform him."
    )
    public ResponseEntity<Void> deleteErrorReport(@PathVariable Integer id,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {


        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("delete-error-report-forbidden"));
        }


        Error error = errorService.getById(id);
        if (error.getIsResolved().equals(false)) {
            emailService.sendErrorDeclinedMessage(userDetails.getEmail(), userDetails.getFirstName());
        }
        errorService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/error/unsolved")
    @ApiOperation(
            value = "Get unsolved error reports"

    )
    public List<ErrorResponse> getErrorUnsolved(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-error-unsolved-forbidden"));
        }

        Iterable<Error> errors = errorService.getUnsolved();
        List<ErrorResponse> responses = new ArrayList<>();
        for (Error e : errors) {
            responses.add(new ErrorResponse(e));
        }
        return responses;


    }


}

