package de.kreuzenonline.kreuzen.course;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.course.requests.CreateCourseRequest;
import de.kreuzenonline.kreuzen.course.requests.UpdateCourseRequest;
import de.kreuzenonline.kreuzen.course.responses.CourseResponse;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
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
@Api(tags = "Course")
public class CourseController {

    private final CourseService courseService;
    private final ResourceBundle resourceBundle;

    public CourseController(CourseService courseService, ResourceBundle resourceBundle) {
        this.courseService = courseService;
        this.resourceBundle = resourceBundle;
    }

    @GetMapping("/course/{id}")
    @ApiOperation(
            value = "Get course",
            notes = "Get the data of a course."
    )
    private CourseResponse getCourse(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Course course = courseService.findById(id);

        return new CourseResponse(course);
    }

    @PostMapping("/module/{moduleId}/course")
    @ApiOperation(
            value = "Create course",
            notes = "Creates a course for a module. Multiple courses can be added to a module."
    )
    private CourseResponse createCourse(
            @PathVariable Integer moduleId,
            @Valid @RequestBody CreateCourseRequest createCourseRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));
        // Only Admins, Mods and Sudo can create a course.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("create-course-forbidden"));
        }

        Course course = courseService.create(moduleId, createCourseRequest.getSemesterId());

        return new CourseResponse(course);
    }

    @GetMapping("module/{moduleId}/course")
    @ApiOperation(
            value = "Get all courses of a module."
    )
    private List<CourseResponse> getCoursesByModule(@PathVariable Integer moduleId,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Iterable<Course> courses = courseService.findAllByModule(moduleId);
        List<CourseResponse> response = new ArrayList<>();
        for (Course course : courses) {
            response.add(new CourseResponse(course));
        }

        return response;
    }

    @PatchMapping("/course/{id}")
    @ApiOperation(
            value = "Updates a course.",
            notes = "All values are optional. If a value is set, then it is updated."
    )
    private CourseResponse updateCourse(@PathVariable Integer id,
                                        @Valid @RequestBody UpdateCourseRequest request,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can change a major.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-course-forbidden"));
        }

        Course course = courseService.update(id, request.getModuleId(), request.getSemesterId());

        return new CourseResponse(course);
    }


    @DeleteMapping("/course/{id}")
    @ApiOperation(
            value = "Deletes a course."
    )
    private ResponseEntity<Void> deleteCourse(@PathVariable Integer id,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can delete a course.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("delete-course-forbidden"));
        }
        courseService.delete(id);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/semester/{semesterId}/course")
    @ApiOperation(
            value = "Get courses by semester.",
            notes = "Gets all courses that are available for a specific semester."
    )
    private List<CourseResponse> getCoursesBySemester(@PathVariable Integer semesterId,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Iterable<Course> courses = courseService.findAllBySemester(semesterId);
        List<CourseResponse> response = new ArrayList<>();
        for (Course course : courses) {
            response.add(new CourseResponse(course));
        }

        return response;
    }
}
