package de.kreuzenonline.kreuzen.university;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.university.requests.CreateUniversityRequest;
import de.kreuzenonline.kreuzen.university.requests.UpdateUniversityRequest;
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
public class UniversityControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UniversityRepo universityRepo;


    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getAllUniversities() throws Exception {
        when(universityRepo.findAll()).thenReturn(Collections.singletonList(new University(1, "Uni", new String[]{"uni-mail.de"})));
        mvc.perform(get("/university").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[*].id").value(1))
                .andExpect(jsonPath("$[*].name").value("Uni"))
                .andExpect(jsonPath("$[0][*][0]").value("uni-mail.de"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getUniversityById() throws Exception {
        when(universityRepo.findById(1)).thenReturn(java.util.Optional.of(new University(1, "Uni", new String[]{"test.de"})));
        mvc.perform(get("/university/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Uni"))
                .andExpect(jsonPath("$.allowedMailDomains").value("test.de"));
    }



    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getUniversityByIdNotFoundCheck() throws Exception {
        mvc.perform(get("/university/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.msg").value("Die gesuchte Universität konnte nicht gefunden werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role="ADMIN")
    public void createNewUniversity() throws Exception {
        String uni = new ObjectMapper().writeValueAsString(new CreateUniversityRequest("TU Darmstadt", new String[]{"tu.de", "tuu.de"}));
        when(universityRepo.save(Mockito.any(University.class))).thenReturn(new University(1, "TU Darmstadt", new String[]{"tu.de", "tuu.de"}));
        mvc.perform(post("/university").accept(MediaType.APPLICATION_JSON)
                .content(uni)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name").value("TU Darmstadt"))
                .andExpect(jsonPath("$.allowedMailDomains[0]").value("tu.de"))
                .andExpect(jsonPath("$.allowedMailDomains[1]").value("tuu.de"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 40)
    public void createNewUniversityNotAdmin() throws Exception {
        String uni = new ObjectMapper().writeValueAsString(new CreateUniversityRequest("TU Darmstadt", new String[]{"tu.de", "tuu.de"}));
        when(universityRepo.save(Mockito.any(University.class))).thenReturn(new University(1, "TU Darmstadt", new String[]{"tu.de", "tuu.de"}));
        mvc.perform(post("/university").accept(MediaType.APPLICATION_JSON)
                .content(uni)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.msg").value("Nur Administratoren dürfen Universitäten anlegen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role="ADMIN")
    public void updateUniversity() throws Exception {

        String uni = new ObjectMapper().writeValueAsString(new UpdateUniversityRequest("TU Darmstadt", new String[]{"tu.de", "tuu.de"}));
        when(universityRepo.findById(1)).thenReturn(java.util.Optional.of(new University(1, "Uni", new String[]{"test.de"})));
        when(universityRepo.save(Mockito.any(University.class))).then(u -> u.getArgument(0));
        mvc.perform(patch("/university/1").accept(MediaType.APPLICATION_JSON).content(uni).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name").value("TU Darmstadt"))
                .andExpect(jsonPath("$.allowedMailDomains[0]").value("tu.de"))
                .andExpect(jsonPath("$.allowedMailDomains[1]").value("tuu.de"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 40)
    public void updateUniversityNotAdmin() throws Exception {

        String uni = new ObjectMapper().writeValueAsString(new UpdateUniversityRequest("TU Darmstadt", new String[]{"tu.de", "tuu.de"}));
        when(universityRepo.findById(1)).thenReturn(java.util.Optional.of(new University(1, "Uni", new String[]{"test.de"})));
        when(universityRepo.save(Mockito.any(University.class))).then(u -> u.getArgument(0));
        mvc.perform(patch("/university/1").accept(MediaType.APPLICATION_JSON).content(uni).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.msg").value("Nur Administratoren dürfen Universitäten bearbeiten/löschen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role="ADMIN")
    public void updateUniversity404() throws Exception {

        String uni = new ObjectMapper().writeValueAsString(new UpdateUniversityRequest("TU Darmstadt", new String[]{"tu.de", "tuu.de"}));
        when(universityRepo.findById(1)).thenReturn(Optional.empty());
        when(universityRepo.save(Mockito.any(University.class))).then(u -> u.getArgument(0));
        mvc.perform(patch("/university/1").accept(MediaType.APPLICATION_JSON).content(uni).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.msg").value("Die gesuchte Universität konnte nicht gefunden werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role="ADMIN")
    public void deleteUniversity() throws Exception {
        doNothing().when(universityRepo).deleteById(1);
        mvc.perform(delete("/university/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(universityRepo, times(1)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 40)
    public void deleteUniversityNotAdmin() throws Exception {
        doNothing().when(universityRepo).deleteById(1);
        mvc.perform(delete("/university/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.msg").value("Nur Administratoren dürfen Universitäten bearbeiten/löschen."));
        verify(universityRepo, times(0)).deleteById(1);
    }


}
