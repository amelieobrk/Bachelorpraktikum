package de.kreuzenonline.kreuzen.hint;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.hint.requests.CreateHintRequest;
import de.kreuzenonline.kreuzen.hint.requests.UpdateHintRequest;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc

public class HintControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private HintRepo hintRepo;

    @Test
    public void hintsNotAccessibleForNonUsers() throws Exception {
        mvc.perform(get("/hint/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(get("/hint/random"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(get("/hint"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(post("/hint"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(patch("/hint/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(delete("/hint/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getHintById() throws Exception {
        when(hintRepo.findById(1)).thenReturn(java.util.Optional.of(new Hint(1, "Answers can be crossed, when you think that they are wrong!", true)));

        mvc.perform(get("/hint/1"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Answers can be crossed, when you think that they are wrong!"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getHintByIdNotFound() throws Exception {
        mvc.perform(get("/hint/1"))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.msg").value("Der gewünschte Tipp des Tages konnte nicht gefunden werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getHintByIdNotForUser() throws Exception {
        when(hintRepo.findById(1)).thenReturn(java.util.Optional.of(new Hint(1, "Answers can be crossed, when you think that they are wrong!", true)));

        mvc.perform(get("/hint/1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur Moderatoren und Administratoren können einen Tipp des Tages per Id abrufen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getAllHints() throws Exception {
        when(hintRepo.findAll()).thenReturn(Arrays.asList(
                new Hint(1, "Hint 1", true),
                new Hint(2, "Hint 2", false),
                new Hint(3, "Hint 3", true)
        ));

        mvc.perform(get("/hint"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].text").value("Hint 1"))
                .andExpect(jsonPath("$.[0].isActive").value(true))
                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].text").value("Hint 2"))
                .andExpect(jsonPath("$.[1].isActive").value(false))
                .andExpect(jsonPath("$.[2].id").value(3))
                .andExpect(jsonPath("$.[2].text").value("Hint 3"))
                .andExpect(jsonPath("$.[2].isActive").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getAllHintsNotForUser() throws Exception {
        mvc.perform(get("/hint"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur Moderatoren und Administratoren können die Liste aller Tipps des Tages einsehen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getRandomHint() throws Exception {
        when(hintRepo.findAllByIsActiveTrue()).thenReturn(Arrays.asList(
                new Hint(1, "Hint 1", true),
                new Hint(2, "Hint 2", true),
                new Hint(3, "Hint 3", true)));

        mvc.perform(get("/hint/random"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getRandomHintNotPossible() throws Exception {
        mvc.perform(get("/hint/random"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Aktuell gibt es keine Tipps des Tages, die angezeigt werden können."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createHint() throws Exception {
        String hint = new ObjectMapper().writeValueAsString(new CreateHintRequest("Test Hint Text", true));
        when(hintRepo.save(Mockito.any(Hint.class))).thenReturn(new Hint(1, "Test Hint Text", true));

        mvc.perform(post("/hint")
                .accept(MediaType.APPLICATION_JSON)
                .content(hint)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Test Hint Text"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createHintTextTooLong() throws Exception {
        String hint = new ObjectMapper().writeValueAsString(new CreateHintRequest("Test Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint TextTest Hint Text", true));
        when(hintRepo.save(Mockito.any(Hint.class))).thenReturn(new Hint(1, "Test Hint Text", true));

        mvc.perform(post("/hint")
                .accept(MediaType.APPLICATION_JSON)
                .content(hint)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Der Text für einen Tipp des Tages sollte nicht länger als 1000 Zeichen sein."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createHintNotAllowed() throws Exception {
        String hint = new ObjectMapper().writeValueAsString(new CreateHintRequest("Test Hint Text", true));
        when(hintRepo.save(Mockito.any(Hint.class))).thenReturn(new Hint(1, "Test Hint Text", true));

        mvc.perform(post("/hint")
                .accept(MediaType.APPLICATION_JSON)
                .content(hint)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur Moderatoren und Administratoren dürfen einen Tipp des Tages erstellen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateHint() throws Exception {
        String hintUpdate = new ObjectMapper().writeValueAsString(new UpdateHintRequest("new Text", false));
        when(hintRepo.findById(1)).thenReturn(java.util.Optional.of(new Hint(1, "Answers can be crossed, when you think that they are wrong!", true)));
        when(hintRepo.existsByTextIgnoreCase("new Text")).thenReturn(false);
        when(hintRepo.save(Mockito.any(Hint.class))).thenAnswer(h -> h.getArgument(0));

        mvc.perform(patch("/hint/1")
                .accept(MediaType.APPLICATION_JSON)
                .content(hintUpdate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("new Text"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateHintNotAllowed() throws Exception {
        String hintUpdate = new ObjectMapper().writeValueAsString(new UpdateHintRequest("new Text", false));
        when(hintRepo.findById(1)).thenReturn(java.util.Optional.of(new Hint(1, "Answers can be crossed, when you think that they are wrong!", true)));
        when(hintRepo.existsByTextIgnoreCase("new Text")).thenReturn(false);
        when(hintRepo.save(Mockito.any(Hint.class))).thenAnswer(h -> h.getArgument(0));

        mvc.perform(patch("/hint/1")
                .accept(MediaType.APPLICATION_JSON)
                .content(hintUpdate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur Moderatoren und Administratoren dürfen einen Tipp des Tages bearbeiten."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void deleteHint() throws Exception {
        mvc.perform(delete("/hint/1"))
                .andExpect(status().is(204));
        verify(hintRepo, times(1)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void deleteHintNotAllowed() throws Exception {
        mvc.perform(delete("/hint/1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur Moderatoren und Administratoren dürfen einen Tipp des Tages löschen."));
        verify(hintRepo, times(0)).deleteById(1);
    }
}
