package de.kreuzenonline.kreuzen.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.comment.requests.CreateCommentRequest;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;

import de.kreuzenonline.kreuzen.comment.requests.UpdateCommentRequest;
import de.kreuzenonline.kreuzen.question.BaseQuestionRepo;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class CommentControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private CommentRepo commentRepo;

    @MockBean
    private BaseQuestionRepo questionRepo;



    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createComment() throws Exception {

        String com = new ObjectMapper().writeValueAsString(new CreateCommentRequest("Das ist ein Kommentar."));

        Instant inst1 = Instant.now();
        when(commentRepo.save(Mockito.any(Comment.class))).thenReturn(new Comment(1, 1, 45, "Das ist ein Kommentar.", inst1, inst1));

        mvc.perform(post("/question/1/comment").accept(MediaType.APPLICATION_JSON).content(com).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.questionId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.comment").value("Das ist ein Kommentar."))
                .andExpect(jsonPath("$.createdAt").value(inst1.toString()))
                .andExpect(jsonPath("$.updatedAt").value(inst1.toString()));


    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getComment() throws Exception {
        Instant inst1 = Instant.now();
        when(commentRepo.findById(1)).thenReturn(java.util.Optional.of(new Comment(1, 1, 45, "Das ist ein Kommentar.", inst1, inst1)));
        mvc.perform(get("/comment/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.questionId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.comment").value("Das ist ein Kommentar."))
                .andExpect(jsonPath("$.createdAt").value(inst1.toString()))
                .andExpect(jsonPath("$.updatedAt").value(inst1.toString()));
    }


    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getCommentsByQuestionId() throws Exception {
        Instant inst1 = Instant.now();
        when(commentRepo.getAllByQuestionId(1)).thenReturn(Collections.singletonList(new Comment(1, 1, 45, "Das ist ein Kommentar.", inst1, inst1)));
        mvc.perform(get("/question/1/comment")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].questionId").value(1))
                .andExpect(jsonPath("$[0].creatorId").value(45))
                .andExpect(jsonPath("$[0].comment").value("Das ist ein Kommentar."))
                .andExpect(jsonPath("$[0].createdAt").value(inst1.toString()))
                .andExpect(jsonPath("$[0].updatedAt").value(inst1.toString()));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void updateComment() throws Exception {
        Instant inst1 = Instant.now();
        String com = new ObjectMapper().writeValueAsString(new UpdateCommentRequest("Das ist ein Kommentar."));
        when(commentRepo.findById(1)).thenReturn(java.util.Optional.of(new Comment(1, 1, 45, "Das ist ein Kommentar", inst1, inst1)));
        when(commentRepo.save(Mockito.any(Comment.class))).thenAnswer(c -> c.getArgument(0));
        mvc.perform(patch("/comment/1").accept(MediaType.APPLICATION_JSON).content(com).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.questionId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.comment").value("Das ist ein Kommentar."))
                .andExpect(jsonPath("$.createdAt").value(inst1.toString()))
                .andExpect(jsonPath("$.updatedAt").value(inst1.toString()));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 44)
    public void updateCommentNotUser() throws Exception {
        Instant inst1 = Instant.now();
        String com = new ObjectMapper().writeValueAsString(new UpdateCommentRequest("Das ist ein Kommentar."));
        when(commentRepo.findById(1)).thenReturn(java.util.Optional.of(new Comment(1, 1, 45, "Das ist ein Kommentar.", inst1, inst1)));
        when(commentRepo.save(Mockito.any(Comment.class))).thenAnswer(c -> c.getArgument(0));
        mvc.perform(patch("/comment/1").accept(MediaType.APPLICATION_JSON).content(com).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void deleteComment() throws Exception {

        Instant inst1 = Instant.now();
        when(commentRepo.findById(1)).thenReturn(java.util.Optional.of(new Comment(1, 1, 45, "Das ist ein Kommentar", inst1, inst1)));
        mvc.perform(delete("/comment/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(commentRepo, times(1)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getCommentById404() throws Exception {
        when(commentRepo.findById(1)).thenReturn(Optional.empty());
        mvc.perform(get("/comment/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.msg").value("Der Kommentar konnte nicht gefunden werden."));
    }


}


