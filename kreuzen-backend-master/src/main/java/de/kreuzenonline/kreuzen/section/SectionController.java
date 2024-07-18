package de.kreuzenonline.kreuzen.section;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.section.requests.CreateSectionRequest;
import de.kreuzenonline.kreuzen.section.requests.UpdateSectionRequest;
import de.kreuzenonline.kreuzen.section.responses.SectionResponse;
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
@Api(tags = "Section")
public class SectionController {

    private final SectionService sectionService;
    private final ResourceBundle resourceBundle;

    public SectionController(SectionService sectionService, ResourceBundle resourceBundle) {
        this.sectionService = sectionService;
        this.resourceBundle = resourceBundle;
    }

    @GetMapping("major/{majorId}/section")
    @ApiOperation(
            value = "Get all sections by major.",
            notes = "Get all sections that a major can have."
    )
    private List<SectionResponse> getSectionsByMajor(@PathVariable Integer majorId) {

        Iterable<Section> sections = sectionService.findAllByMajor(majorId);
        List<SectionResponse> response = new ArrayList<>();
        for (Section section : sections) {
            response.add(new SectionResponse(section));
        }
        return response;
    }

    @GetMapping("module/{moduleId}/section")
    @ApiOperation(
            value = "Get all sections assigned to a module.",
            notes = "Get all sections that are assigned to a module."
    )
    private List<SectionResponse> getSectionsByModule(@PathVariable Integer moduleId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Iterable<Section> sections = sectionService.findAllByModule(moduleId);
        List<SectionResponse> response = new ArrayList<>();
        for (Section section : sections) {
            response.add(new SectionResponse(section));
        }
        return response;
    }

    @GetMapping("/section/{sectionId}")
    @ApiOperation(
            value = "Get section by Id."
    )
    private SectionResponse getSection(@PathVariable Integer sectionId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Section section = sectionService.findById(sectionId);
        return new SectionResponse(section);
    }

    @PostMapping("/major/{majorId}/section")
    @ApiOperation(
            value = "Create section",
            notes = "Creates a section for a major."
    )
    private SectionResponse createSection(@PathVariable Integer majorId, @Valid @RequestBody CreateSectionRequest createSectionRequest,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority((Roles.MODERATOR.getId())))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins, Mods and Sudo can create a section.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("create-section-forbidden"));
        }
        Section section = sectionService.create(
                majorId,
                createSectionRequest.getName());
        return new SectionResponse(section);
    }

    @PatchMapping("/section/{sectionId}")
    @ApiOperation(
            value = "Updates a section",
            notes = "All values are optional. If a value is set, then it is updated."
    )
    private SectionResponse updateSection(@PathVariable Integer sectionId, @Valid @RequestBody UpdateSectionRequest request,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can change a major.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-section-forbidden"));
        }
        Section section = sectionService.update(sectionId, request.getName());
        return new SectionResponse(section);
    }

    @DeleteMapping("/section/{sectionId}")
    @ApiOperation(
            value = "Deletes a section."
    )
    private ResponseEntity<Void> deleteSection(@PathVariable Integer sectionId,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins and Sudo can delete a section.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-section-forbidden"));
        }
        sectionService.delete(sectionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/major/{majorId}/section")
    @ApiOperation(
            value = "Get sections by user and major."
    )
    private List<SectionResponse> getSectionsByUser(@PathVariable Integer userId, @PathVariable Integer majorId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCurrentUser = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.getId()))
                && userDetails.getId().equals(userId);
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Users can only view their own sections. Admins and Sudo can view all users' sections.
        if (!isCurrentUser && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-user-sections-forbidden"));
        }

        List<Section> sectionsByUser = sectionService.getSectionsByUserAndMajor(userId, majorId);
        List<SectionResponse> responseList = new ArrayList<>();

        for (Section section : sectionsByUser) {
            responseList.add(new SectionResponse(section));
        }
        return responseList;
    }

    @PutMapping("/user/{userId}/major/{majorId}/section/{sectionId}")
    @ApiOperation(
            value = "Add a section to the sections a user pursues"
    )
    private SectionResponse addSectionToUser(@PathVariable Integer userId, @PathVariable Integer majorId, @PathVariable Integer sectionId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCurrentUser = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.getId()))
                && userDetails.getId().equals(userId);
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Users can only edit their own sections. Admins and Sudo can edit all users' sections.
        if (!isCurrentUser && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("edit-user-sections-forbidden"));
        }

        sectionService.addSectionToUser(userId, majorId, sectionId);
        return new SectionResponse(sectionService.findById(sectionId));
    }

    @DeleteMapping("/user/{userId}/major/{majorId}/section/{sectionId}")
    @ApiOperation(
            value = "Remove section from a user."
    )
    private ResponseEntity<Void> removeSectionFromUser(@PathVariable Integer userId, @PathVariable Integer majorId, @PathVariable Integer sectionId,
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
            throw new ForbiddenException(resourceBundle.getString("edit-user-sections-forbidden"));
        }
        sectionService.removeSectionFromUser(userId, majorId, sectionId);
        return ResponseEntity.noContent().build();
    }

}
