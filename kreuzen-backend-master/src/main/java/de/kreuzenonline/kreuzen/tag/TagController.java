package de.kreuzenonline.kreuzen.tag;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.question.BaseQuestionServiceImpl;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.tag.requests.CreateTagRequest;
import de.kreuzenonline.kreuzen.tag.requests.UpdateTagRequest;
import de.kreuzenonline.kreuzen.tag.responses.TagResponse;
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
@Api(tags = "Tag")
public class TagController {

    private final TagService tagService;
    private final BaseQuestionServiceImpl baseQuestionService;
    private final ResourceBundle resourceBundle;

    public TagController(TagService tagService, BaseQuestionServiceImpl baseQuestionService, ResourceBundle resourceBundle) {
        this.tagService = tagService;
        this.baseQuestionService = baseQuestionService;
        this.resourceBundle = resourceBundle;
    }

    @PostMapping("/module/{moduleId}/tag")
    @ApiOperation(
            value = "Create tag",
            notes = "Creates a tag for a module. Multiple tags can be added to a module."
    )
    private TagResponse createTag(
            @PathVariable Integer moduleId,
            @Valid @RequestBody CreateTagRequest createTagRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));
        // Only Admins, Mods and Sudo can create a tag.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("create-tag-forbidden"));
        }

        Tag tag = tagService.create(createTagRequest.getName(), moduleId);

        return new TagResponse(tag);
    }

    @GetMapping("/tag/{id}")
    @ApiOperation(
            value = "Get tag.",
            notes = "Get a specific tag by its id."
    )
    private TagResponse getTag(@PathVariable Integer id,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));
        // Only Admins, Mods and Sudo can create a tag.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-tag-forbidden"));
        }

        Tag tag = tagService.findById(id);

        return new TagResponse(tag);
    }

    @GetMapping("/module/{moduleId}/tag")
    @ApiOperation(
            value = "Get tags.",
            notes = "Get a list of all tags that belong to a semester."
    )
    private List<TagResponse> getTagsByModule(@PathVariable Integer moduleId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Iterable<Tag> tags = tagService.findAllByModule(moduleId);
        List<TagResponse> response = new ArrayList<>();
        for (Tag tag : tags) {
            response.add(new TagResponse(tag));
        }

        return response;
    }

    @PatchMapping("/tag/{id}")
    @ApiOperation(
            value = "Updates a tag.",
            notes = "Just the name of the tag can be changed.")
    private TagResponse updateTag(@PathVariable Integer id,
                                  @Valid @RequestBody UpdateTagRequest request,
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins, Mods and Sudo can change a tag.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-tag-forbidden"));
        }

        Tag tag = tagService.update(id, request.getNewName());

        return new TagResponse(tag);
    }

    @DeleteMapping("/tag/{id}")
    @ApiOperation(
            value = "Deletes a tag.",
            notes = "Deletes a tag by the given id."
    )
    private ResponseEntity<Void> deleteTag(@PathVariable Integer id,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins, Mods and Sudo can delete a tag.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("delete-tag-forbidden"));
        }

        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/question/{questionId}/tag")
    @ApiOperation(value = "Get tags by question.")
    private List<TagResponse> getTagsByQuestion(@PathVariable Integer questionId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Iterable<Tag> tags = tagService.findAllByQuestion(questionId);
        List<TagResponse> response = new ArrayList<>();
        for (Tag tag : tags) {
            response.add(new TagResponse(tag));
        }

        return response;
    }

    @PutMapping("/question/{questionId}/tag/{tagId}")
    @ApiOperation(value = "Add a tag to the selected question")
    private TagResponse addTagToQuestion(@PathVariable Integer questionId, @PathVariable Integer tagId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCreator = userDetails.getId().equals(baseQuestionService.getById(questionId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins, Mods and Sudo can delete a tag.
        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("add-tag-to-question-forbidden"));
        }

        tagService.addTagToQuestion(questionId, tagId);
        return new TagResponse(tagService.findById(tagId));
    }

    @DeleteMapping("/question/{questionId}/tag/{tagId}")
    @ApiOperation(value = "Removes a tag from a question.")
    private ResponseEntity<Void> removeTagFromQuestion(@PathVariable Integer questionId, @PathVariable Integer tagId,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCreator = userDetails.getId().equals(baseQuestionService.getById(questionId).getCreatorId());
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Only Admins, Mods and Sudo can delete a tag.
        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(resourceBundle.getString("remove-tag-from-question-forbidden"));
        }

        tagService.removeTagFromQuestion(questionId, tagId);
        return ResponseEntity.noContent().build();
    }

}

