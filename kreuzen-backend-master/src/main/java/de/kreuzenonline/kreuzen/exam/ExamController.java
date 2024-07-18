package de.kreuzenonline.kreuzen.exam;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exam.requests.CreateExamRequest;
import de.kreuzenonline.kreuzen.exam.requests.DeleteExamRequest;
import de.kreuzenonline.kreuzen.exam.requests.UpdateExamRequest;
import de.kreuzenonline.kreuzen.exam.responses.ExamResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@RestController
@Api(tags = "Exam")
public class ExamController {

    private final ExamService examService;
    private final ResourceBundle resourceBundle;
    private final PasswordEncoder passwordEncoder;

    public ExamController(ExamService examService, ResourceBundle resourceBundle, PasswordEncoder passwordEncoder) {
        this.examService = examService;
        this.resourceBundle = resourceBundle;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/exam/{id}")
    @ApiOperation(
            value = "Get exam",
            notes = "Get a specific exam by its id."
    )
    public ExamResponse getExam(@PathVariable Integer id,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Exam exam = examService.getById(id);

        return new ExamResponse(exam);
    }

    @PostMapping("/course/{courseId}/exam")
    @ApiOperation(
            value = "Create exam",
            notes = "Create a new exam."
    )
    public ExamResponse createExam(@PathVariable Integer courseId, @Valid @RequestBody CreateExamRequest request,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Exam exam = examService.create(request.getName(), courseId, request.getDate(), false, request.getIsRetry());
        return new ExamResponse(exam);
    }

    @PatchMapping("/exam/{id}")
    @ApiOperation(
            value = "Update exam",
            notes = "Update the information of an exam. All values are optional. If a value is set, then it is updated."
    )
    public ExamResponse updateExam(@PathVariable Integer id, @Valid @RequestBody UpdateExamRequest request,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-exam-forbidden"));
        }

        Exam exam = examService.update(id, request.getName(), request.getCourseId(), request.getDate(), request.getIsComplete(), request.getIsRetry());

        return new ExamResponse(exam);
    }

    @DeleteMapping("exam/{id}")
    @ApiOperation(
            value = "Delete exam",
            notes = "Deletes a specific exam by its id"
    )
    public ResponseEntity<Void> deleteExam(@PathVariable Integer id, @Valid @RequestBody DeleteExamRequest request,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {

        request.setId(id);
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("delete-exam-forbidden"));
        }


        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new ForbiddenException(resourceBundle.getString("delete-exam-password-wrong"));
        }

        examService.delete(request.getId());

        return ResponseEntity.noContent().build();

    }

    @GetMapping("/university/{uniId}/exam")
    @ApiOperation(
            value = "Get exams by university"

    )
    private List<ExamResponse> getExamsByUniversity(
            @PathVariable Integer uniId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "-1") Integer semester,
            @RequestParam(required = false, defaultValue = "-1") Integer major) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (uniId != userDetails.getUniversityId()) {
            throw new ForbiddenException(resourceBundle.getString("get-exams-by-university-forbidden-not-own-university"));
        }

        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-exams-by-university-forbidden"));
        }


        Iterable<Exam> exams;
        if (semester != -1) {
            if (major != -1) {
                exams = examService.getExamsByUniversityAndMajorAndSemester(uniId, major, semester);
            } else {
                exams = examService.getExamsBySemesterAndUniversity(semester, uniId);
            }
        } else {
            if (major != -1) {
                exams = examService.getExamsByUniversityAndMajor(uniId, major);
            } else {
                exams = examService.getExamsByUniversity(uniId);
            }
        }

        List<ExamResponse> response = new ArrayList<>();
        for (Exam exam : exams) {
            response.add(new ExamResponse(exam));
        }

        return response;
    }

    @GetMapping("/course/{courseId}/exam")
    @ApiOperation(
            value = "Get exams by course"

    )
    private List<ExamResponse> getExamsByCourse(@PathVariable Integer courseId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-exams-by-course-forbidden"));
        }


        Iterable<Exam> exams = examService.getExamsByCourse(courseId);
        List<ExamResponse> response = new ArrayList<>();
        for (Exam exam : exams) {
            response.add(new ExamResponse(exam));
        }

        return response;
    }

    @GetMapping("/module/{moduleId}/exam")
    @ApiOperation(
            value = "Get exams by module"

    )
    private List<ExamResponse> getExamsByModule(@PathVariable Integer moduleId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));


        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-exams-by-module-forbidden"));
        }


        Iterable<Exam> exams = examService.getExamsByModule(moduleId);
        List<ExamResponse> response = new ArrayList<>();
        for (Exam exam : exams) {
            response.add(new ExamResponse(exam));
        }

        return response;
    }


}
