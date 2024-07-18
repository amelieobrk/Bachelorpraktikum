package de.kreuzenonline.kreuzen.semester;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.semester.requests.CreateSemesterRequest;
import de.kreuzenonline.kreuzen.semester.requests.DeleteSemesterRequest;
import de.kreuzenonline.kreuzen.semester.requests.UpdateSemesterRequest;
import de.kreuzenonline.kreuzen.semester.responses.SemesterResponse;
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

@RestController
@RequestMapping("/semester")
@Api(tags = "Semester")
public class SemesterController {

    private final SemesterService semesterService;
    private final PasswordEncoder passwordEncoder;
    private final ResourceBundle resourceBundle;

    public SemesterController(SemesterService semesterService, PasswordEncoder passwordEncoder, ResourceBundle resourceBundle) {
        this.semesterService = semesterService;
        this.passwordEncoder = passwordEncoder;
        this.resourceBundle = resourceBundle;
    }

    @GetMapping
    @ApiOperation(value = "Get all semesters")
    public List<SemesterResponse> getAllSemesters(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Iterable<Semester> semesters = semesterService.getAll();
        List<SemesterResponse> responses = new ArrayList<>();
        for (Semester s : semesters) {
            responses.add(new SemesterResponse(s));
        }
        return responses;
    }

    @GetMapping("/{id}")
    @ApiOperation(
            value = "Get semester",
            notes = "Gets a specific semester by its id.")
    public SemesterResponse getSemester(@PathVariable Integer id,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Mods, Admins and Sudo can get a semester.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-a-semester-forbidden"));
        }
        Semester semester = semesterService.getById(id);

        return new SemesterResponse(semester);
    }

    @PostMapping
    @ApiOperation(
            value = "Create semester",
            notes = "Creates a new semester."
    )
    public SemesterResponse createSemester(@Valid @RequestBody CreateSemesterRequest request,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Mods, Admins and Sudo can create a semester.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("create-semester-forbidden"));
        }

        Semester semester = semesterService.create(request.getName(), request.getStartYear(), request.getEndYear());

        return new SemesterResponse(semester);
    }

    @PatchMapping("/{id}")
    @ApiOperation(
            value = "Update semester",
            notes = "Update the information for one semester. All values are optional. If a value is set, then it is updated."
    )
    public SemesterResponse updateSemester(@PathVariable Integer id, @Valid @RequestBody UpdateSemesterRequest request,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can change a semester.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-semester-forbidden"));
        }

        Semester semester = semesterService.update(id, request.getName(), request.getStartYear(), request.getEndYear());

        return new SemesterResponse(semester);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(
            value = "Delete semester",
            notes = "Deletes a specific semester by its id"
    )
    public ResponseEntity<Void> deleteSemester(@PathVariable Integer id, @Valid @RequestBody DeleteSemesterRequest request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        request.setId(id);
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can delete a semester.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-semester-forbidden"));
        }

        // To delete a password, the Administrator has to re-enter his password for security purposes.
        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new ForbiddenException(resourceBundle.getString("delete-semester-password-wrong"));
        }

        semesterService.delete(request.getId());

        return ResponseEntity.noContent().build();

    }
}
