package de.kreuzenonline.kreuzen.user;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.auth.CustomUserDetailsService;
import de.kreuzenonline.kreuzen.email.EmailService;
import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import de.kreuzenonline.kreuzen.exceptions.VerificationException;
import de.kreuzenonline.kreuzen.university.University;
import de.kreuzenonline.kreuzen.university.UniversityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    private final UniversityService universityService;
    private final PasswordEncoder passwordEncoder;
    private final ResourceBundle resourceBundle;
    private final CustomUserDetailsService userDetailsService;
    private final EmailService emailService;

    public UserServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder, ResourceBundle resourceBundle, UniversityService universityService, @Lazy CustomUserDetailsService userDetailsService, EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.resourceBundle = resourceBundle;
        this.universityService = universityService;
        this.userDetailsService = userDetailsService;
        this.emailService = emailService;
    }

    @Override
    public User getById(int id) {
        Optional<User> user = userRepo.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("user-not-found"));
        }
        return user.get();
    }

    @Override
    public void deleteById(int id) {
        Optional<User> user = userRepo.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("user-not-found"));
        }
        userRepo.deleteById(id);
    }

    public User updateUser(String newUsername,
                           String newEmail,
                           String newFirstName,
                           String newLastName,
                           int id,
                           Integer newUniversityId,
                           String newPassword,
                           String newRole,
                           Boolean newLocked) {
        User userToBeUpdated = getById(id);
        // The first step is to check which fields shall be updated. If none of them is used, the user will be returned.
        // Checks for collisions and throws an exception when necessary.
        if (newUsername != null) {
            if (!newUsername.equalsIgnoreCase(userToBeUpdated.getUsername()) && userRepo.existsByUsernameIgnoreCase(newUsername)) {
                throw new ConflictException(resourceBundle.getString("username-taken"));
            }
            userToBeUpdated.setUsername(newUsername);
        }

        if (newEmail != null) {
            // check mail by university
            universityService.checkEmailAllowed(userToBeUpdated.getUniversityId(), newEmail);

            // set email
            userToBeUpdated.setEmail(newEmail);
            userToBeUpdated.setEmailConfirmed(false);

            // send new confirmation mail
            Pair<String, Integer> token = userDetailsService.resendEmailConfirmationToken(userToBeUpdated);
            emailService.sendConfirmEmailMessage(
                    newEmail,
                    token.getSecond() + "-" + token.getFirst(),
                    userToBeUpdated.getFirstName()
            );
        }

        // When there can't be a collision and the values are validated by request, the information will be set in the updated User entity.
        if (newUniversityId != null) {
            userToBeUpdated.setUniversityId(newUniversityId);

        }
        if (newPassword != null) {
            userToBeUpdated.setPasswordHash(passwordEncoder.encode(newPassword));
        }

        if (newFirstName != null) {
            userToBeUpdated.setFirstName(newFirstName);
        }

        if (newLastName != null) {
            userToBeUpdated.setLastName(newLastName);
        }

        if (newRole != null) {
            userToBeUpdated.setRole(newRole);
        }

        if (newLocked != null) {
            userToBeUpdated.setLocked(newLocked);
        }

        // After all changes are applied, the updated user will be saved and returned.
        return userRepo.save(userToBeUpdated);
    }

    @Override
    public List<User> getByPagination(int limit, int skip) {
        return userRepo.findAllPagination(limit, skip);
    }

    @Override
    public long getCount() {
        return userRepo.count();
    }

    @Override
    public List<User> getByPagination(String searchTerm, int limit, int skip) {
        return userRepo.findBySearchTerm(searchTerm, limit, skip);
    }

    @Override
    public long getCount(String searchTerm) {
        return userRepo.countBySearchTerm(searchTerm);
    }
}
