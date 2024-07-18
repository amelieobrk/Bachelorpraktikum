package de.kreuzenonline.kreuzen.semester;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.semester.requests.CreateSemesterRequest;
import de.kreuzenonline.kreuzen.semester.requests.DeleteSemesterRequest;
import de.kreuzenonline.kreuzen.semester.requests.UpdateSemesterRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc

public class SemesterControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private SemesterRepo semesterRepo;

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getAllSemesters() throws Exception {
        when(semesterRepo.findAll()).thenReturn(Collections.singletonList(new Semester(1, "WS 20/21", 2020, 2021)));
        mvc.perform(get("/semester").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("WS 20/21"))
                .andExpect(jsonPath("$[*].startYear").value(2020))
                .andExpect(jsonPath("$[*].endYear").value(2021));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "MOD")
    public void getSemester() throws Exception {
        when(semesterRepo.findById(1)).thenReturn(java.util.Optional.of(new Semester(1, "WS 20/21", 2020, 2021)));
        mvc.perform(get("/semester/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("WS 20/21"))
                .andExpect(jsonPath("$.startYear").value("2020"))
                .andExpect(jsonPath("$.endYear").value("2021"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getSemesterNotFound() throws Exception {
        when(semesterRepo.findById(2)).thenReturn(Optional.empty());

        mvc.perform(get("/semester/2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createSemesterNotPossibleForUser() throws Exception {
        String semester = new ObjectMapper().writeValueAsString(new CreateSemesterRequest("SS 2021", 2021, 2021));
        mvc.perform(post("/semester")
                .accept(MediaType.APPLICATION_JSON)
                .content(semester)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createSemester() throws Exception {
        String semester = new ObjectMapper().writeValueAsString(new CreateSemesterRequest("SS 2021", 2021, 2021));
        when(semesterRepo.save(Mockito.any(Semester.class))).thenReturn(new Semester(1, "SS 2021", 2021, 2021));
        mvc.perform(post("/semester")
                .accept(MediaType.APPLICATION_JSON)
                .content(semester)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("SS 2021"))
                .andExpect(jsonPath("$.startYear").value("2021"))
                .andExpect(jsonPath("$.endYear").value("2021"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createSemesterFailsNameNotSet() throws Exception {
        String semester = new ObjectMapper().writeValueAsString(new CreateSemesterRequest("", 2020, 2021));
        mvc.perform(post("/semester")
                .accept(MediaType.APPLICATION_JSON)
                .content(semester)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createSemesterFailsYearNotInRange() throws Exception {
        String semester = new ObjectMapper().writeValueAsString(new CreateSemesterRequest("WS 20/21", 202, 2021));
        mvc.perform(post("/semester")
                .accept(MediaType.APPLICATION_JSON)
                .content(semester)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createSemesterFailsEndBeforeStart() throws Exception {
        String semester = new ObjectMapper().writeValueAsString(new CreateSemesterRequest("WS 20/21", 2022, 2021));
        mvc.perform(post("/semester")
                .accept(MediaType.APPLICATION_JSON)
                .content(semester)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Das Endjahr darf nicht vor dem Startjahr liegen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createSemesterFailsSemesterTooLong() throws Exception {
        String semester = new ObjectMapper().writeValueAsString(new CreateSemesterRequest("WS 20/21", 2018, 2021));
        mvc.perform(post("/semester")
                .accept(MediaType.APPLICATION_JSON)
                .content(semester)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Der Abstand zwischen Start- und Endjahr darf maximal 1 Jahr betragen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createSemesterFailsNameAlreadyExists() throws Exception {
        when(semesterRepo.existsByNameIgnoreCase("WS 20/21"))
                .thenReturn(true);
        String semester = new ObjectMapper().writeValueAsString(new CreateSemesterRequest("WS 20/21", 2018, 2021));
        mvc.perform(post("/semester")
                .accept(MediaType.APPLICATION_JSON)
                .content(semester)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Es existiert bereits ein Semester mit dieser Bezeichnung."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateSemesterNotAllowed() throws Exception {
        mvc.perform(patch("/semester/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateSemesterNotPossibleWithoutName() throws Exception {
        String semester = new ObjectMapper().writeValueAsString(new CreateSemesterRequest("", 2020, 2021));

        mvc.perform(patch("/semester/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(semester))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateSemesterNotPossibleNameExists() throws Exception {
        when(semesterRepo.existsByNameIgnoreCase("WS202021"))
                .thenReturn(true);
        String semester = new ObjectMapper().writeValueAsString(new CreateSemesterRequest("WD202021", 2020, 2021));

        mvc.perform(patch("/semester/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(semester))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void successfulSemesterUpdate() throws Exception {
        String semester = new ObjectMapper().writeValueAsString(new UpdateSemesterRequest("WS 21/22", 2021, 2022));
        when(semesterRepo.findById(1))
                .thenReturn(java.util.Optional.of(new Semester(1, "WS 21/22", 2021, 2022)));
        when(semesterRepo.save(Mockito.any(Semester.class)))
                .thenAnswer(s -> s.getArgument(0));

        mvc.perform(patch("/semester/1")
                .accept(MediaType.APPLICATION_JSON)
                .content(semester)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("WS 21/22"))
                .andExpect(jsonPath("$.startYear").value(2021))
                .andExpect(jsonPath("$.endYear").value(2022));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void deleteSemesterNotAllowedForUser() throws Exception {
        when(semesterRepo.findById(1))
                .thenReturn(java.util.Optional.of(new Semester(1, "WS 21/22", 2021, 2022)));

        mvc.perform(delete("/semester/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN", password = "123456789")
    public void deleteSemesterNotAllowedWithWrongPassword() throws Exception {
        String delete = new ObjectMapper().writeValueAsString(new DeleteSemesterRequest(null, "987654321"));

        mvc.perform(delete("/semester/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(delete))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void deleteSemesterSuccessful() throws Exception {
        String delete = new ObjectMapper().writeValueAsString(new DeleteSemesterRequest(null, "p"));

        mvc.perform(delete("/semester/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(delete))
                .andExpect(status().is(204));
        verify(semesterRepo, times(1)).deleteById(1);
    }
}
