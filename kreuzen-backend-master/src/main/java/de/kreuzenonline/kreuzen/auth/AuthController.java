package de.kreuzenonline.kreuzen.auth;

import de.kreuzenonline.kreuzen.auth.requests.*;
import de.kreuzenonline.kreuzen.auth.responses.PreRegistrationResponse;
import de.kreuzenonline.kreuzen.auth.responses.UserDetailsResponse;
import de.kreuzenonline.kreuzen.email.EmailService;
import de.kreuzenonline.kreuzen.exceptions.BadRequestException;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.university.University;
import de.kreuzenonline.kreuzen.university.responses.UniversityResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@RestController
@RequestMapping("/auth")
@Api(tags = "Auth")
public class AuthController {

    private final CustomUserDetailsService userDetailsService;
    private final EmailService emailService;
    private final ResourceBundle resourceBundle;

    public AuthController(CustomUserDetailsService userDetailsService, EmailService emailService, ResourceBundle resourceBundle) {
        this.userDetailsService = userDetailsService;
        this.emailService = emailService;
        this.resourceBundle = resourceBundle;
    }

    @GetMapping("/pre-register")
    @ApiOperation(
            value = "Pre Registration Checks",
            notes = "Checks whether the username and email are used and returns a list of fitting universities."
    )
    public PreRegistrationResponse preRegistration(@RequestParam String username,
                                                   @RequestParam String email) {

        List<University> universities = userDetailsService.preRegistration(username, email);
        List<UniversityResponse> universityResponses = new ArrayList<>();

        for (University uni : universities) {
            universityResponses.add(new UniversityResponse(uni));
        }

        return new PreRegistrationResponse(
                universityResponses
        );
    }

    @PostMapping("/register")
    @ApiOperation(
            value = "Requests user",
            notes = "Username and email must be unique."
    )
    public UserDetailsResponse register(@Valid @RequestBody RegistrationRequest request) {

        Pair<CustomUserDetails, String> details = userDetailsService.createUser(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getUniversityId(),
                request.getMajors(),
                request.getMajorSections()
        );

        emailService.sendConfirmEmailMessage(
                details.getFirst().getEmail(),
                details.getFirst().getId() + "-" + details.getSecond(),
                request.getFirstName()
        );

        return new UserDetailsResponse(details.getFirst());
    }

    @PostMapping("/request-pw-reset")
    @ApiOperation(
            value = "Requests password request",
            notes = "Requests a password request mail. Only one password request per user is valid at a time."
    )
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {

        Pair<String, Integer> token = userDetailsService.createPasswordResetToken(passwordResetRequest.getEmail());
        CustomUserDetails user = (CustomUserDetails) userDetailsService.loadUserByUsername(passwordResetRequest.getEmail());

        emailService.sendPasswordResetMessage(
                user.getEmail(),
                token.getSecond() + "-" + token.getFirst(),
                user.getFirstName()
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Splits the given token into an id and the actual token. This short form is used so that the user can enter
     * the token more easily.
     *
     * @param t Combined token
     * @return Id and actual token
     */
    private Pair<Integer, String> splitToken(String t) {
        String[] parts = t.split("-");
        if (parts.length < 2) {
            throw new BadRequestException(resourceBundle.getString("invalid-token"));
        }

        int id;
        StringBuilder token = new StringBuilder(parts[1]);
        for (int i = 2; i < parts.length; i++) {
            token.append("-");
            token.append(parts[i]);
        }
        try {
            id = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new BadRequestException(resourceBundle.getString("invalid-token"));
        }

        return Pair.of(id, token.toString());
    }

    @PostMapping("/confirm-pw-reset")
    @ApiOperation(
            value = "Confirm password reset",
            notes = "Confirmation of the password reset. If the token is valid, the new password is set."
    )
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequest confirmPasswordResetRequest) {

        try {
            Pair<Integer, String> tokenSplit = splitToken(confirmPasswordResetRequest.getToken());

            CustomUserDetails user = userDetailsService.confirmPasswordReset(
                    tokenSplit.getFirst(),
                    tokenSplit.getSecond(),
                    confirmPasswordResetRequest.getNewPassword()
            );

            String jwt = userDetailsService.generateJwt(user);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + jwt);
            return ResponseEntity.ok().headers(headers).build();
        } catch (Exception e) {
            throw new BadRequestException(resourceBundle.getString("invalid-token"));
        }
    }

    @PostMapping("/confirm-email")
    @ApiOperation(
            value = "Confirm email address",
            notes = "Confirm the email address of a user by supplying a secret token."
    )
    public ResponseEntity<Void> confirmEmail(@Valid @RequestBody ConfirmEmailRequest confirmEmailRequest) {

        try {

            Pair<Integer, String> tokenSplit = splitToken(confirmEmailRequest.getToken());

            userDetailsService.confirmEmail(
                    tokenSplit.getFirst(),
                    tokenSplit.getSecond()
            );

            String jwt = userDetailsService.generateJwt(userDetailsService.loadUserById(tokenSplit.getFirst()));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + jwt);

            return ResponseEntity.ok().headers(headers).build();
        } catch (Exception e) {
            throw new BadRequestException(resourceBundle.getString("invalid-token"));
        }
    }

    @PostMapping("/confirm-email-admin")
    @ApiOperation(
            value = "Confirm email address",
            notes = "Confirm the email address of a user by using admin powers."
    )
    public ResponseEntity<Void> confirmEmailAdmin(
            @Valid @RequestBody AdminConfirmEmailRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        if (userDetails == null) {
            throw new ForbiddenException(resourceBundle.getString("unauthorized"));
        }

        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.getId()))
                || userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUDO.getId()));

        if (!isAdmin) {
            throw new ForbiddenException(resourceBundle.getString("AuthController-confirmEmailAdmin-forbidden"));
        }

        userDetailsService.adminConfirmEmail(request.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-confirmation-email")
    @ApiOperation(
            value = "Resend confirmation mail",
            notes = "Resend a confirmation mail. This generates a new confirmation code and invalidates previous requests."
    )
    public ResponseEntity<Void> resendConfirmationEmail(@Valid @RequestBody ResendEmailConfirmationRequest request) {

        Pair<String, Integer> token = userDetailsService.resendEmailConfirmationToken(request.getEmail());
        CustomUserDetails user = (CustomUserDetails) userDetailsService.loadUserByUsername(request.getEmail());

        emailService.sendConfirmEmailMessage(
                user.getEmail(),
                token.getSecond() + "-" + token.getFirst(),
                user.getFirstName()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @ApiOperation(
            value = "Get current user details",
            notes = "This endpoint can be used to infer who is logged in only by knowing the auth token."
    )
    public UserDetailsResponse getAuthenticatedUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return new UserDetailsResponse(userDetails);
    }
}
