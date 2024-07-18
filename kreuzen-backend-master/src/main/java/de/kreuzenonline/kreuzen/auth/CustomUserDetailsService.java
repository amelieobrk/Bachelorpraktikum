package de.kreuzenonline.kreuzen.auth;

import de.kreuzenonline.kreuzen.university.University;
import de.kreuzenonline.kreuzen.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.data.util.Pair;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface CustomUserDetailsService extends UserDetailsService {

    /**
     * Checks whether the username and email are still available. Further it checks whether the provided email
     * is valid for at least one university. It returns a list of all eligible universities.
     *
     * @param username Username of the user
     * @param email    Email of the user
     * @return List of universities that fit for the email address
     */
    List<University> preRegistration(String username, String email);

    /**
     * Creates a new user.
     * Before the user is created, the following checks are performed:
     * - email address allowed by university?
     * - name or email collision?
     *
     * @param username     Username
     * @param email        Email
     * @param password     Password (clear)
     * @param universityId Id of the university
     * @return User Details for created user
     */
    Pair<CustomUserDetails, String> createUser(String username, String firstName, String lastName, String email, String password, int universityId, Integer[] majors, Integer[] majorSections);

    /**
     * Loads user by id.
     *
     * @param id Id of the user
     * @return UserDetails for given id
     * @throws UsernameNotFoundException No user found
     */
    CustomUserDetails loadUserById(Integer id) throws UsernameNotFoundException;

    /**
     * Creates a new password reset token.
     * Previous tokens are invalidated.
     *
     * @param username Username or email of affected user
     * @return Raw password reset token and the id of the user
     */
    Pair<String, Integer> createPasswordResetToken(String username);

    /**
     * Confirm the password reset of a user.
     * The token is checked with the specified id.
     * If it is valid then the new password is set and the token is invalidated.
     *
     * @param id       Id of the user
     * @param token    PW Reset Token
     * @param password New Password
     * @return User Details of the affected user
     */
    CustomUserDetails confirmPasswordReset(Integer id, String token, String password);

    /**
     * Confirm the email address of a user.
     * The token is checked against the token hash for the specified id of the user.
     * If it matches the email address is considered confirmed.
     *
     * @param id    Id of the user
     * @param token Confirmation Token
     */
    void confirmEmail(Integer id, String token);

    /**
     * Confirm the email address of a user. The token is not checked, therefore this should only be called
     * by admins.
     *
     * @param id Id of the user
     */
    void adminConfirmEmail(Integer id);

    /**
     * Generates a new email confirmation token as the old one is hashed.
     * Old confirmation tokens are invalidated and only the latest is valid.
     * <p>
     * Users who already have confirmed their mail don't get a new token.
     *
     * @param username Username or Email of user
     * @return Confirmation token and the id of the user
     */
    Pair<String, Integer> resendEmailConfirmationToken(String username);

    /**
     * Generates a new email confirmation token as the old one is hashed.
     * Old confirmation tokens are invalidated and only the latest is valid.
     * <p>
     * Users who already have confirmed their mail don't get a new token.
     *
     * @param user User to receive a new confirmation token
     * @return Confirmation token and the id of the user
     */
    public Pair<String, Integer> resendEmailConfirmationToken(User user);

    /**
     * Generates a secret token with 20 characters.
     * A secure random generator is used.
     *
     * @return Secret token
     */
    String generateSecretToken();

    /**
     * Generates a valid jwt token.
     *
     * @param userDetails User for whom a token should be generated
     * @return jwt token
     */
    String generateJwt(CustomUserDetails userDetails);

    /**
     * Extract claims from a jwt token. If the token is not valid a RuntimeException is thrown.
     *
     * @param jwt Jwt that should be read
     * @return jwt claims
     */
    Jws<Claims> getJwtClaims(String jwt);
}
