package de.kreuzenonline.kreuzen.major;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.major.requests.CreateMajorRequest;
import de.kreuzenonline.kreuzen.major.requests.UpdateMajorRequest;
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

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class MajorControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private MajorRepo majorRepo;


    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getMajorByUniversityId() throws Exception {

        when(majorRepo.findAllByUniversityId(1)).thenReturn(Collections.singletonList(new Major(1, "Medicine", 1)));
        mvc.perform(get("/university/1/major").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("Medicine"))
                .andExpect(jsonPath("$[*].universityId").value(1));
    }


    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createMajor() throws Exception {
        String maj = new ObjectMapper().writeValueAsString(new CreateMajorRequest("Medicine"));
        when(majorRepo.save(Mockito.any(Major.class))).thenReturn(new Major(1, "Medicine", 1));
        mvc.perform(post("/university/1/major").accept(MediaType.APPLICATION_JSON).content(maj).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Medicine"))
                .andExpect(jsonPath("$.universityId").value(1));
    }




    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getMajorById() throws Exception {
        when(majorRepo.findById(1)).thenReturn(java.util.Optional.of(new Major(1, "Medicine", 1)));
        mvc.perform(get("/major/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Medicine"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getMajorById404() throws Exception {
        when(majorRepo.findById(1)).thenReturn(Optional.empty());
        mvc.perform(get("/major/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.msg").value("Der gesuchte Studiengang konnte nicht gefunden werden."));

    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateMajor() throws Exception {
        String major = new ObjectMapper().writeValueAsString(new UpdateMajorRequest("Psychology"));
        when(majorRepo.findById(1)).thenReturn(java.util.Optional.of(new Major(1, "Medicine", 1)));
        when(majorRepo.save(Mockito.any(Major.class))).thenAnswer(m -> m.getArgument(0));
        mvc.perform(patch("/major/1").accept(MediaType.APPLICATION_JSON).content(major).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name").value("Psychology"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.universityId").value(1));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void deleteMajor() throws Exception {
        doNothing().when(majorRepo).deleteById(1);
        mvc.perform(delete("/major/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(majorRepo, times(1)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 1)
    public void getMajorByUserId() throws Exception {
        List<Major> l = new ArrayList<>();
        l.add(new Major(1, "Medicine", 1));
        l.add(new Major(2, "Psychology", 1));
        when(majorRepo.findAllByUser(1)).thenReturn(l);
        mvc.perform(get("/user/1/major").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Medicine"))
                .andExpect(jsonPath("$[0].universityId").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].name").value("Psychology"))
                .andExpect(jsonPath("$[1].universityId").value("1"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getMajorByUserIdNotPossible() throws Exception {
        List<Major> l = new ArrayList<>();
        l.add(new Major(1, "Medicine", 1));
        l.add(new Major(2, "Psychology", 1));
        when(majorRepo.findAllByUser(1)).thenReturn(l);
        mvc.perform(get("/user/1/major").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Du darfst nur deine eigenen Studiengänge sehen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void addMajor() throws Exception {
        when(majorRepo.findById(1)).thenReturn(java.util.Optional.of(new Major(1, "Medicine", 1)));
        doNothing().when(majorRepo).addMajorToUser(1, 1);
        mvc.perform(put("/user/1/major/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name").value("Medicine"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.universityId").value(1));

        verify(majorRepo, times(1)).addMajorToUser(1, 1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void addMajorNotPossible() throws Exception {
        when(majorRepo.findById(1)).thenReturn(java.util.Optional.of(new Major(1, "Medicine", 1)));
        doNothing().when(majorRepo).addMajorToUser(1, 1);

        mvc.perform(put("/user/1/major/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verify(majorRepo, times(0)).addMajorToUser(1, 1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void addMajor404() throws Exception {
        when(majorRepo.findById(1)).thenReturn(java.util.Optional.empty());
        doNothing().when(majorRepo).addMajorToUser(1, 1);
        mvc.perform(put("/user/1/major/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.msg").value("Der gesuchte Studiengang konnte nicht gefunden werden."));

    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void removeMajorFromUser() throws Exception {
        doNothing().when(majorRepo).removeMajorFromUser(1, 1);
        mvc.perform(delete("/user/1/major/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(majorRepo, times(1)).removeMajorFromUser(1, 1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 1)
    public void getMajorByModule() throws Exception {
        when(majorRepo.findAllByModuleId(1)).thenReturn(Arrays.asList(
                new Major(1, "Med", 1),
                new Major(2,"Psy",1)));
        mvc.perform(get("/module/1/major").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Med"))
                .andExpect(jsonPath("$[0].universityId").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].name").value("Psy"))
                .andExpect(jsonPath("$[1].universityId").value("1"));



    }


}
