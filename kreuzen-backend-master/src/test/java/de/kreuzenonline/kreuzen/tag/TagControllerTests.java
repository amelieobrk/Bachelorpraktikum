package de.kreuzenonline.kreuzen.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.BaseQuestionRepo;
import de.kreuzenonline.kreuzen.tag.requests.CreateTagRequest;
import de.kreuzenonline.kreuzen.tag.requests.UpdateTagRequest;
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
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class TagControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private TagRepo tagRepo;
    @MockBean
    private BaseQuestionRepo baseQuestionRepo;

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "MOD")
    public void createTagAsMod() throws Exception {
        String tag = new ObjectMapper().writeValueAsString(new CreateTagRequest("Anatomie"));
        when(tagRepo.save(Mockito.any(Tag.class))).thenReturn(new Tag(1, "Anatomie", 2));

        mvc.perform(post("/module/2/tag").accept(MediaType.APPLICATION_JSON).content(tag).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Anatomie"))
                .andExpect(jsonPath("$.moduleId").value(2));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createTagFailsAsUser() throws Exception {
        String tag = new ObjectMapper().writeValueAsString(new CreateTagRequest("Anatomie"));
        when(tagRepo.save(Mockito.any(Tag.class))).thenReturn(new Tag(1, "Anatomie", 2));

        mvc.perform(post("/module/2/tag").accept(MediaType.APPLICATION_JSON).content(tag).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(tagRepo, times(0)).save(Mockito.any(Tag.class));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createTagFailsNameTooShort() throws Exception {
        String tag = new ObjectMapper().writeValueAsString(new CreateTagRequest("An"));
        when(tagRepo.save(Mockito.any(Tag.class))).thenReturn(new Tag(1, "An", 2));

        mvc.perform(post("/module/2/tag").accept(MediaType.APPLICATION_JSON).content(tag).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Der Name des Tags sollte aus mindestens 3 Zeichen bestehen."));
        verify(tagRepo, times(0)).save(Mockito.any(Tag.class));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createTagFailsNameNotSet() throws Exception {
        String tag = new ObjectMapper().writeValueAsString(new CreateTagRequest(null));
        when(tagRepo.save(Mockito.any(Tag.class))).thenReturn(new Tag(1, "", 2));

        mvc.perform(post("/module/2/tag").accept(MediaType.APPLICATION_JSON).content(tag).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Der Tag muss einen Namen haben."));
        verify(tagRepo, times(0)).save(Mockito.any(Tag.class));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getTag() throws Exception {
        when(tagRepo.findById(3)).thenReturn(java.util.Optional.of(new Tag(3, "Biochemie", 7)));

        mvc.perform(get("/tag/3")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Biochemie"))
                .andExpect(jsonPath("$.moduleId").value(7));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getTagFailsForUser() throws Exception {
        mvc.perform(get("/tag/4"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getTagNotFound() throws Exception {
        when(tagRepo.findById(123)).thenReturn(Optional.empty());

        mvc.perform(get("/tag/123")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getTagsByModule() throws Exception {
        when(tagRepo.findAllByModuleId(2)).thenReturn(Arrays.asList(new Tag(1, "Anatomie", 2),
                new Tag(2, "Histologie", 2),
                new Tag(3, "Biochemie", 2)));

        mvc.perform(get("/module/2/tag").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("Anatomie"))
                .andExpect(jsonPath("$.[0].moduleId").value(2))
                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].name").value("Histologie"))
                .andExpect(jsonPath("$.[1].moduleId").value(2))
                .andExpect(jsonPath("$.[2].id").value(3))
                .andExpect(jsonPath("$.[2].name").value("Biochemie"))
                .andExpect(jsonPath("$.[2].moduleId").value(2));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "MOD")
    public void updateTagSuccessful() throws Exception {
        String tag = new ObjectMapper().writeValueAsString(new UpdateTagRequest("Histologie"));
        when(tagRepo.findById(2))
                .thenReturn(java.util.Optional.of(new Tag(2, "Anatomie", 12)));
        when(tagRepo.save(Mockito.any(Tag.class)))
                .thenAnswer(t -> t.getArgument(0));

        mvc.perform(patch("/tag/2")
                .accept(MediaType.APPLICATION_JSON)
                .content(tag)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Histologie"))
                .andExpect(jsonPath("$.moduleId").value(12));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateTagFailsForUser() throws Exception {
        String tag = new ObjectMapper().writeValueAsString(new UpdateTagRequest("Histologie"));

        mvc.perform(patch("/tag/2")
                .accept(MediaType.APPLICATION_JSON)
                .content(tag)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur Moderatoren und Administratoren dürfen Tags bearbeiten."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateTagFailsWithoutName() throws Exception {
        String tag = new ObjectMapper().writeValueAsString(new UpdateTagRequest());

        mvc.perform(patch("/tag/2")
                .accept(MediaType.APPLICATION_JSON)
                .content(tag)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Es muss ein Name für den Tag angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateTagFailsNameTooLong() throws Exception {
        String tag = new ObjectMapper().writeValueAsString(new UpdateTagRequest("MehrAls32ZeichenMehrAls32ZeichenMehrAls32Zeichen"));

        mvc.perform(patch("/tag/2")
                .accept(MediaType.APPLICATION_JSON)
                .content(tag)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Der Name des Tags sollte aus höchstens 32 Zeichen bestehen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void deleteTagSuccessful() throws Exception {
        doNothing().when(tagRepo).deleteById(4);
        mvc.perform(delete("/tag/4"))
                .andExpect(status().is(204));
        verify(tagRepo, times(1)).deleteById(4);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void deleteTagFailsForUser() throws Exception {
        mvc.perform(delete("/tag/4"))
                .andExpect(status().is(403));
        verify(tagRepo, times(0)).deleteById(4);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getTagsByQuestion() throws Exception {
        when(tagRepo.findAllByQuestionId(2)).thenReturn(Arrays.asList(new Tag(1, "Anatomie", 2),
                new Tag(2, "Histologie", 2),
                new Tag(3, "Biochemie", 2)));

        mvc.perform(get("/question/2/tag").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("Anatomie"))
                .andExpect(jsonPath("$.[0].moduleId").value(2))
                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].name").value("Histologie"))
                .andExpect(jsonPath("$.[1].moduleId").value(2))
                .andExpect(jsonPath("$.[2].id").value(3))
                .andExpect(jsonPath("$.[2].name").value("Biochemie"))
                .andExpect(jsonPath("$.[2].moduleId").value(2));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void addTagToQuestion() throws Exception {
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", true)));
        when(tagRepo.findById(1)).thenReturn(Optional.of(new Tag(1, "Anatomie", 2)));
        doNothing().when(tagRepo).addTagToQuestion(1, 1);
        mvc.perform(put("/question/1/tag/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Anatomie"))
                .andExpect(jsonPath("$.moduleId").value(2));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void removeTagFromQuestion() throws Exception {
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", true)));
        doNothing().when(tagRepo).removeTagFromQuestion(1, 1);
        mvc.perform(delete("/question/1/tag/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(tagRepo, times(1)).removeTagFromQuestion(1, 1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void addTagToQuestionNotAuthorized() throws Exception {

        mvc.perform(delete("/question/1/tag/2"))
                .andExpect(status().is4xxClientError());
        verify(tagRepo, times(0)).removeTagFromQuestion(1, 2);

    }
}
