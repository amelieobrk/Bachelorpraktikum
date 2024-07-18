package de.kreuzenonline.kreuzen.hint;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.hint.requests.CreateHintRequest;
import de.kreuzenonline.kreuzen.hint.requests.UpdateHintRequest;
import de.kreuzenonline.kreuzen.hint.responses.HintResponse;
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
@Api(tags = "Hint")
public class HintController {

    private final HintService hintService;
    private final ResourceBundle resourceBundle;

    public HintController(HintService hintService, ResourceBundle resourceBundle) {
        this.hintService = hintService;
        this.resourceBundle = resourceBundle;
    }

    @GetMapping("/hint/{id}")
    @ApiOperation(value = "Get a hint by its id.")
    public HintResponse getHint(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        Hint hint = hintService.getById(id);

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()));

        // Only admins/mods are allowed to access a hint by its id.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-hint-forbidden"));
        }
        return new HintResponse(hint);
    }

    @GetMapping("/hint/random")
    @ApiOperation(value = "Get a random hint.",
            notes = "Function gets one random hint out of all the hints, that are set active.")
    public HintResponse getRandomHint(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        Hint hint = hintService.getRandomHint();

        return new HintResponse(hint);
    }

    @GetMapping("/hint")
    @ApiOperation(value = "Get all hints")
    public List<HintResponse> getAllHints(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()));

        // Only admins/mods are allowed to access the list of all hints.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("get-all-hints-forbidden"));
        }

        Iterable<Hint> hints = hintService.getAll();
        List<HintResponse> responses = new ArrayList<>();
        for (Hint hint : hints) {
            responses.add(new HintResponse(hint));
        }
        return responses;
    }

    @PostMapping("/hint")
    @ApiOperation(value = "Creates a hint.")
    public HintResponse createHint(@Valid @RequestBody CreateHintRequest request,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("create-hint-forbidden"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()));

        // Only admins/mods are allowed to update a hint.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("create-hint-forbidden"));
        }

        Hint hint = hintService.create(request.getText(), request.getIsActive());

        return new HintResponse(hint);
    }

    @PatchMapping("/hint/{id}")
    @ApiOperation(value = "Updates a hint.",
            notes = "Updates a hint with a specific id, e.g. to change the text or to set it (in)active.")
    public HintResponse updateHint(@PathVariable Integer id,
                                   @Valid @RequestBody UpdateHintRequest request,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()));

        // Only admins/mods are allowed to update a hint.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-hint-forbidden"));
        }

        Hint hint = hintService.update(id, request.getText(), request.getIsActive());

        return new HintResponse(hint);

    }

    @DeleteMapping("/hint/{id}")
    @ApiOperation(value = "Deletes a hint by its id.")
    public ResponseEntity<Void> deleteHint(@PathVariable Integer id,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()));

        // Only admins/mods are allowed to delete a hint.
        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("delete-hint-forbidden"));
        }

        hintService.delete(id);

        return ResponseEntity.noContent().build();

    }
}
