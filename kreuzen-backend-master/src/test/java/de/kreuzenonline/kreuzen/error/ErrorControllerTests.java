package de.kreuzenonline.kreuzen.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.comment.Comment;
import de.kreuzenonline.kreuzen.comment.CommentRepo;
import de.kreuzenonline.kreuzen.error.requests.CreateErrorRequest;
import de.kreuzenonline.kreuzen.error.requests.UpdateErrorRequest;
import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.BaseQuestionRepo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.annotation.Id;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class ErrorControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ErrorRepo errorRepo;
    @MockBean
    private BaseQuestionRepo questionRepo;
    @MockBean
    private CommentRepo commentRepo;

    private Integer id;
    private Integer questionId;
    private Integer creatorId;
    private String comment;
    private String source;
    private Boolean isResolved;
    private Integer lastAssignedModeratorId;
    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getError() throws Exception {
        when(errorRepo.findById(1)).thenReturn(java.util.Optional.of(new Error(1, 1, 45, "Fehler in Frage", "Link", false, 10)));
        mvc.perform(get("/error/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.questionId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.comment").value("Fehler in Frage"))
                .andExpect(jsonPath("$.source").value("Link"))
                .andExpect(jsonPath("$.isResolved").value(false))
                .andExpect(jsonPath("$.lastAssignedModeratorId").value(10));

    }
    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role= "ADMIN")
    public void getErrorById404() throws Exception {
        when(errorRepo.findById(1)).thenReturn(Optional.empty());
        mvc.perform(get("/error/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.msg").value("Der Fehlerbericht konnte nicht gefunden werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getErrorNotAdmin() throws Exception {
        when(errorRepo.findById(1)).thenReturn(java.util.Optional.of(new Error(1, 1, 45, "Fehler in Frage", "Link", false, 10)));
        mvc.perform(get("/errorReport/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void createError() throws Exception {
        String report = new ObjectMapper().writeValueAsString(new CreateErrorRequest("Fehler in Frage", null));
        Instant inst1 = Instant.now();
        when(errorRepo.save(Mockito.any(Error.class))).thenReturn(new Error(1, 1, 45, "Fehler in Frage", "Link", false, 10));
        when(commentRepo.save(Mockito.any(Comment.class))).thenReturn(new Comment(1, 1, 1, "Kommentar",inst1, inst1 ));
        when(questionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Question", "Single-Chouce", "Add", 2, 1, 1, 1, 1, "Origin", true)));
        mvc.perform(post("/question/1/error").accept(MediaType.APPLICATION_JSON).content(report).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.questionId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.comment").value("Fehler in Frage"))
                .andExpect(jsonPath("$.source").value("Link"))
                .andExpect(jsonPath("$.isResolved").value(false))
                .andExpect(jsonPath("$.lastAssignedModeratorId").value(10));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateError() throws Exception {
        String errorReport = new ObjectMapper().writeValueAsString(new UpdateErrorRequest("Updated", "Link", true));
        when(errorRepo.findById(1)).thenReturn(java.util.Optional.of(new Error(1, 1, 45, "Not Updated", "No Link", false, 5)));
        when(errorRepo.save(Mockito.any(Error.class))).thenAnswer(e -> e.getArgument(0));
        mvc.perform(patch("/error/1").accept(MediaType.APPLICATION_JSON).content(errorReport).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.comment").value("Updated"))
                .andExpect(jsonPath("$.source").value("Link"))
                .andExpect(jsonPath("$.isResolved").value(true))
                .andExpect(jsonPath("$.lastAssignedModeratorId").value(45));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateErrorNotAdmin() throws Exception {
        String errorReport = new ObjectMapper().writeValueAsString(new UpdateErrorRequest("Updated", "Link", true));
        mvc.perform(patch("/error/1")
                .accept(MediaType.APPLICATION_JSON)
                .content(errorReport)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role="ADMIN")
    public void deleteError() throws Exception {
        when(errorRepo.findById(1)).thenReturn(java.util.Optional.of(new Error(1, 1, 45, "Not Updated", "No Link", false, 5)));
        doNothing().when(errorRepo).deleteById(1);
        mvc.perform(delete("/error/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(errorRepo, times(1)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void deleteErrorNotAdmin() throws Exception {
        doNothing().when(errorRepo).deleteById(1);
        mvc.perform(delete("/error/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getErrorUnsolved() throws Exception {
        when(errorRepo.findAllByIsResolved(false)).thenReturn(Arrays.asList(
                new Error(1, 1, 20, "Falsch1", "Link1", false, 2),
                new Error(2,5, 10, "Falsch2", "Link2", false,3)));
        mvc.perform(get("/error/unsolved").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

}


