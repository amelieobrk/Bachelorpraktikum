package de.kreuzenonline.kreuzen.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import de.kreuzenonline.kreuzen.question.requests.CreateQuestionRequest;
import de.kreuzenonline.kreuzen.question.requests.UpdateQuestionRequest;
import de.kreuzenonline.kreuzen.question.responses.BaseQuestionResponse;
import de.kreuzenonline.kreuzen.question.types.QuestionTypeMapperService;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.session.SessionService;
import de.kreuzenonline.kreuzen.utils.PaginationResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@RestController
@Api(tags = "Question")
public class QuestionController {

    private final BaseQuestionService baseQuestionService;
    private final QuestionTypeMapperService questionTypeMapperService;
    private final SessionService sessionService;
    private final BaseQuestionRepo baseQuestionRepo;
    private final ObjectMapper mapper;
    private final ResourceBundle resourceBundle;

    public QuestionController(BaseQuestionServiceImpl baseQuestionService, QuestionTypeMapperService questionTypeMapperService, SessionService sessionService, BaseQuestionRepo baseQuestionRepo, ObjectMapper mapper, ResourceBundle resourceBundle) {
        this.baseQuestionService = baseQuestionService;
        this.questionTypeMapperService = questionTypeMapperService;
        this.sessionService = sessionService;
        this.baseQuestionRepo = baseQuestionRepo;
        this.mapper = mapper;
        this.resourceBundle = resourceBundle;
    }

    @GetMapping("/question/{id}")
    @ApiOperation(value = "Get a question",
            notes = "Gets a specific question by its id.")
    public BaseQuestionResponse getQuestion(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        // Get base question
        BaseQuestion baseQuestion = baseQuestionRepo.findById(id).orElseThrow(() -> new NotFoundException(resourceBundle.getString("question-not-found")));
        // Get question from typeService
        return questionTypeMapperService.getServiceByType(baseQuestion.getType())
                .map(service -> service.getByQuestionId(id))
                .map(BaseQuestion::toResponse)
                .orElse(new BaseQuestionResponse(baseQuestion));
    }

    @DeleteMapping("/question/{id}")
    @ApiOperation(value = "Deletes a question.",
            notes = "Primarily deletes the base question but also deletes the type question.")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Integer id,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        BaseQuestion toDelete = baseQuestionService.getById(id);

