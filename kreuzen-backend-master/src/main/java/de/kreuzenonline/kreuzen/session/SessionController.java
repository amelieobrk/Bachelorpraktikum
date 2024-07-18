package de.kreuzenonline.kreuzen.session;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.BaseQuestionService;
import de.kreuzenonline.kreuzen.question.types.QuestionTypeMapperService;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceService;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceService;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.session.requests.CreateSessionRequest;
import de.kreuzenonline.kreuzen.session.requests.SetSelectionRequest;
import de.kreuzenonline.kreuzen.session.requests.SetTimeRequest;
import de.kreuzenonline.kreuzen.session.requests.UpdateSessionRequest;
import de.kreuzenonline.kreuzen.session.responses.*;
import de.kreuzenonline.kreuzen.session.selections.MultipleChoiceSelection;
import de.kreuzenonline.kreuzen.session.selections.SingleChoiceSelection;
import de.kreuzenonline.kreuzen.utils.PaginationResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Api(tags = "Session")
public class SessionController {

    private final BaseQuestionService baseQuestionService;
    private final SessionService sessionService;
    private final ResourceBundle resourceBundle;
    private final SingleChoiceService singleChoiceService;
    private final MultipleChoiceService multipleChoiceService;
    private final QuestionTypeMapperService questionTypeMapperService;

    public SessionController(BaseQuestionService baseQuestionService, SessionService sessionService, ResourceBundle resourceBundle, SingleChoiceService singleChoiceService, MultipleChoiceService multipleChoiceService, QuestionTypeMapperService questionTypeMapperService) {
        this.baseQuestionService = baseQuestionService;
        this.sessionService = sessionService;
        this.resourceBundle = resourceBundle;
        this.singleChoiceService = singleChoiceService;
        this.multipleChoiceService = multipleChoiceService;
        this.questionTypeMapperService = questionTypeMapperService;
    }

    @GetMapping("/session/{id}")
    @ApiOperation(
            value = "Get session",
            notes = "Get a specific session by its id."
    )
    public SessionResponse getSession(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Session session = sessionService.getById(id);

        boolean isCreator = userDetails.getId().equals(session.getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only admins/mods and the initial creator are allowed to access the session.
        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("get-session-forbidden"));
        }

