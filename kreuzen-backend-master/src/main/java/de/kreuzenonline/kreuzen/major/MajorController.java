package de.kreuzenonline.kreuzen.major;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.major.requests.CreateMajorRequest;
import de.kreuzenonline.kreuzen.major.requests.UpdateMajorRequest;
import de.kreuzenonline.kreuzen.major.responses.MajorResponse;
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
@Api(tags = "Major")
public class MajorController {

    private final MajorService majorService;
    private final ResourceBundle resourceBundle;

    public MajorController(MajorService majorService, ResourceBundle resourceBundle) {
        this.majorService = majorService;
        this.resourceBundle = resourceBundle;
    }

    @GetMapping("/university/{uniId}/major")
    @ApiOperation(
            value = "Get all majors by university",
            notes = "Get all majors that a university offers."
    )
    private List<MajorResponse> getMajorsByUniversity(@PathVariable Integer uniId) {

        Iterable<Major> majors = majorService.findAllByUniversity(uniId);
        List<MajorResponse> response = new ArrayList<>();
        for (Major major : majors) {
            response.add(new MajorResponse(major));
        }

        return response;
    }

    @PostMapping("/university/{uniId}/major")
    @ApiOperation(
            value = "Create major",
            notes = "Creates a major for a university. Majors are bound to one - and only one - university."
    )
    private MajorResponse createMajor(
            @PathVariable Integer uniId,
            @Valid @RequestBody CreateMajorRequest createMajorRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority((Roles.MODERATOR.getId())))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins, Mods and Sudo can create a major.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("create-major-forbidden"));
        }

        Major major = majorService.create(
                uniId,
                createMajorRequest.getName()
        );

        return new MajorResponse(major);
    }

    @GetMapping("/major/{id}")
    @ApiOperation(
            value = "Get major",
            notes = "Get the data of a major."
    )
    private MajorResponse getMajor(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Major major = majorService.findById(id);

        return new MajorResponse(major);
    }

    @PatchMapping("/major/{id}")
    @ApiOperation(
            value = "Updates a major",
            notes = "All values are optional. If a value is set, then it is updated."
    )
    private MajorResponse updateMajor(@PathVariable Integer id, @Valid @RequestBody UpdateMajorRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can change a major.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-major-forbidden"));
        }

        Major major = majorService.update(id, request.getName());

        return new MajorResponse(major);
    }

    @DeleteMapping("/major/{id}")
    @ApiOperation(value = "Remove a major from a university.")
    private ResponseEntity<Void> deleteMajor(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can delete a major.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-major-forbidden"));
        }
        majorService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/major")
    @ApiOperation(
            value = "Get majors by user",
            notes = "Get the majors that a certain user chose."
    )
    private List<MajorResponse> getMajorsByUser(@PathVariable Integer userId, @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCurrentUser = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.getId()))
                && userDetails.getId().equals(userId);
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Users can only view their own majors. Admins and Sudo can edit all users' majors.
        if (!isCurrentUser && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-user-majors-forbidden"));
        }
        List<Major> majorsByUser = majorService.getMajorsByUser(userId);
        List<MajorResponse> responseList = new ArrayList<>();

        for (Major major : majorsByUser) {
            responseList.add(new MajorResponse(major));
        }

        return responseList;
    }

    @GetMapping("/module/{moduleId}/major")
    @ApiOperation(
            value = "Get majors by module",
            notes = "Get the majors that are assigned to a module."
    )
    private List<MajorResponse> getMajorsByModule(@PathVariable Integer moduleId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Iterable<Major> majors = majorService.findAllByModule(moduleId);
        List<MajorResponse> responseList = new ArrayList<>();

        for (Major major : majors) {
            responseList.add(new MajorResponse(major));
        }

        return responseList;
    }

    @PutMapping("/user/{userId}/major/{majorId}")
    @ApiOperation(
            value = "Add a major to the majors a user pursues",
            notes = "Calling this endpoint more than one time should have no effect as put is used. " +
                    "No body is required. When called, the specified major is added to the list of a user."
    )
    private MajorResponse addMajorToUser(@PathVariable Integer userId, @PathVariable Integer majorId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCurrentUser = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.getId()))
                && userDetails.getId().equals(userId);
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Users can only edit their own majors. Admins and Sudo can edit all users' majors.
        if (!isCurrentUser && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("edit-user-majors-forbidden"));
        }

        majorService.addMajorToUser(userId, majorId);
        return new MajorResponse(majorService.findById(majorId));
    }

    @DeleteMapping("/user/{userId}/major/{majorId}")
    @ApiOperation(
            value = "Removes a major from a user",
            notes = "It is not checked whether the user selected the major or not. " +
                    "If the major was selected then it is removed, else nothing happens."
    )
    private ResponseEntity<Void> removeMajorFromUser(@PathVariable Integer userId, @PathVariable Integer majorId,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCurrentUser = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.getId()))
                && userDetails.getId().equals(userId);
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Users can only edit their own majors. Admins and Sudo can edit all users' majors.
        if (!isCurrentUser && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("edit-user-majors-forbidden"));
        }

        majorService.removeMajorFromUser(userId, majorId);
        return ResponseEntity.noContent().build();
    }
}
