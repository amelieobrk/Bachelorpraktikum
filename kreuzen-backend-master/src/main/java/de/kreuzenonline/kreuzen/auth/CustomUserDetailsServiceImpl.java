package de.kreuzenonline.kreuzen.auth;

import de.kreuzenonline.kreuzen.auth.data.*;
import de.kreuzenonline.kreuzen.exceptions.*;
import de.kreuzenonline.kreuzen.major.MajorService;
import de.kreuzenonline.kreuzen.role.RoleRepo;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.section.SectionService;
import de.kreuzenonline.kreuzen.university.University;
import de.kreuzenonline.kreuzen.university.UniversityService;
import de.kreuzenonline.kreuzen.user.User;
import de.kreuzenonline.kreuzen.user.UserRepo;
import de.kreuzenonline.kreuzen.user.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

@Service
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final CustomUserDetailsRepo userDetailsRepo;
    private final UserService userService;
    private final EmailConfirmationTokenRepo emailConfirmationTokenRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final UniversityService universityService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final SecretKey jwtKey;
    private final MajorService majorService;
    private final SectionService sectionService;
    private final ResourceBundle resourceBundle;
    private final RandomString tokenGenerator;

    public CustomUserDetailsServiceImpl(CustomUserDetailsRepo userDetailsRepo, UniversityService universityService, PasswordEncoder passwordEncoder, UserRepo userRepo, RoleRepo roleRepo, EmailConfirmationTokenRepo emailConfirmationTokenRepo, PasswordResetTokenRepo passwordResetTokenRepo, UserService userService, @Value("${jwt.key}") String jwtKey, MajorService majorService, SectionService sectionService, ResourceBundle resourceBundle) {
        this.userDetailsRepo = userDetailsRepo;
        this.universityService = universityService;
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.emailConfirmationTokenRepo = emailConfirmationTokenRepo;
        this.passwordResetTokenRepo = passwordResetTokenRepo;
        this.userService = userService;
        if (!StringUtils.hasLength(jwtKey)) {
            throw new RuntimeException("No JWT key is set. Please set \"jwt.key\" in the application.properties");
        }
        if (jwtKey.getBytes().length < (512 / 8)) {
            throw new RuntimeException("The JWT key is too small. Please use at least 512 Bit");
        }
        this.jwtKey = Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));
        this.majorService = majorService;
        this.sectionService = sectionService;
        this.resourceBundle = resourceBundle;
        this.tokenGenerator = new RandomString(12);
    }

    /**
     * Loads user by name. The name may be the username or the email of the user.
     *
     * @param ident Name or EmailAddress
     * @return UserDetails for given name
     * @throws UsernameNotFoundException No user found
     */
    @Override
    public CustomUserDetails loadUserByUsername(String ident) throws UsernameNotFoundException {

        return userDetailsRepo.findByUsername(ident)
                .or(() -> userDetailsRepo.findByEmail(ident))
                .orElseThrow(() -> new UsernameNotFoundException(resourceBundle.getString("user-not-found")));
    }

    /**
     * @inheritDoc
     */
    @Override
    public CustomUserDetails loadUserById(Integer id) throws UsernameNotFoundException {

        return userDetailsRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(resourceBundle.getString("user-not-found")));
    }

    @Override
    public List<University> preRegistration(String username, String email) {

        // Check for collisions
        if (userRepo.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException(resourceBundle.getString("username-taken"));
        }
        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new ConflictException(resourceBundle.getString("email-taken"));
        }

        String[] emailSplit = email.split("@");
        String domain = emailSplit[emailSplit.length - 1];

        return universityService.getByDomain(domain);
    }

    /**
     * @inheritDoc
     */
    @Transactional
    @Override
    public Pair<CustomUserDetails, String> createUser(String username, String firstName, String lastName, String email, String password, int universityId, Integer[] majors, Integer[] majorSections) {

        // Check for collisions
        if (userRepo.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException(resourceBundle.getString("username-taken"));
        }
        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new ConflictException(resourceBundle.getString("email-taken"));
        }

        universityService.checkEmailAllowed(universityId, email);

        // Create user
        User user = new User();
        String confirmationToken = generateSecretToken();

        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailConfirmed(false);
        user.setUniversityId(universityId);
        user.setRole(Roles.USER.getId());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        try {
            user = userRepo.save(user);
            emailConfirmationTokenRepo.upsert(
                    user.getId(),
                    passwordEncoder.encode(confirmationToken)
            );

            if (majors != null) {
                for (Integer major : majors) {
                    majorService.addMajorToUser(user.getId(), major);
                }
                if (majorSections != null) {
                    for (Integer section : majorSections) {
                        sectionService.addSectionToUser(user.getId(), section);
                    }
                }
            }
        } catch (RuntimeException e) {
            // Delete User if created
            e.printStackTrace();
            throw new InternalServerException("Could not create user.");
        }

        return Pair.of(this.loadUserByUsername(user.getUsername()), confirmationToken);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Pair<String, Integer> createPasswordResetToken(String username) {

        try {
            CustomUserDetails user = this.loadUserByUsername(username);

            String token = generateSecretToken();
            passwordResetTokenRepo.upsert(
                    user.getId(),
                    passwordEncoder.encode(token),
                    Instant.now().plus(24, ChronoUnit.HOURS)
            );

            return Pair.of(token, user.getId());
        } catch (UsernameNotFoundException e) {
            throw new NotFoundException(resourceBundle.getString("user-not-found"));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    @Transactional
    public CustomUserDetails confirmPasswordReset(Integer id, String token, String password) {

        PasswordResetToken resetToken = passwordResetTokenRepo
                .findById(id)
                .orElseThrow(() -> new BadRequestException(resourceBundle.getString("invalid-token")));

        if (resetToken.getExpiresAt().isAfter(Instant.now())) {
            if (passwordEncoder.matches(token, resetToken.getTokenHash())) {
                // Token is valid
                User user = userService.getById(id);
                user.setPasswordHash(passwordEncoder.encode(password));
                userRepo.save(user);
                passwordResetTokenRepo.deleteById(id);
            } else {
                throw new BadRequestException(resourceBundle.getString("invalid-token"));
            }
        } else {
            throw new BadRequestException(resourceBundle.getString("invalid-token"));
        }

        return loadUserById(id);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void confirmEmail(Integer id, String token) {

        User user = userService.getById(id);
        if (user.isEmailConfirmed()) {
            throw new BadRequestException(resourceBundle.getString("invalid-token"));
        }

        EmailConfirmationToken confirmationToken = emailConfirmationTokenRepo.findById(id)
                .orElseThrow(() -> new BadRequestException(resourceBundle.getString("invalid-token")));
        if (passwordEncoder.matches(token, confirmationToken.getTokenHash())) {
            // Confirm mail
            user.setEmailConfirmed(true);
            userRepo.save(user);
        } else {
            throw new BadRequestException(resourceBundle.getString("invalid-token"));
        }
    }

    @Override
    public void adminConfirmEmail(Integer id) {
        User user = userService.getById(id);
        if (user.isEmailConfirmed()) {
            throw new BadRequestException(resourceBundle.getString("invalid-token"));
        }
        user.setEmailConfirmed(true);
        userRepo.save(user);
    }

    @Override
    public Pair<String, Integer> resendEmailConfirmationToken(String username) {

        CustomUserDetails userDetails = this.loadUserByUsername(username);

        return resendEmailConfirmationToken(
                new User(
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getFirstName(),
                        userDetails.getLastName(),
                        userDetails.getEmail(),
                        userDetails.getPasswordHash(),
                        userDetails.getRole(),
                        userDetails.isEmailConfirmed(),
                        userDetails.isLocked(),
                        userDetails.getUniversityId(),
                        userDetails.getCreatedAt(),
                        userDetails.getUpdatedAt()
                )
        );
    }

    @Override
    public Pair<String, Integer> resendEmailConfirmationToken(User user) {

        try {

            if (user.isEmailConfirmed()) {
                throw new BadRequestException("Your email address is already confirmed.");
            }

            String token = generateSecretToken();
            emailConfirmationTokenRepo.upsert(
                    user.getId(),
                    passwordEncoder.encode(token)
            );

            return Pair.of(token, user.getId());
        } catch (UsernameNotFoundException e) {
            throw new NotFoundException(resourceBundle.getString("user-not-found"));
        }
    }

    /**
     * Generates a secret token with 20 characters.
     * A secure random generator is used.
     *
     * @return Secret token
     */
    @Override
    public String generateSecretToken() {
        String token = this.tokenGenerator.nextString();
        String[] segments = token.split("(?<=\\G.{4})");
        return String.join("-", segments);
    }

    @Override
    public String generateJwt(CustomUserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getId().toString())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(jwtKey)
                .compact();
    }

    @Override
    public Jws<Claims> getJwtClaims(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtKey)
                .build()
                .parseClaimsJws(jwt.replace("Bearer", ""));
    }
}