        return new SessionResponse(session);
    }

    @PostMapping("/session")
    @ApiOperation(
            value = "Create session",
            notes = "Create a new session"
    )
    public SessionResponse createSession(@Valid @RequestBody CreateSessionRequest request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Session session = sessionService.create(
                userDetails.getId(),
                request.getName(),
                request.getSessionType(),
                request.getIsRandom(),
                request.getNotes(),
                request.getModuleIds(),
                request.getSemesterIds(),
                request.getTagIds(),
                request.getQuestionTypes(),
                request.getQuestionOrigins(),
                request.getFilterTerm()
        );
        return new SessionResponse(session);
    }

    @PatchMapping("/session/{id}")
    @ApiOperation(
            value = "Update session",
            notes = "Update an existing session"
    )
    public SessionResponse updateSession(@PathVariable Integer id, @Valid @RequestBody UpdateSessionRequest request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Session session = sessionService.getById(id);

        boolean isCreator = userDetails.getId().equals(session.getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only admins/mods and the initial creator are allowed to access the session.
        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("update-session-forbidden"));
        }

        session = sessionService.update(id, request.getName(), request.getSessionType(), request.getIsRandom(), request.getNotes());

        return new SessionResponse(session);
    }

    @DeleteMapping("session/{id}")
    @ApiOperation(value = "Delete session",
            notes = "Deletes specific session by its id")
    public ResponseEntity<Void> deleteSession(@PathVariable Integer id,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Session session = sessionService.getById(id);

        boolean isCreator = userDetails.getId().equals(session.getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only admins/mods and the initial creator are allowed to access the session.
        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("delete-session-forbidden"));
        }

        sessionService.delete(id);

        return ResponseEntity.noContent().build();

    }

    @GetMapping("/session/{sessionId}/question/{localId}/selection")
    @ApiOperation(value = "Get user's answer for a question.",
            notes = "For every question the user has to select at least one answer. This function gets the selected answer.")
    public List<Object> getSelection(@PathVariable Integer sessionId, @PathVariable Integer localId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        BaseQuestion question = baseQuestionService.findBySessionLocalId(sessionId, localId);

        boolean isCreator = userDetails.getId().equals(sessionService.getById(sessionId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only admins/mods and the initial creator are allowed to access the session.
        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("get-question-selection-forbidden"));
        }

        List<Object> selectionResponse = new ArrayList<>();
        switch (question.getType()) {
            case "single-choice":
                Iterable<SingleChoiceSelection> singleChoiceSelections = sessionService.findAllSingleChoiceSelections(sessionId, localId);
                for (SingleChoiceSelection selection : singleChoiceSelections) {
                    selectionResponse.add(new SingleChoiceSelectionResponse(selection));
                }
                return selectionResponse;
            case "multiple-choice":
                Iterable<MultipleChoiceSelection> multipleChoiceSelections = sessionService.findAllMultipleChoiceSelections(sessionId, localId);
                for (MultipleChoiceSelection selection : multipleChoiceSelections) {

                    selectionResponse.add(new MultipleChoiceSelectionResponse(selection));
                }
                return selectionResponse;
            default:
                selectionResponse.add(new ConflictException(resourceBundle.getString("question-no-valid-type")));
                return selectionResponse;
        }
    }


    @GetMapping("/session/{id}/results")
    @ApiOperation(value = "Get results of session")
    public List<QuestionResultResponse> getResult(@PathVariable Integer id,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCreator = userDetails.getId().equals(sessionService.getById(id).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("get-question-result-forbidden"));
        }

        Iterable<SessionQuestion> questions = sessionService.getAllSessionQuestions(id);
        List<QuestionResultResponse> responses = new ArrayList<>();
        for (SessionQuestion question : questions) {
            BaseQuestion q = baseQuestionService.getById(question.getQuestionId()); // for better performance just fetch the type of the question.
            switch (q.getType()) {
                case "single-choice":
                    QuestionResultResponse singleResponse = sessionService.singleChoiceResult(id, singleChoiceService.getByQuestionId(q.getId()), question.getLocalId());
                    responses.add(singleResponse);
                    break;

                case "multiple-choice":
                    QuestionResultResponse multipleResponse = sessionService.multipleChoiceResult(id, multipleChoiceService.getByQuestionId(q.getId()), question.getLocalId());
                    responses.add(multipleResponse);
                    break;

                default:
                    responses.add(new QuestionResultResponse(id, q, 0, question.getLocalId()));
                    break;
            }
        }

        responses.sort((o1, o2) -> o1.getLocalId() - o2.getLocalId());

        return responses;
    }

    @PatchMapping("/session/{id}/reset")
    @ApiOperation(
            value = "Resets given answers"
    )
    public SessionResponse resetSelection(@PathVariable Integer id,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Session session = sessionService.getById(id);

        boolean isCreator = userDetails.getId().equals(session.getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("reset-selection-forbidden"));
        }

        session = sessionService.resetSelection(id);

        return new SessionResponse(session);
    }

    @GetMapping("/user/{userId}/session")
    @ApiOperation(value = "Get sessions by user.")
    public PaginationResponse<SessionResponse> getSessionByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Integer userId,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer skip) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCreator = userDetails.getId().equals(userId);
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("get-sessions-by-user-forbidden"));
        }
        List<Session> sessions;

        Integer count;
        sessions = sessionService.getByPagination(userId, limit, skip);
        count = sessionService.getCountByUser(userId);

        return new PaginationResponse<>(
                count,
                sessions.stream().map(SessionResponse::new).collect(Collectors.toList())
        );
    }

    @PutMapping("/session/{sessionId}/question/{localId}/selection")
    @ApiOperation(
            value = "Add selection to question in session"
    )
    private List<Object> addSelection(@Valid @RequestBody SetSelectionRequest request, @PathVariable Integer sessionId, @PathVariable Integer localId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Session session = sessionService.getById(sessionId);
        boolean isCreator = userDetails.getId().equals(session.getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("add-selection-forbidden"));
        }

        if (request.getCrossedLocalAnswerIds() == null) {
            request.setCrossedLocalAnswerIds(new Integer[]{});
        }
        List<Object> selectionResponse = new ArrayList<>();
        switch (request.getType()) {
            case "single-choice":
                sessionService.addSingleChoiceSelection(sessionId, localId, request.getCheckedLocalAnswerId(), request.getCrossedLocalAnswerIds());
                Iterable<SingleChoiceSelection> singleChoiceSelections = sessionService.findAllSingleChoiceSelections(sessionId, localId);
                for (SingleChoiceSelection selection : singleChoiceSelections) {
                    selectionResponse.add(new SingleChoiceSelectionResponse(selection));
                }
                return selectionResponse;
            case "multiple-choice":
                if (request.getCheckedLocalAnswerIds() == null) {
                    request.setCheckedLocalAnswerIds(new Integer[]{});
                }
                sessionService.addMultipleChoiceSelection(sessionId, localId, request.getCheckedLocalAnswerIds(), request.getCrossedLocalAnswerIds());
                Iterable<MultipleChoiceSelection> multipleChoiceSelections = sessionService.findAllMultipleChoiceSelections(sessionId, localId);
                for (MultipleChoiceSelection selection : multipleChoiceSelections) {
                    selectionResponse.add(new MultipleChoiceSelectionResponse(selection));
                }
                return selectionResponse;
            default:
                selectionResponse.add(new ConflictException(resourceBundle.getString("question-no-valid-type")));
                return selectionResponse;
        }
    }

    @PutMapping("/session/{sessionId}/question/{localId}/time")
    @ApiOperation(
            value = "Add time to question in session",
            notes = "As long as a user answers a question within a session a PUT request with the cumulated answering time is sent." +
                    "Last submitted time shows how long it took to find the supposed correct answer(s)."
    )
    private SessionQuestionResponse addTime(@Valid @RequestBody SetTimeRequest request, @PathVariable Integer sessionId, @PathVariable Integer localId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Session session = sessionService.getById(sessionId);
        boolean isCreator = userDetails.getId().equals(session.getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("add-time-forbidden"));
        }
        return new SessionQuestionResponse(sessionService.addTime(sessionId, localId, request.getTime()));
    }

    @PatchMapping("/session/{sessionId}/question/{localId}/submit")
    @ApiOperation(value = "Submit a question within a session.",
            notes = "After answering a question it can be set to submitted.")
    public SessionQuestionResponse submitQuestion(@PathVariable Integer sessionId,
                                                  @PathVariable Integer localId,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isCreator = userDetails.getId().equals(sessionService.getById(sessionId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("submit-question-selection-forbidden"));
        }
        return new SessionQuestionResponse(sessionService.submitQuestion(sessionId, localId));
    }

    @PatchMapping("/session/{sessionId}/submit")
    @ApiOperation(value = "Submit a session.",
            notes = "Marks the session completed and submits all questions.")
    public SessionResponse submitSession(@PathVariable Integer sessionId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isCreator = userDetails.getId().equals(sessionService.getById(sessionId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("submit-question-selection-forbidden"));
        }
        return new SessionResponse(sessionService.finishSession(sessionId));
    }

    @GetMapping("/session/{sessionId}/question/{localId}/status")
    @ApiOperation(value = "Get the status of a question.")
    public SessionQuestionResponse getQuestionStatus(@PathVariable Integer sessionId,
                                                     @PathVariable Integer localId,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isCreator = userDetails.getId().equals(sessionService.getById(sessionId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("get-question-status-forbidden"));
        }
        return new SessionQuestionResponse(sessionService.getQuestionBySessionAndLocalId(sessionId, localId));
    }

    @GetMapping("/session/question/count")
    @ApiOperation(value = "Get number of questions by criteria",
            notes = "Gets the count of numbers that match with certain defined criteria.")
    public QuestionCountResponse getQuestionCountWithParameters(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                @RequestParam(required = false) Integer[] moduleIds,
                                                                @RequestParam(required = false) Integer[] semesterIds,
                                                                @RequestParam(required = false) Integer[] tagIds,
                                                                @RequestParam(required = false) String[] questionTypes,
                                                                @RequestParam(required = false) String[] questionOrigins,
                                                                @RequestParam(required = false) String textFilter) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        return new QuestionCountResponse(sessionService.getCountByParameters(moduleIds, semesterIds, tagIds, questionTypes, questionOrigins, textFilter));
    }

    @GetMapping("/session/{id}/question/count")
    @ApiOperation(value = "Get number of questions in session")
    public QuestionCountResponse getQuestionCountWithParameters(@PathVariable Integer id,
                                                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCreator = userDetails.getId().equals(sessionService.getById(id).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("get-question-count-with-parameters-forbidden"));
        }

        return new QuestionCountResponse(sessionService.getCount(id));
    }

}


