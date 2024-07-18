package de.kreuzenonline.kreuzen.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.kreuzenonline.kreuzen.auth.CustomUserDetailsService;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.auth.data.CustomUserDetailsRepo;
import de.kreuzenonline.kreuzen.auth.data.EmailConfirmationTokenRepo;
import de.kreuzenonline.kreuzen.auth.data.PasswordResetTokenRepo;
import de.kreuzenonline.kreuzen.role.RoleRepo;
import de.kreuzenonline.kreuzen.email.EmailService;
import de.kreuzenonline.kreuzen.role.Roles;
import de.kreuzenonline.kreuzen.university.UniversityService;
import de.kreuzenonline.kreuzen.user.requests.UpdateUserRequest;
import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class UserControllerTests {

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
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getUserShouldReturnCorrectUserInformation() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "123234345456", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());
        when(userRepo.findById(45)).thenReturn(Optional.of(user));

        mvc.perform(get("/user/{id}", 45)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test"))
                .andExpect(jsonPath("$.email").value("test@uni.de"))
                .andExpect(jsonPath("$.id").value("45"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void userCanNotBeFound() throws Exception {
        when(userRepo.findById(44)).thenReturn(Optional.empty());

        mvc.perform(get("/user/{id}", 44)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void userCanBeDeletedByItself() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());
        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        doNothing().when(userRepo).deleteById(45);

        mvc.perform(delete("/user/{id}", 45)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 46, role = "ADMIN")
    public void userCanBeDeletedByAdmin() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());
        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        doNothing().when(userRepo).deleteById(45);

        mvc.perform(delete("/user/{id}", 45)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(username = "test2", email = "test2@uni.de", id = 47)
    public void userCanNotBeDeletedByAnotherUser() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());
        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        mvc.perform(delete("/user/{id}", 45)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void userCanNotBeDeletedByUnauthorizedPerson() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());
        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        mvc.perform(delete("/user/{id}", 45)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 48, role = "ADMIN")
    public void userCanBeUpdatedByAdmin() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setId(45);
        req.setNewUsername("testPatch");

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.existsByUsernameIgnoreCase("testPatch"))
                .thenReturn(false);
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(u -> u.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testPatch"))
                .andDo(print());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 48, role = "ADMIN")
    public void emailCanBeUpdatedByAdminUnconfirmed() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setId(45);
        req.setNewEmail("test2@uni.de");

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.existsByUsernameIgnoreCase("testPatch"))
                .thenReturn(false);
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(u -> u.getArgument(0));
        doNothing().when(universityService).checkEmailAllowed(1, "test2@uni.de");
        when(customUserDetailsService.resendEmailConfirmationToken(Mockito.any(User.class))).thenReturn(Pair.of("TOKEN", 45));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test2@uni.de"))
                .andDo(print());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 48, role = "ADMIN")
    public void emailCanBeUpdatedByAdminConfirmed() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), true, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setId(45);
        req.setNewEmail("test2@uni.de");

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.existsByUsernameIgnoreCase("testPatch"))
                .thenReturn(false);
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(u -> u.getArgument(0));
        doNothing().when(universityService).checkEmailAllowed(1, "test2@uni.de");
        when(customUserDetailsService.resendEmailConfirmationToken(Mockito.any(User.class))).thenReturn(Pair.of("TOKEN", 45));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "USER")
    public void emailCanNotBeUpdatedByUser() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), true, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setId(45);
        req.setNewEmail("test2@uni.de");

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test2", email = "test2@uni.de", id = 46)
    public void userCanNotBeUpdatedByAnotherUser() throws Exception {
        mvc.perform(patch("/user/{id}", 45)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void userCanNotBeUpdatedBecauseUsernameIsTooShort() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNewUsername("t");

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.existsByUsernameIgnoreCase("t")).thenReturn(false);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void userCanNotBeUpdatedBecauseUsernameIsInUse() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setId(45);
        req.setNewUsername("testtest");

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.existsByUsernameIgnoreCase("testtest")).thenReturn(true);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void userCanNotBeUpdatedBecausePasswordTooShort() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNewUsername("testPatched");
        req.setNewPassword("123456");

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.existsByUsernameIgnoreCase("testPatched")).thenReturn(false);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void userReturnsBecauseThereIsNothingToUpdate() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNewUsername("testPatched");
        req.setNewPassword("123456");

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.existsByUsernameIgnoreCase("testPatched")).thenReturn(false);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void successfulUserUpdate() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setId(45);
        req.setNewUsername("testPatch");

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.existsByUsernameIgnoreCase("testPatch"))
                .thenReturn(false);
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(u -> u.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testPatch"))
                .andDo(print());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    public void setRoleAdminSuccess() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNewRole(Roles.ADMIN.getId());

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(u -> u.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andDo(print());
        verify(userRepo, times(1)).save(Mockito.any());
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(argument.capture());
        Assert.isTrue(argument.getValue().getRole().equals("ADMIN"), "Role should have changed");
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    public void setPasswordWithoutOldPasswordAsAdmin() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNewPassword("new pw top secret");

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(u -> u.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
        verify(userRepo, times(1)).save(Mockito.any());
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(argument.capture());
        Assert.isTrue(!argument.getValue().getPasswordHash().equals("HASH"), "Password Hash should have changed");
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    public void lockAccountAdminSuccess() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNewLocked(true);

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(u -> u.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
        verify(userRepo, times(1)).save(Mockito.any());
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(argument.capture());
        Assert.isTrue(argument.getValue().isLocked(), "Account should be locked");
    }

    @Test
    @WithMockCustomUser(role = "USER")
    public void lockAccountUserForbidden() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNewLocked(true);

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(u -> u.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    @WithMockCustomUser(role = "USER")
    public void setRoleAsUserForbidden() throws Exception {
        User user = new User(45, "test", "first", "last", "test@uni.de", "HASH", Roles.USER.getId(), false, false, 1, Instant.now(), Instant.now());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNewRole("ADMIN");

        when(userRepo.findById(45)).thenReturn(Optional.of(user));
        when(userRepo.save(Mockito.any(User.class))).thenAnswer(u -> u.getArgument(0));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(req);

        mvc.perform(patch("/user/{id}", 45)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    public void loadUsers() throws Exception {

        when(userRepo.findAllPagination(20, 40)).thenReturn(Arrays.asList(
                new User(
                        45,
                        "test",
                        "first",
                        "last",
                        "test@uni.de",
                        "HASH",
                        Roles.USER.getId(),
                        false,
                        false,
                        1,
                        Instant.now(),
                        Instant.now()
                ),
                new User(
                        46,
                        "test1",
                        "first1",
                        "last1",
                        "test@uni.de1",
                        "HASH1",
                        Roles.USER.getId(),
                        false,
                        false,
                        1,
                        Instant.now(),
                        Instant.now()
                ),
                new User(
                        47,
                        "test2",
                        "first2",
                        "last2",
                        "test@uni.de2",
                        "HASH",
                        Roles.USER.getId(),
                        false,
                        false,
                        1,
                        Instant.now(),
                        Instant.now()
                )));

        when(userRepo.count()).thenReturn((long) 1234);

        mvc.perform(get("/user?limit=20&skip=40"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.count").value(1234))
                .andExpect(jsonPath("$.entities.[0].id").value(45))
                .andExpect(jsonPath("$.entities.[0].username").value("test"))
                .andExpect(jsonPath("$.entities.[0].firstName").value("first"))
                .andExpect(jsonPath("$.entities.[0].lastName").value("last"))
                .andExpect(jsonPath("$.entities.[0].email").value("test@uni.de"))
                .andExpect(jsonPath("$.entities.[1].id").value(46))
                .andExpect(jsonPath("$.entities.[1].username").value("test1"))
                .andExpect(jsonPath("$.entities.[1].firstName").value("first1"))
                .andExpect(jsonPath("$.entities.[1].lastName").value("last1"))
                .andExpect(jsonPath("$.entities.[1].email").value("test@uni.de1"))
                .andExpect(jsonPath("$.entities.[2].id").value(47))
                .andExpect(jsonPath("$.entities.[2].username").value("test2"))
                .andExpect(jsonPath("$.entities.[2].firstName").value("first2"))
                .andExpect(jsonPath("$.entities.[2].lastName").value("last2"))
                .andExpect(jsonPath("$.entities.[2].email").value("test@uni.de2"));
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    public void loadUsersSearch() throws Exception {

        when(userRepo.findBySearchTerm("test", 20, 40)).thenReturn(Arrays.asList(
                new User(
                        45,
                        "test",
                        "first",
                        "last",
                        "test@uni.de",
                        "HASH",
                        Roles.USER.getId(),
                        false,
                        false,
                        1,
                        Instant.now(),
                        Instant.now()
                ),
                new User(
                        46,
                        "test1",
                        "first1",
                        "last1",
                        "test@uni.de1",
                        "HASH1",
                        Roles.USER.getId(),
                        false,
                        false,
                        1,
                        Instant.now(),
                        Instant.now()
                ),
                new User(
                        47,
                        "test2",
                        "first2",
                        "last2",
                        "test@uni.de2",
                        "HASH",
                        Roles.USER.getId(),
                        false,
                        false,
                        1,
                        Instant.now(),
                        Instant.now()
                )));

        when(userRepo.countBySearchTerm("test")).thenReturn(1234);

        mvc.perform(get("/user?limit=20&skip=40&searchTerm=test"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.count").value(1234))
                .andExpect(jsonPath("$.entities.[0].id").value(45))
                .andExpect(jsonPath("$.entities.[0].username").value("test"))
                .andExpect(jsonPath("$.entities.[0].firstName").value("first"))
                .andExpect(jsonPath("$.entities.[0].lastName").value("last"))
                .andExpect(jsonPath("$.entities.[0].email").value("test@uni.de"))
                .andExpect(jsonPath("$.entities.[1].id").value(46))
                .andExpect(jsonPath("$.entities.[1].username").value("test1"))
                .andExpect(jsonPath("$.entities.[1].firstName").value("first1"))
                .andExpect(jsonPath("$.entities.[1].lastName").value("last1"))
                .andExpect(jsonPath("$.entities.[1].email").value("test@uni.de1"))
                .andExpect(jsonPath("$.entities.[2].id").value(47))
                .andExpect(jsonPath("$.entities.[2].username").value("test2"))
                .andExpect(jsonPath("$.entities.[2].firstName").value("first2"))
                .andExpect(jsonPath("$.entities.[2].lastName").value("last2"))
                .andExpect(jsonPath("$.entities.[2].email").value("test@uni.de2"));
    }

    @Test
    @WithMockCustomUser(role = "USER")
    public void loadUsersUserForbidden() throws Exception {

        mvc.perform(get("/user?limit=20&skip=40"))
                .andExpect(status().is4xxClientError());
    }
}