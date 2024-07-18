package de.kreuzenonline.kreuzen.user;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.exceptions.BadRequestException;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.user.requests.UpdateUserRequest;
import de.kreuzenonline.kreuzen.user.responses.UserResponse;
import de.kreuzenonline.kreuzen.utils.PaginationResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@Api(tags = "User")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ResourceBundle resourceBundle;

    public UserController(UserService userService, PasswordEncoder passwordEncoder, ResourceBundle resourceBundle) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.resourceBundle = resourceBundle;
    }

    @GetMapping("/{userId}")
    @ApiOperation(
            value = "Get user by Id."
    )
    public UserResponse getUser(@PathVariable Integer userId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }
        User user = userService.getById(userId);
        return new UserResponse(user);
    }

    @GetMapping
    @ApiOperation(
            value = "Get a list of users."
    )
    public PaginationResponse<UserResponse> getUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(required = false) String searchTerm
    ) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("insufficient-permissions"));
        }

        List<User> users;
        long count;
        if (StringUtils.hasLength(searchTerm)) {
            users = userService.getByPagination(searchTerm, limit, skip);
            count = userService.getCount(searchTerm);
        } else {
            users = userService.getByPagination(limit, skip);
            count = userService.getCount();
        }


        return new PaginationResponse<>(
                count,
                users.stream().map(UserResponse::new).collect(Collectors.toList())
        );
    }

    @DeleteMapping("/{userId}")
    @ApiOperation(
            value = "Delete a user by Id."
    )
    public ResponseEntity<Void> deleteUser(
            @PathVariable("userId") int userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCurrentUser = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.getId()))
                && userDetails.getId().equals(userId);
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        // Users can only delete their own profile. Admins and Sudo can delete all profiles.
        if (!isCurrentUser && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-user-forbidden"));
        }
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}")
    @ApiOperation(
            value = "Updates the user's information.",
            notes = "Updates the user's information. When the password shall be changed, the user also has to enter his old password for verification."
    )
    public UserResponse updateUser(
            @PathVariable Integer userId,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isCurrentUser = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.getId()))
                && userDetails.getId().equals(userId);
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));
        boolean isMod = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.MODERATOR.getId()));

        // Users can only edit their own profile. Admins and Sudo can edit all profiles.
        if (!isCurrentUser && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-user-forbidden"));
        }

        if (StringUtils.hasLength(request.getNewRole()) && !isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("update-user-role-forbidden"));
        }

        if (request.getNewLocked() != null && !(isAdmin || isMod)) {
            throw new BadRequestException(resourceBundle.getString("lock-account-forbidden"));
        }

        if (request.getNewEmail() != null) {
            // Only admins can update the email address
            if (!isAdmin) {
                throw new ForbiddenException(resourceBundle.getString("update-user-email-only-admin"));
            }
            User user = userService.getById(userId);
            // Only unconfirmed emails can be updated
            if (user.isEmailConfirmed()) {
                throw new ForbiddenException(resourceBundle.getString("update-user-email-only-unconfirmed"));
            }
        }

        request.setId(userId);

        // Admins can set passwords without check
        if (StringUtils.hasLength(request.getNewPassword()) && !isAdmin) {
            if (!StringUtils.hasLength(request.getOldPassword())) {
                throw new BadRequestException(resourceBundle.getString("update-password-requires-password"));
            }
            if (!passwordEncoder.matches(request.getOldPassword(), userService.getById(userId).getPasswordHash())) {
                throw new BadRequestException(resourceBundle.getString("update-password-old-password-invalid"));
            }
        }
        User updatedUser = userService.updateUser(
                request.getNewUsername(),
                request.getNewEmail(),
                request.getNewFirstName(),
                request.getNewLastName(),
                userId,
                request.getNewUniversityId(),
                request.getNewPassword(),
                request.getNewRole(),
                request.getNewLocked()
        );

        return new UserResponse(updatedUser);
    }
}
