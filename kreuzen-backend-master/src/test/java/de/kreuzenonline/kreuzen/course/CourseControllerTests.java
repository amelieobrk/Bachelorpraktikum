package de.kreuzenonline.kreuzen.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.course.requests.CreateCourseRequest;
import de.kreuzenonline.kreuzen.course.requests.UpdateCourseRequest;
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

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc

public class CourseControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private CourseRepo courseRepo;

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getCourse() throws Exception {
        when(courseRepo.findById(1)).thenReturn(java.util.Optional.of(new Course(1, 2, 3, "")));

        mvc.perform(get("/course/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.semesterId").value(2))
                .andExpect(jsonPath("$.moduleId").value(3));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getCourseNotFound() throws Exception {
        when(courseRepo.findById(24)).thenReturn(java.util.Optional.empty());

        mvc.perform(get("/course/24")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createCourseNotPossibleForUser() throws Exception {
        String course = new ObjectMapper().writeValueAsString(new CreateCourseRequest(2));
        mvc.perform(post("/module/1/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(course)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "MOD")
    public void createCourseByModerator() throws Exception {
        String course = new ObjectMapper().writeValueAsString(new CreateCourseRequest(2));
        when(courseRepo.save(Mockito.any(Course.class))).thenReturn(new Course(1, 2, 3, ""));
        mvc.perform(post("/module/1/course")
                .accept(MediaType.APPLICATION_JSON)
                .content(course)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.semesterId").value(2))
                .andExpect(jsonPath("$.moduleId").value(3));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de")
    public void getCoursesByModule() throws Exception {

        when(courseRepo.findAllByModuleId(1)).thenReturn(Collections.singletonList(new Course(1, 2, 3, "")));
        mvc.perform(get("/module/1/course").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[*].id").value(1))
                .andExpect(jsonPath("$.[*].moduleId").value(3))
                .andExpect(jsonPath("$.[*].semesterId").value(2));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateCourseNotPossibleForUser() throws Exception {
        mvc.perform(patch("/course/2"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void successfulUpdate() throws Exception {
        String course = new ObjectMapper().writeValueAsString(new UpdateCourseRequest(2, 3));
        when(courseRepo.findById(1)).thenReturn(java.util.Optional.of(new Course(1, 1, 1, "")));
        when(courseRepo.save(Mockito.any(Course.class))).thenAnswer(c -> c.getArgument(0));
        mvc.perform(patch("/course/1").accept(MediaType.APPLICATION_JSON).content(course).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.moduleId").value(2))
                .andExpect(jsonPath("$.semesterId").value(3));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "MOD")
    public void deleteCourseNotPossibleForMod() throws Exception {
        mvc.perform(delete("/course/123"))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void deleteCoursePossibleForAdmin() throws Exception {
        doNothing().when(courseRepo).deleteById(123);
        mvc.perform(delete("/course/123"))
                .andExpect(status().is(204));
        verify(courseRepo, times(1)).deleteById(123);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getCoursesBySemester() throws Exception {
        when(courseRepo.findAllBySemesterId(1)).thenReturn(Arrays.asList(new Course(1, 1, 2, "Good course"),
                new Course(2, 1, 23, "Better course"),
                new Course(3, 1, 14, "Best course")));

        mvc.perform(get("/semester/1/course"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].semesterId").value(1))
                .andExpect(jsonPath("$[0].moduleId").value(2))
                .andExpect(jsonPath("$[0].name").value("Good course"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].semesterId").value(1))
                .andExpect(jsonPath("$[1].moduleId").value(23))
                .andExpect(jsonPath("$[1].name").value("Better course"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].semesterId").value(1))
                .andExpect(jsonPath("$[2].moduleId").value(14))
                .andExpect(jsonPath("$[2].name").value("Best course"));

    }

}