        boolean isCreator = userDetails.getId().equals(toDelete.getCreatorId());

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only admins/mods and the initial creator are allowed to delete a question.
        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("delete-question-forbidden"));
        }

        baseQuestionService.delete(toDelete.getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/question")
    @ApiOperation(value = "Post a question.",
            notes = "Insert a new question into the website.")
    public BaseQuestionResponse createQuestion(HttpServletRequest httpRequest, @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        String body = new BufferedReader(
                new InputStreamReader(httpRequest.getInputStream(), StandardCharsets.UTF_8)
        ).lines().collect(Collectors.joining("\n"));

        @Valid CreateQuestionRequest request = mapper.readValue(body, CreateQuestionRequest.class);

        BaseQuestion baseQuestion = baseQuestionService.create(request.getText(), request.getType(), request.getAdditionalInformation(), request.getPoints(), request.getExamId(), request.getCourseId(), userDetails.getId(), request.getOrigin(), request.getFile());

        /*
          In the first step the base question will be created before checking the requirements of the respective type question.
          After that the type question will be created. If an error occurs while creating the type question, the base question will be deleted in order to keep data consistent.
         */

        try {
            return questionTypeMapperService.getServiceByType(request.getType())
                    .map(service -> service.genericCreate(mapper, body, baseQuestion.getId()))
                    .map(BaseQuestion::toResponse)
                    .orElseThrow(() -> new ConflictException(resourceBundle.getString("question-no-valid-type")));
        } catch (Throwable e) {
            baseQuestionService.delete(baseQuestion.getId());
            throw e;
        }
    }

    @PatchMapping("/question/{id}/approve")
    @ApiOperation(value = "Approve a question.",
            notes = "Admins and Mods have to approve a question before it's visible for all users.")
    public BaseQuestionResponse approveQuestion(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        // Only admins/mods are allowed to approve a question but mods can't approve a question which they updated on their own.
        boolean isUpdater = userDetails.getId().equals(baseQuestionService.getById(id).getUpdaterId());
        boolean isMod = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()));
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isAdmin && !isMod) {
            throw new ForbiddenException(resourceBundle.getString("approve-question-forbidden"));
        }
        if (isUpdater && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("approve-question-forbidden-after-update"));
        }

        return baseQuestionService.approve(id).toResponse();
    }

    @PatchMapping("/question/{id}")
    @ApiOperation(value = "Updates a question.",
            notes = "Change the values of a saved question.")
    public BaseQuestionResponse updateQuestion(HttpServletRequest httpRequest, @PathVariable Integer id,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        BaseQuestion toBeUpdated = baseQuestionService.getById(id);

        boolean isCreator = userDetails.getId().equals(toBeUpdated.getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only admins/mods and the initial creator are allowed to update a question.
        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("update-question-forbidden"));
        }

        String body = new BufferedReader(new InputStreamReader(httpRequest.getInputStream(), StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));

        UpdateQuestionRequest request = mapper.readValue(body, UpdateQuestionRequest.class);

        BaseQuestion baseQuestion = baseQuestionService.update(id, request.getText(), request.getAdditionalInformation(), request.getPoints(), request.getExamId(), request.getCourseId(), request.getOrigin(), request.getFile(), userDetails.getId());

        // If a question gets updated it has to be approved by mods/admins again.
        baseQuestionService.disapprove(id);

        return questionTypeMapperService.getServiceByType(baseQuestion.getType())
                .map(service -> service.genericUpdate(mapper, body, id))
                .map(BaseQuestion::toResponse)
                .orElseThrow(() -> new RuntimeException(resourceBundle.getString("question-no-valid-type")));
    }

    @GetMapping("/course/{courseId}/question")
    @ApiOperation(value = "Get questions by course.",
            notes = "Shows all questions that are linked to a specific course.")
    private List<BaseQuestionResponse> getQuestionByCourse(@PathVariable Integer courseId,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        Iterable<BaseQuestion> questions = baseQuestionService.findAllByCourse(courseId, isAdmin);
        List<BaseQuestionResponse> response = new ArrayList<>();
        for (BaseQuestion base : questions) {
            response.add(new BaseQuestionResponse(base));
        }

        return response;
    }

    @GetMapping("/exam/{examId}/question")
    @ApiOperation(value = "Get questions by exam.",
            notes = "Shows all questions that are included in a specific exam.")
    private List<BaseQuestionResponse> getQuestionsByExam(@PathVariable Integer examId,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        Iterable<BaseQuestion> questions = baseQuestionService.findAllByExam(examId, isAdmin);
        List<BaseQuestionResponse> response = new ArrayList<>();
        for (BaseQuestion base : questions) {
            response.add(new BaseQuestionResponse(base));
        }
        return response;
    }

    @GetMapping("/question")
    @ApiOperation(value = "Get a list of questions.")
    public PaginationResponse<BaseQuestionResponse> getQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(required = false) Boolean onlyApproved,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Integer semesterId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer moduleId,
            @RequestParam(required = false) Integer examId,
            @RequestParam(required = false) Integer tagId) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        List<BaseQuestion> questions = baseQuestionService.getByPagination(onlyApproved, searchTerm, semesterId, moduleId, courseId, examId, tagId, limit, skip, isAdmin);
        long count = baseQuestionService.getCount(onlyApproved, searchTerm, semesterId, moduleId, courseId, examId, tagId, isAdmin);

        return new PaginationResponse<>(
                count,
                questions.stream().map(BaseQuestionResponse::new).collect(Collectors.toList())
        );
    }

    @PutMapping("/session/{sessionId}/question/{questionId}")
    @ApiOperation(value = "Put a question into a session.",
            notes = "Puts a question by its id (type of the question is irrelevant) into an existing session.")
    public BaseQuestionResponse addQuestionToSession(@PathVariable Integer sessionId, @PathVariable Integer questionId,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isCreator = userDetails.getId().equals(sessionService.getById(sessionId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("add-question-to-session-forbidden"));
        }
        baseQuestionService.addQuestionToSession(sessionId, questionId);
        return new BaseQuestionResponse(baseQuestionService.getById(questionId));
    }

    @DeleteMapping("/session/{sessionId}/question/{questionId}")
    @ApiOperation(value = "Delete a question from a session.",
            notes = "Removes a question by its id (type of the question is irrelevant) from an existing session.")
    public ResponseEntity<Void> removeQuestionFromSession(@PathVariable Integer sessionId, @PathVariable Integer questionId,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isCreator = userDetails.getId().equals(sessionService.getById(sessionId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("remove-question-from-session-forbidden"));
        }
        baseQuestionService.removeQuestionFromSession(sessionId, questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session/{sessionId}/question")
    @ApiOperation(value = "Get questions by session.",
            notes = "Shows all questions that are included in a specific session.")
    private List<BaseQuestionResponse> getQuestionsBySession(@PathVariable Integer sessionId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCreator = userDetails.getId().equals(sessionService.getById(sessionId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));
        if (!isCreator && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-questions-by-session-forbidden"));
        }

        Iterable<BaseQuestion> questions = baseQuestionService.findAllBySession(sessionId);
        List<BaseQuestionResponse> response = new ArrayList<>();
        for (BaseQuestion base : questions) {
            response.add(new BaseQuestionResponse(base));
        }
        return response;
    }

    @GetMapping("/session/{sessionId}/question/{localId}")
    @ApiOperation(value = "Get question within a session.",
            notes = "Gets a question within a session by its local id within the session.")
    private BaseQuestionResponse getSessionQuestionByLocalId(@PathVariable Integer sessionId, @PathVariable Integer localId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCreator = userDetails.getId().equals(sessionService.getById(sessionId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));
        if (!isCreator && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-questions-by-session-forbidden"));
        }

        // Get base question
        BaseQuestion baseQuestion = baseQuestionService.findBySessionLocalId(sessionId, localId);
        // Get question from typeService
        return questionTypeMapperService.getServiceByType(baseQuestion.getType())
                .map(service -> service.getByQuestionId(baseQuestion.getId()))
                .map(BaseQuestion::toResponse)
                .orElse(new BaseQuestionResponse(baseQuestion));
    }
}
