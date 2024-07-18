package de.kreuzenonline.kreuzen.university;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.university.requests.CreateUniversityRequest;
import de.kreuzenonline.kreuzen.university.requests.UpdateUniversityRequest;
import de.kreuzenonline.kreuzen.university.responses.UniversityResponse;
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
@RequestMapping("/university")
@Api(tags = "University")
public class UniversityController {

    private final UniversityService universityService;
    private final ResourceBundle resourceBundle;

    public UniversityController(UniversityService universityService, ResourceBundle resourceBundle) {
        this.universityService = universityService;
        this.resourceBundle = resourceBundle;
    }

    @GetMapping
    @ApiOperation(
            value = "Get all universities",
            notes = "Get all registered universities."
    )
    public List<UniversityResponse> getAllUniversities(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Iterable<University> universities = universityService.getAll();
        List<UniversityResponse> responses = new ArrayList<>();
        for (University u : universities) {
            responses.add(new UniversityResponse(u));
        }

        return responses;
    }

    @GetMapping("/{id}")
    @ApiOperation(
            value = "Get university",
            notes = "Get a specific university by its id."
    )
    public UniversityResponse getUniversity(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        University university = universityService.getById(id);

        return new UniversityResponse(university);
    }

    @PostMapping
    @ApiOperation(
            value = "Create university",
            notes = "Create a new university."
    )
    public UniversityResponse createUniversity(@Valid @RequestBody CreateUniversityRequest request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can create a university.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("create-university-forbidden"));
        }

        University university = universityService.create(request.getName(), request.getAllowedDomains());

        return new UniversityResponse(university);
    }

    @PatchMapping("/{id}")
    @ApiOperation(
            value = "Update university",
            notes = "Update the information of a university. All values are optional. If a value is set, then it is updated."
    )
    public UniversityResponse updateUniversity(@PathVariable Integer id, @Valid @RequestBody UpdateUniversityRequest request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can change a university.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-university-forbidden"));
        }

        University university = universityService.update(id, request.getName(), request.getAllowedDomains());

        return new UniversityResponse(university);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(
            value = "Delete university",
            notes = "This also deletes all the data that is dependent on the university, like their users and majors."
    )
    public ResponseEntity<Void> deleteUniversity(@PathVariable Integer id,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can delete a university.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-university-forbidden"));
        }
        universityService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
