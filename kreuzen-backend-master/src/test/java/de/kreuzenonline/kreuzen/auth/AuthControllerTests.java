package de.kreuzenonline.kreuzen.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.kreuzenonline.kreuzen.auth.data.*;
import de.kreuzenonline.kreuzen.auth.requests.*;
import de.kreuzenonline.kreuzen.auth.responses.UserDetailsResponse;
import de.kreuzenonline.kreuzen.email.EmailService;
import de.kreuzenonline.kreuzen.exceptions.VerificationException;
import de.kreuzenonline.kreuzen.role.RoleRepo;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.university.University;
import de.kreuzenonline.kreuzen.university.UniversityService;
import de.kreuzenonline.kreuzen.user.User;
import de.kreuzenonline.kreuzen.user.UserRepo;
import de.kreuzenonline.kreuzen.user.requests.UpdateUserRequest;
import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class AuthControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepo userRepo;
    @MockBean
    private CustomUserDetailsRepo userDetailsRepo;
    @MockBean
    private UniversityService universityService;
    @MockBean
    private RoleRepo roleRepo;
    @MockBean
    private EmailService emailService;
    @MockBean
    private EmailConfirmationTokenRepo emailConfirmationTokenRepo;
    @MockBean
    private PasswordResetTokenRepo passwordResetTokenRepo;

    @Test
    @WithMockCustomUser(username = "test1", email = "test1@uni.de", id = 45)
    public void meShouldReturnCurrentUser() throws Exception {

        mvc.perform(get("/auth/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.username").value("test1"))
                .andExpect(jsonPath("$.email").value("test1@uni.de"))
                .andExpect(jsonPath("$.id").value("45"));

    }

    @Test
    public void registrationShouldDetectConflictEmail() {
        when(userRepo.existsByEmailIgnoreCase("FooBar@uni-mail.de")).thenReturn(true);

        ResponseEntity<UserDetailsResponse> response = this.restTemplate.postForEntity("http://localhost:" + port + "/auth/register", new RegistrationRequest(
                "Foo",
                "First",
                "Last",
                "FooBar@uni-mail.de",
                "TopSecret",
                1,
                new Integer[0],
                new Integer[0]
        ), UserDetailsResponse.class);

        assertThat(
                response.getStatusCode()
        ).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void registrationNonUniEmail() {

        when(universityService.getById(1)).thenReturn(new University(1, "Foo", new String[0]));
        when(userRepo.existsByEmailIgnoreCase("FooBar@uni-mail.de")).thenReturn(true);
        doThrow(new VerificationException("Nutze deine Uni Mail!")).when(universityService).checkEmailAllowed(Mockito.anyInt(), Mockito.anyString());

        ResponseEntity<UserDetailsResponse> response = this.restTemplate.postForEntity("http://localhost:" + port + "/auth/register", new RegistrationRequest(
                "Foo",
                "First",
                "Last",
                "FooBar@asd.de",
                "TopSecret",
                1,
                new Integer[0],
                new Integer[0]
        ), UserDetailsResponse.class);

        assertThat(
                response.getStatusCode()
        ).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    public void registrationDbError() {

        when(universityService.getById(1)).thenReturn(new University(1, "Uni", new String[]{"uni-mail.de"}));
        when(userRepo.existsByUsernameIgnoreCase("Foo")).thenReturn(false);
        when(userRepo.existsByEmailIgnoreCase("FooBar@uni-mail.de")).thenReturn(false);
        when(userRepo.save(Mockito.any(User.class))).thenThrow(new RuntimeException("Could not save to db."));

        ResponseEntity<UserDetailsResponse> response = this.restTemplate.postForEntity("http://localhost:" + port + "/auth/register", new RegistrationRequest(
                "Foo",
                "First",
                "Last",
                "FooBar@uni-mail.de",
                "TopSecret",
                1,
                new Integer[0],
                new Integer[0]
        ), UserDetailsResponse.class);

        assertThat(
                response.getStatusCode()
        ).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void registrationShouldDetectConflictUsername() {
        when(userRepo.existsByUsernameIgnoreCase("Foo")).thenReturn(true);

        ResponseEntity<UserDetailsResponse> response = this.restTemplate.postForEntity("http://localhost:" + port + "/auth/register", new RegistrationRequest(
                "Foo",
                "First",
                "Last",
                "FooBar@uni-mail.de",
                "TopSecret",
                1,
                new Integer[0],
                new Integer[0]
        ), UserDetailsResponse.class);

        assertThat(
                response.getStatusCode()
        ).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void successfulRegistration() {
        when(universityService.getById(1)).thenReturn(new University(1, "Uni", new String[]{"uni-mail.de"}));
        when(userRepo.existsByUsernameIgnoreCase("Foo")).thenReturn(false);
        when(userRepo.existsByEmailIgnoreCase("FooBar@uni-mail.de")).thenReturn(false);
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2);
            return u;
        });
        when(userDetailsRepo.findByUsername("Foo")).thenReturn(java.util.Optional.of(new CustomUserDetails(
                2,
                "Foo",
                "First",
                "Last",
                "FooBar@uni-mail.de",
                "HASH",
                "USER",
                1,
                false,
                false,
                Instant.now(),
                Instant.now()
        )));
        when(userDetailsRepo.findByEmail("FooBar@uni-mail.de")).thenReturn(java.util.Optional.of(new CustomUserDetails(
                2,
                "Foo",
                "First",
                "Last",
                "FooBar@uni-mail.de",
                "HASH",
                "USER",
                1,
                false,
                false,
                Instant.now(),
                Instant.now()
        )));

        ResponseEntity<UserDetailsResponse> response = this.restTemplate.postForEntity("http://localhost:" + port + "/auth/register", new RegistrationRequest(
                "Foo",
                "First",
                "Last",
                "FooBar@uni-mail.de",
                "TopSecret",
                1,
                new Integer[0],
                new Integer[0]
        ), UserDetailsResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualToIgnoringCase("Foo");
        assertThat(response.getBody().getFirstName()).isEqualToIgnoringCase("First");
        assertThat(response.getBody().getLastName()).isEqualToIgnoringCase("Last");
        assertThat(response.getBody().getEmail()).isEqualToIgnoringCase("FooBar@uni-mail.de");
        assertThat(response.getBody().getRole()).isEqualTo("USER");
        assertThat(response.getBody().getId()).isEqualTo(2);
    }

    @Test
    void loginValidUsername() throws Exception {
        when(userDetailsRepo.findByUsername("test")).thenReturn(Optional.of(
                new CustomUserDetails(
                        2,
                        "test",
                        "first",
                        "last",
                        "email@uni.de",
                        PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                        Roles.USER.getId(),
                        1,
                        true,
                        false,
                        Instant.now(),
                        Instant.now()
                )));
        when(userDetailsRepo.findByEmail("test")).thenReturn(Optional.empty());

        mvc.perform(post("/auth/login").content(new ObjectMapper().writeValueAsString(new LoginRequest(
                "test",
                "TopSecretPW"
        ))).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(header().exists("Authorization"));
    }

    @Test
    void loginValidEmail() throws Exception {
        when(userDetailsRepo.findByEmail("email@uni.de")).thenReturn(Optional.of(
                new CustomUserDetails(
                        2,
                        "test",
                        "first",
                        "last",
                        "email@uni.de",
                        PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                        Roles.USER.getId(),
                        1,
                        true,
                        false,
                        Instant.now(),
                        Instant.now()
                )));
        when(userDetailsRepo.findByUsername("email@uni.de")).thenReturn(Optional.empty());

        mvc.perform(post("/auth/login").content(new ObjectMapper().writeValueAsString(new LoginRequest(
                "email@uni.de",
                "TopSecretPW"
        ))).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(header().exists("Authorization"));
    }

    @Test
    void loginInvalidCredentials() throws Exception {
        when(userDetailsRepo.findByEmail("email@uni.de")).thenReturn(Optional.of(
                new CustomUserDetails(
                        2,
                        "test",
                        "first",
                        "last",
                        "email@uni.de",
                        PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                        Roles.USER.getId(),
                        1,
                        true,
                        false,
                        Instant.now(),
                        Instant.now()
                )));
        when(userDetailsRepo.findByUsername("email@uni.de")).thenReturn(Optional.empty());

        mvc.perform(post("/auth/login").content(new ObjectMapper().writeValueAsString(new LoginRequest(
                "email@uni.de",
                "TopSecretPWA"
        ))).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(401));
    }

    @Test
    void loginValidAndMe() throws Exception {
        when(userDetailsRepo.findByUsername("test")).thenReturn(Optional.of(
                new CustomUserDetails(
                        2,
                        "test",
                        "first",
                        "last",
                        "email@uni.de",
                        PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                        Roles.USER.getId(),
                        1,
                        true,
                        false,
                        Instant.now(),
                        Instant.now()
                )));
        when(userDetailsRepo.findById(2)).thenReturn(Optional.of(
                new CustomUserDetails(
                        2,
                        "test",
                        "first",
                        "last",
                        "email@uni.de",
                        PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                        Roles.USER.getId(),
                        1,
                        true,
                        false,
                        Instant.now(),
                        Instant.now()
                )));
        when(userDetailsRepo.findByEmail("test")).thenReturn(Optional.empty());

        String token = mvc.perform(post("/auth/login").content(new ObjectMapper().writeValueAsString(new LoginRequest(
                "test",
                "TopSecretPW"
        ))).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(header().exists("Authorization"))
                .andReturn().getResponse().getHeader("Authorization");

        mvc.perform(get("/auth/me").accept(MediaType.APPLICATION_JSON).header("Authorization", token))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.username").value("test"))
                .andExpect(jsonPath("$.email").value("email@uni.de"))
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void loginNonExistentAccount() throws Exception {
        when(userDetailsRepo.findByEmail("email@uni.de")).thenReturn(Optional.empty());
        when(userDetailsRepo.findByUsername("email@uni.de")).thenReturn(Optional.empty());

        mvc.perform(post("/auth/login").content(new ObjectMapper().writeValueAsString(new LoginRequest(
                "email@uni.de",
                "TopSecretPWA"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(401));
    }

    @Test
    void pwResetRequestValid() throws Exception {
        when(userDetailsRepo.findByEmail("email@uni.de")).thenReturn(Optional.of(
                new CustomUserDetails(
                        2,
                        "test",
                        "first",
                        "last",
                        "email@uni.de",
                        PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                        Roles.USER.getId(),
                        1,
                        true,
                        false,
                        Instant.now(),
                        Instant.now()
                )));

        mvc.perform(post("/auth/request-pw-reset").content(new ObjectMapper().writeValueAsString(new PasswordResetRequest(
                "email@uni.de"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is(204));
    }

    @Test
    void pwResetRequestNonExistent() throws Exception {
        when(userDetailsRepo.findByEmail("email@uni.de")).thenReturn(Optional.empty());

        mvc.perform(post("/auth/request-pw-reset").content(new ObjectMapper().writeValueAsString(new PasswordResetRequest(
                "email@uni.de"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is(404));
    }

    @Test
    void pwResetConfirmationValid() throws Exception {
        when(passwordResetTokenRepo.findById(1)).thenReturn(Optional.of(new PasswordResetToken(
                1,
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("Top-Secret-Token"),
                Instant.now().plus(1, ChronoUnit.HOURS)
        )));
        when(userDetailsRepo.findById(1)).thenReturn(Optional.of(new CustomUserDetails(
                1,
                "test",
                "first",
                "last",
                "email@uni.de",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                Roles.USER.getId(),
                1,
                true,
                false,
                Instant.now(),
                Instant.now()
        )));
        when(userRepo.findById(1)).thenReturn(Optional.of(new User(
                1,
                "test",
                "first",
                "last",
                "email@uni.de",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                Roles.USER.getId(),
                false,
                false,
                2,
                Instant.now(),
                Instant.now()
        )));

        mvc.perform(post("/auth/confirm-pw-reset").content(new ObjectMapper().writeValueAsString(new ConfirmPasswordResetRequest(
                "1-Top-Secret-Token",
                "TopSecretPassword"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().exists("Authorization"))
                .andExpect(status().is(200));
    }

    @Test
    void pwResetConfirmationExpired() throws Exception {
        when(passwordResetTokenRepo.findById(1)).thenReturn(Optional.of(new PasswordResetToken(
                1,
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretToken"),
                Instant.now().minus(1, ChronoUnit.HOURS)
        )));
        when(userDetailsRepo.findById(1)).thenReturn(Optional.of(new CustomUserDetails(
                1,
                "test",
                "first",
                "last",
                "email@uni.de",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                Roles.USER.getId(),
                1,
                true,
                false,
                Instant.now(),
                Instant.now()
        )));
        when(userRepo.findById(1)).thenReturn(Optional.of(new User(
                1,
                "test",
                "first",
                "last",
                "email@uni.de",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                Roles.USER.getId(),
                false,
                false,
                2,
                Instant.now(),
                Instant.now()
        )));

        mvc.perform(post("/auth/confirm-pw-reset").content(new ObjectMapper().writeValueAsString(new ConfirmPasswordResetRequest(
                "1.TopSecretToken",
                "TopSecretPassword"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void pwResetConfirmationInvalidCode() throws Exception {
        when(passwordResetTokenRepo.findById(1)).thenReturn(Optional.of(new PasswordResetToken(
                1,
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretTokenn"),
                Instant.now().plus(1, ChronoUnit.HOURS)
        )));

        mvc.perform(post("/auth/confirm-pw-reset").content(new ObjectMapper().writeValueAsString(new ConfirmPasswordResetRequest(
                "1.TopSecretToken",
                "TopSecretPassword"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void pwResetConfirmationInvalidId() throws Exception {
        when(passwordResetTokenRepo.findById(1)).thenReturn(Optional.empty());

        mvc.perform(post("/auth/confirm-pw-reset").content(new ObjectMapper().writeValueAsString(new ConfirmPasswordResetRequest(
                "1.TopSecretToken",
                "TopSecretPassword"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void confirmEmailValid() throws Exception {
        when(emailConfirmationTokenRepo.findById(2)).thenReturn(Optional.of(new EmailConfirmationToken(
                2,
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("Top-Secret-Token")
        )));
        when(userDetailsRepo.findById(2)).thenReturn(Optional.of(new CustomUserDetails(
                2,
                "test",
                "first",
                "last",
                "email@uni.de",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                Roles.USER.getId(),
                1,
                true,
                false,
                Instant.now(),
                Instant.now()
        )));
        when(userRepo.findById(2)).thenReturn(Optional.of(new User(
                2,
                "test",
                "first",
                "last",
                "email@uni.de",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                Roles.USER.getId(),
                false,
                false,
                2,
                Instant.now(),
                Instant.now()
        )));

        mvc.perform(post("/auth/confirm-email").content(new ObjectMapper().writeValueAsString(new ConfirmEmailRequest(
                "2-Top-Secret-Token"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().exists("Authorization"))
                .andExpect(status().is(200));
    }

    @Test
    void confirmEmailInvalidCode() throws Exception {
        when(emailConfirmationTokenRepo.findById(2)).thenReturn(Optional.of(new EmailConfirmationToken(
                2,
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretTokenn")
        )));

        mvc.perform(post("/auth/confirm-email").content(new ObjectMapper().writeValueAsString(new ConfirmEmailRequest(
                "2.TopSecretToken"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void confirmEmailInvalidId() throws Exception {
        when(emailConfirmationTokenRepo.findById(2)).thenReturn(Optional.empty());

        mvc.perform(post("/auth/confirm-email").content(new ObjectMapper().writeValueAsString(new ConfirmEmailRequest(
                "2.TopSecretToken"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void confirmEmailAlreadyConfirmed() throws Exception {
        when(emailConfirmationTokenRepo.findById(2)).thenReturn(Optional.empty());
        when(userRepo.findById(2)).thenReturn(Optional.of(new User(
                2,
                "test",
                "first",
                "last",
                "email@uni.de",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                Roles.USER.getId(),
                true,
                false,
                2,
                Instant.now(),
                Instant.now()
        )));

        mvc.perform(post("/auth/confirm-email").content(new ObjectMapper().writeValueAsString(new ConfirmEmailRequest(
                "2.TopSecretToken"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void resendConfirmationMailValid() throws Exception {
        when(userDetailsRepo.findByEmail("email@uni.de")).thenReturn(Optional.of(new CustomUserDetails(
                2,
                "test",
                "first",
                "last",
                "email@uni.de",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                Roles.USER.getId(),
                1,
                false,
                false,
                Instant.now(),
                Instant.now()
        )));

        mvc.perform(post("/auth/resend-confirmation-email").content(new ObjectMapper().writeValueAsString(new ResendEmailConfirmationRequest(
                "email@uni.de"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200));
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void resendConfirmationMailAlreadyConfirmed() throws Exception {
        when(userDetailsRepo.findByEmail("email@uni.de")).thenReturn(Optional.of(new CustomUserDetails(
                2,
                "test",
                "first",
                "last",
                "email@uni.de",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                Roles.USER.getId(),
                1,
                true,
                false,
                Instant.now(),
                Instant.now()
        )));

        mvc.perform(post("/auth/resend-confirmation-email").content(new ObjectMapper().writeValueAsString(new ResendEmailConfirmationRequest(
                "email@uni.de"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void resendConfirmationMailNotFound() throws Exception {
        when(userDetailsRepo.findByEmail("email@uni.de")).thenReturn(Optional.empty());

        mvc.perform(post("/auth/resend-confirmation-email").content(new ObjectMapper().writeValueAsString(new ResendEmailConfirmationRequest(
                "email@uni.de"
        ))).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void preRegistrationConflictUsername() throws Exception {
        when(userRepo.existsByEmailIgnoreCase("email@uni.de")).thenReturn(false);
        when(userRepo.existsByUsernameIgnoreCase("test")).thenReturn(true);

        mvc.perform(get("/auth/pre-register?username=test&email=email@uni.de"))
                .andExpect(status().is(409));
    }

    @Test
    void preRegistrationConflictEmail() throws Exception {
        when(userRepo.existsByEmailIgnoreCase("email@uni.de")).thenReturn(true);
        when(userRepo.existsByUsernameIgnoreCase("test")).thenReturn(false);

        mvc.perform(get("/auth/pre-register?username=test&email=email@uni.de"))
                .andExpect(status().is(409));
    }

    @Test
    void preRegistrationNonUniEmail() throws Exception {

        when(userRepo.existsByEmailIgnoreCase("email@uni.de")).thenReturn(false);
        when(userRepo.existsByUsernameIgnoreCase("test")).thenReturn(false);
        when(universityService.getByDomain("uni.de")).thenReturn(new ArrayList<>());

        mvc.perform(get("/auth/pre-register?username=test&email=email@uni.de"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.universities").isArray())
                .andExpect(jsonPath("$.universities").isEmpty());
    }

    @Test
    void preRegistrationValid() throws Exception {
        when(userRepo.existsByEmailIgnoreCase("email@uni.de")).thenReturn(false);
        when(userRepo.existsByUsernameIgnoreCase("test")).thenReturn(false);
        when(universityService.getByDomain("uni.de")).thenReturn(Collections.singletonList(new University(1, "TuDa", new String[]{"uni.de"})));

        mvc.perform(get("/auth/pre-register?username=test&email=email@uni.de"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.universities").isArray())
                .andExpect(jsonPath("$.universities[0].id").value(1))
                .andExpect(jsonPath("$.universities[0].name").value("TuDa"))
                .andExpect(jsonPath("$.universities[0].allowedMailDomains[0]").value("uni.de"));
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    public void activateAccountAdminSuccess() throws Exception {
        User user = new User(
                2,
                "test",
                "first",
                "last",
                "email@uni.de",
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("TopSecretPW"),
                Roles.USER.getId(),
                false,
                false,
                2,
                Instant.now(),
                Instant.now()
        );
        when(userRepo.findById(2)).thenReturn(Optional.of(user));
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(u -> u.getArgument(0));

        mvc.perform(
                post("/auth/confirm-email-admin")
                        .content(new ObjectMapper().writeValueAsString(new AdminConfirmEmailRequest(
                                2
                        )))
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
        verify(userRepo, times(1)).save(Mockito.any());
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(argument.capture());
        Assert.isTrue(argument.getValue().isEmailConfirmed(), "Email should be confirmed");
    }

    @Test
    @WithMockCustomUser(role = "USER")
    public void activateAccountUserForbidden() throws Exception {

        mvc.perform(
                post("/auth/confirm-email-admin")
                        .content(new ObjectMapper().writeValueAsString(new AdminConfirmEmailRequest(
                                2
                        )))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
