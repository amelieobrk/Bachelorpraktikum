package de.kreuzenonline.kreuzen.exam;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.exam.requests.CreateExamRequest;

import de.kreuzenonline.kreuzen.exam.requests.UpdateExamRequest;
import de.kreuzenonline.kreuzen.module.requests.DeleteModuleRequest;

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


import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class ExamControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ExamRepo examRepo;

    @Test
    public void getExamNotPossibleForNonUsers() throws Exception {
        mvc.perform(get("/exam/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getExam() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        when(examRepo.findById(1)).thenReturn(java.util.Optional.of(new Exam(1, "exam", 1, date, true, true)));
        mvc.perform(get("/exam/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("exam"))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.date").value("2020-04-10"))
                .andExpect(jsonPath("$.isComplete").value(true))
                .andExpect(jsonPath("$.isRetry").value(true));

    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createExam() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        String ex = objectMapper.writeValueAsString(new CreateExamRequest("exam", date, true));

        when(examRepo.save(Mockito.any(Exam.class))).thenReturn(new Exam(1, "exam", 1, date, false, true));
        mvc.perform(post("/course/1/exam").accept(MediaType.APPLICATION_JSON).content(ex).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("exam"))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.date").value("2020-04-10"))
                .andExpect(jsonPath("$.isComplete").value(false))
                .andExpect(jsonPath("$.isRetry").value(true));

    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createExamWithUsedDate() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 4);
        String ex = objectMapper.writeValueAsString(new UpdateExamRequest("examNew", 2, date, false, false));
        when(examRepo.existsByCourseIdAndDate(1, date)).thenReturn(true);
        mvc.perform(post("/course/1/exam").accept(MediaType.APPLICATION_JSON).content(ex).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Es darf innerhalb eines Kurses nur eine Klausur pro Datum angelegt werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateExam() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        LocalDate newDate = LocalDate.of(2020, Month.MARCH, 12);
        String ex = objectMapper.writeValueAsString(new UpdateExamRequest("examNew", 2, newDate, false, false));
        when(examRepo.findById(1)).thenReturn(java.util.Optional.of(new Exam(1, "exam", 1, date, true, true)));
        when(examRepo.save(Mockito.any(Exam.class))).thenAnswer(m -> m.getArgument(0));
        mvc.perform(patch("/exam/1").accept(MediaType.APPLICATION_JSON).content(ex).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("examNew"))
                .andExpect(jsonPath("$.courseId").value(2))
                .andExpect(jsonPath("$.date").value("2020-03-12"))
                .andExpect(jsonPath("$.isComplete").value(false))
                .andExpect(jsonPath("$.isRetry").value(false));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateExamNotAdminOrMod() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        LocalDate newDate = LocalDate.of(2020, Month.MARCH, 12);
        String ex = objectMapper.writeValueAsString(new UpdateExamRequest("examNew", 2, newDate, false, false));
        when(examRepo.findById(1)).thenReturn(java.util.Optional.of(new Exam(1, "exam", 1, date, true, true)));
        when(examRepo.save(Mockito.any(Exam.class))).thenAnswer(m -> m.getArgument(0));
        mvc.perform(patch("/exam/1").accept(MediaType.APPLICATION_JSON).content(ex).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void deleteExam() throws Exception {
        String delete = new ObjectMapper().writeValueAsString(new DeleteModuleRequest(null, "p"));
        mvc.perform(delete("/exam/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(delete))
                .andExpect(status().is(204));
        verify(examRepo, times(1)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void deleteExamNotAdminOrMod() throws Exception {
        String delete = new ObjectMapper().writeValueAsString(new DeleteModuleRequest(null, "p"));
        mvc.perform(delete("/exam/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(delete))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN", password = "test1")
    public void deleteExamWrongPassword() throws Exception {
        String delete = new ObjectMapper().writeValueAsString(new DeleteModuleRequest(null, "test2"));
        mvc.perform(delete("/exam/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(delete))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getExamsByUniversityId() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        when(examRepo.findAllByUniversityId(1)).thenReturn(Collections.singletonList(new Exam(1, "exam", 1, date, true, true)));
        mvc.perform(get("/university/1/exam").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("exam"))
                .andExpect(jsonPath("$[*].courseId").value(1))
                .andExpect(jsonPath("$[*].date").value("2020-04-10"))
                .andExpect(jsonPath("$[*].isComplete").value(true))
                .andExpect(jsonPath("$[*].isRetry").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getExamsByUniversityIdAndMajor() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        when(examRepo.findAllByUniversityIdAndMajorId(1, 1)).thenReturn(Collections.singletonList(new Exam(1, "exam", 1, date, true, true)));
        mvc.perform(get("/university/1/exam?major=1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("exam"))
                .andExpect(jsonPath("$[*].courseId").value(1))
                .andExpect(jsonPath("$[*].date").value("2020-04-10"))
                .andExpect(jsonPath("$[*].isComplete").value(true))
                .andExpect(jsonPath("$[*].isRetry").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getExamsByUniversityIdAndSemester() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        when(examRepo.findAllByUniversityIdAndSemesterId(1, 1)).thenReturn(Collections.singletonList(new Exam(1, "exam", 1, date, true, true)));
        mvc.perform(get("/university/1/exam?semester=1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("exam"))
                .andExpect(jsonPath("$[*].courseId").value(1))
                .andExpect(jsonPath("$[*].date").value("2020-04-10"))
                .andExpect(jsonPath("$[*].isComplete").value(true))
                .andExpect(jsonPath("$[*].isRetry").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getExamsByUniversityIdAndMajorAndSemester() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        when(examRepo.findAllByUniversityIdAndMajorIdAndSemesterId(1, 1, 1)).thenReturn(Collections.singletonList(new Exam(1, "exam", 1, date, true, true)));
        mvc.perform(get("/university/1/exam?semester=1&major=1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("exam"))
                .andExpect(jsonPath("$[*].courseId").value(1))
                .andExpect(jsonPath("$[*].date").value("2020-04-10"))
                .andExpect(jsonPath("$[*].isComplete").value(true))
                .andExpect(jsonPath("$[*].isRetry").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getExamsByUniversityIdNotAdminOrMod() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        when(examRepo.findAllByUniversityId(1)).thenReturn(Collections.singletonList(new Exam(1, "exam", 1, date, true, true)));
        mvc.perform(get("/university/1/exam").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getExamsByCourseId() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        when(examRepo.findAllByCourseId(1)).thenReturn(Collections.singletonList(new Exam(1, "exam", 1, date, true, true)));
        mvc.perform(get("/course/1/exam").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("exam"))
                .andExpect(jsonPath("$[*].courseId").value(1))
                .andExpect(jsonPath("$[*].date").value("2020-04-10"))
                .andExpect(jsonPath("$[*].isComplete").value(true))
                .andExpect(jsonPath("$[*].isRetry").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getExamsByModuleId() throws Exception {
        LocalDate date = LocalDate.of(2020, Month.APRIL, 10);
        when(examRepo.findAllByModuleId(1)).thenReturn(Collections.singletonList(new Exam(1, "exam", 1, date, true, true)));
        mvc.perform(get("/module/1/exam").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("exam"))
                .andExpect(jsonPath("$[*].courseId").value(1))
                .andExpect(jsonPath("$[*].date").value("2020-04-10"))
                .andExpect(jsonPath("$[*].isComplete").value(true))
                .andExpect(jsonPath("$[*].isRetry").value(true));
    }


}
