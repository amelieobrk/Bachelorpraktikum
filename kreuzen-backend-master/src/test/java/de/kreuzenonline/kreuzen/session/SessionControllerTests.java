package de.kreuzenonline.kreuzen.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.BaseQuestionRepo;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceQuestionEntry;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceQuestionRepo;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceQuestionEntry;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceQuestionRepo;
import de.kreuzenonline.kreuzen.session.requests.*;
import de.kreuzenonline.kreuzen.session.selections.MultipleChoiceSelection;
import de.kreuzenonline.kreuzen.session.selections.SingleChoiceSelection;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class SessionControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private SessionRepo sessionRepo;
    @MockBean
    private BaseQuestionRepo baseQuestionRepo;
    @MockBean
    private SingleChoiceQuestionRepo singleChoiceQuestionRepo;
    @MockBean
    private MultipleChoiceQuestionRepo multipleChoiceQuestionRepo;
    @MockBean
    private MultipleChoiceSelectionRepo multipleChoiceSelectionRepo;
    @MockBean
    private SingleChoiceSelectionRepo singleChoiceSelectionRepo;
    @MockBean
    private SessionQuestionRepo sessionQuestionRepo;

    @Test
    public void noAccessForNonUsers() throws Exception {
        mvc.perform(get("/session/1"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/session"))
                .andExpect(status().isUnauthorized());
        mvc.perform(patch("/session/1"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/session/1"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/session/1/question/1/selection"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/session/1/question?limit=20&skip=0&textFilter=test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getSessionNotFound() throws Exception {
        mvc.perform(get("/session/2021"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("Die gewünschte Session konnte nicht gefunden werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getSessionSuccess() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "Here could be an annotation.", "exam", "TestSession", true, false)));
        mvc.perform(get("/session/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.name").value("TestSession"))
                .andExpect(jsonPath("$.sessionType").value("exam"))
                .andExpect(jsonPath("$.isRandom").value(true))
                .andExpect(jsonPath("$.notes").value("Here could be an annotation."))
                .andExpect(jsonPath("$.isFinished").value(false));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 27)
    public void getSessionFromAnotherUserNotPossible() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "Here could be an annotation.", "exam", "TestSession", true, false)));
        mvc.perform(get("/session/1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Die Session kann nur vom Ersteller und Administratoren eingesehen werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createSessionSuccess() throws Exception {
        String session = objectMapper.writeValueAsString(new CreateSessionRequest("TestSession", "exam", true, "Here could be an annotation.", null, null, null, null, null, null));
        when(sessionRepo.save(Mockito.any(Session.class))).thenReturn(new Session(1, 45, "Here could be an annotation.", "exam", "TestSession", true, false));

        mvc.perform(post("/session")
                .accept(MediaType.APPLICATION_JSON).content(session).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.name").value("TestSession"))
                .andExpect(jsonPath("$.sessionType").value("exam"))
                .andExpect(jsonPath("$.isRandom").value(true))
                .andExpect(jsonPath("$.notes").value("Here could be an annotation."))
                .andExpect(jsonPath("$.isFinished").value(false));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createSessionConflicts() throws Exception {
        String sessionWithoutName = objectMapper.writeValueAsString(new CreateSessionRequest(null, "exam", true, "Here could be an annotation.", null, null, null, null, null, null));
        String sessionWithoutType = objectMapper.writeValueAsString(new CreateSessionRequest("TestSession", null, true, "Here could be an annotation.", null, null, null, null, null, null));
        String sessionWithoutIsRandom = objectMapper.writeValueAsString(new CreateSessionRequest("TestSession", "exam", null, "Here could be an annotation.", null, null, null, null, null, null));
        String sessionWithNameTooShort = objectMapper.writeValueAsString(new CreateSessionRequest("Te", "exam", true, "Here could be an annotation.", null, null, null, null, null, null));
        String sessionWithNameTooLong = objectMapper.writeValueAsString(new CreateSessionRequest("TestSessionTestSessionTestSessionTestSessionTestSessionTestSession", "exam", true, "Here could be an annotation.", null, null, null, null, null, null));
        String sessionWithNotesTooLong = objectMapper.writeValueAsString(new CreateSessionRequest("TestSession", "exam", true, "TestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSession", null, null, null, null, null, null));

        mvc.perform(post("/session")
                .accept(MediaType.APPLICATION_JSON).content(sessionWithoutName).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Es muss ein Name für die Session angegeben werden."));
        mvc.perform(post("/session")
                .accept(MediaType.APPLICATION_JSON).content(sessionWithoutType).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Es muss angegeben werden, ob es sich um eine Übungs- oder eine Lernsession handelt."));
        mvc.perform(post("/session")
                .accept(MediaType.APPLICATION_JSON).content(sessionWithoutIsRandom).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Es muss angegeben werden, ob die Fragen der Session in zufälliger Reihenfolge angezeigt werden sollen."));
        mvc.perform(post("/session")
                .accept(MediaType.APPLICATION_JSON).content(sessionWithNameTooShort).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Der Name einer Session sollte mindestens 3 Zeichen haben."));
        mvc.perform(post("/session")
                .accept(MediaType.APPLICATION_JSON).content(sessionWithNameTooLong).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Der Name einer Session sollte höchstens 64 Zeichen haben."));
        mvc.perform(post("/session")
                .accept(MediaType.APPLICATION_JSON).content(sessionWithNotesTooLong).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Die Anmerkungen zu einer Session sollten nicht mehr als 1.000 Zeichen beinhalten."));

    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateSession() throws Exception {
        String request = objectMapper.writeValueAsString(new UpdateSessionRequest("new Name", "practice", false, "Now the questions won't appear randomly anymore."));

        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        when(sessionRepo.save(Mockito.any(Session.class))).thenAnswer(s -> s.getArgument(0));

        mvc.perform(patch("/session/1").accept(MediaType.APPLICATION_JSON).content(request).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.name").value("new Name"))
                .andExpect(jsonPath("$.sessionType").value("practice"))
                .andExpect(jsonPath("$.isRandom").value(false))
                .andExpect(jsonPath("$.notes").value("Now the questions won't appear randomly anymore."))
                .andExpect(jsonPath("$.isFinished").value(false));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 43)
    public void updateSessionConflicts() throws Exception {
        String requestNotCreator = objectMapper.writeValueAsString(new UpdateSessionRequest("new Name", "practice", false, "Now the questions won't appear randomly anymore."));
        String requestNameTooShort = objectMapper.writeValueAsString(new UpdateSessionRequest("na", "practice", false, "Now the questions won't appear randomly anymore."));
        String requestNameTooLong = objectMapper.writeValueAsString(new UpdateSessionRequest("namenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamename", "practice", false, "Now the questions won't appear randomly anymore."));
        String requestNotesTooLong = objectMapper.writeValueAsString(new CreateSessionRequest("TestSession", "exam", true, "TestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSessionTestSession", null, null, null, null, null, null));

        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        when(sessionRepo.save(Mockito.any(Session.class))).thenAnswer(s -> s.getArgument(0));

        mvc.perform(patch("/session/1")
                .accept(MediaType.APPLICATION_JSON).content(requestNotCreator).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Eine Session darf nur durch den Ersteller oder Administratoren verändert werden."));
        mvc.perform(patch("/session/1")
                .accept(MediaType.APPLICATION_JSON).content(requestNameTooShort).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Der Name einer Session sollte mindestens 3 Zeichen haben."));
        mvc.perform(patch("/session/1")
                .accept(MediaType.APPLICATION_JSON).content(requestNameTooLong).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Der Name einer Session sollte höchstens 64 Zeichen haben."));
        mvc.perform(patch("/session/1")
                .accept(MediaType.APPLICATION_JSON).content(requestNotesTooLong).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Die Anmerkungen zu einer Session sollten nicht mehr als 1.000 Zeichen beinhalten."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void deleteSession() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        mvc.perform(delete("/session/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(sessionRepo, times(1)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 44)
    public void deleteSessionNotCreator() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        mvc.perform(delete("/session/1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Eine Session darf nur durch den Ersteller oder Administratoren gelöscht werden."));
        verify(sessionRepo, times(0)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getSelectionSingleChoice() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        when(baseQuestionRepo.findBySessionAndLocalId(1, 1)).thenReturn(new BaseQuestion(12, "Question in a session", "single-choice", null, 1, 1, 23, 45, 45, "STUD", false));
        when(singleChoiceSelectionRepo.findSingleChoiceSelections(1, 1)).thenReturn(Arrays.asList(
                new SingleChoiceSelection(1, 1, 1, 1, true, false),
                new SingleChoiceSelection(2, 1, 1, 2, false, true),
                new SingleChoiceSelection(3, 1, 1, 3, false, false)
        ));

        mvc.perform(get("/session/1/question/1/selection")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].localAnswerId").value(1))
                .andExpect(jsonPath("$.[0].isChecked").value(true))
                .andExpect(jsonPath("$.[0].isCrossed").value(false))
                .andExpect(jsonPath("$.[1].localAnswerId").value(2))
                .andExpect(jsonPath("$.[1].isChecked").value(false))
                .andExpect(jsonPath("$.[1].isCrossed").value(true))
                .andExpect(jsonPath("$.[2].localAnswerId").value(3))
                .andExpect(jsonPath("$.[2].isChecked").value(false))
                .andExpect(jsonPath("$.[2].isCrossed").value(false));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 459)
    public void getSelectionNotAllowed() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        when(baseQuestionRepo.findBySessionAndLocalId(1,1)).thenReturn(new BaseQuestion(12, "Question in a session", "multiple-choice", null, 1, 1, 1, 45, 45, "STUD", true));
        when(multipleChoiceSelectionRepo.findMultipleChoiceSelections(1, 1)).thenReturn(Arrays.asList(
                new MultipleChoiceSelection(1, 1, 1, 1, true, false),
                new MultipleChoiceSelection(2, 1, 1, 2, true, false),
                new MultipleChoiceSelection(3, 1, 1, 3, false, true)
        ));

        mvc.perform(get("/session/1/question/1/selection"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur der Session-Ersteller sowie Administratoren dürfen die Antworten auf Session-Fragen einsehen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getSelectionMultipleChoice() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        when(baseQuestionRepo.findBySessionAndLocalId(1,1)).thenReturn(new BaseQuestion(12, "Question in a session", "multiple-choice", null, 1, 1, 1, 45, 45, "STUD", true));
        when(multipleChoiceSelectionRepo.findMultipleChoiceSelections(1, 1)).thenReturn(Arrays.asList(
                new MultipleChoiceSelection(1, 1, 1, 1, true, false),
                new MultipleChoiceSelection(2, 1, 1, 2, true, false),
                new MultipleChoiceSelection(3, 1, 1, 3, false, true)
        ));

        mvc.perform(get("/session/1/question/1/selection")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].localAnswerId").value(1))
                .andExpect(jsonPath("$.[0].isChecked").value(true))
                .andExpect(jsonPath("$.[0].isCrossed").value(false))
                .andExpect(jsonPath("$.[1].localAnswerId").value(2))
                .andExpect(jsonPath("$.[1].isChecked").value(true))
                .andExpect(jsonPath("$.[1].isCrossed").value(false))
                .andExpect(jsonPath("$.[2].localAnswerId").value(3))
                .andExpect(jsonPath("$.[2].isChecked").value(false))
                .andExpect(jsonPath("$.[2].isCrossed").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getCount() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        when(sessionRepo.getQuestionCountBySessionId(1)).thenReturn(1234);
        mvc.perform(get("/session/question/count"))
                .andExpect(status().is(200));
        verify(sessionRepo, times(1)).getCountByParameters(new Integer[]{}, new Integer []{}, new Integer []{}, new String [] {}, new String []{}, null);

    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 40)
    public void getCountNotAllowed() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        when(sessionRepo.getQuestionCountBySessionId(1)).thenReturn(1234);
        mvc.perform(get("/session/1/question/count"))
                .andExpect(status().is(403));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getCountByParameters() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        when(sessionRepo.getCountByParameters(new Integer[]{}, new Integer []{}, new Integer []{}, new String [] {}, new String []{}, "test"))
                .thenReturn(12);
        mvc.perform(get("/session/question/count?textFilter=test"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.count").value(12));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 412)
    public void getCountByParametersNotAllowed() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "any notes", "exam", "name", true, false)));
        when(sessionRepo.getCountByParameters(null, null, null, null, null, "test"))
                .thenReturn(12);
        mvc.perform(get("/session/1/question/count?textFilter=test"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur der Ersteller der Session oder ein Administrator darf die Anzahl der Fragen mit bestimmten Parametern sehen."));
    }


    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getResult() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 12, 45, 45, 45, "STUD",  true)));
        when(baseQuestionRepo.findById(2)).thenReturn(java.util.Optional.of(new BaseQuestion(2, "Testfrage2", "multiple-choice", "Hier könnte ein Link stehen.", 2, 12, 45, 45, 45, "STUD",  true)));
        when(baseQuestionRepo.findById(3)).thenReturn(java.util.Optional.of(new BaseQuestion(3, "Testfrage3", "single-choice", "Hier könnte ein Link stehen.", 2, 12, 45, 45, 45, "IMPP",  true)));
        when(sessionQuestionRepo.findAllBySessionId(1)).thenReturn(Arrays.asList(
                new SessionQuestion(1, 1, 1, 1, 5, true),
                new SessionQuestion(2, 1, 2, 2, 5, true),
                new SessionQuestion(3, 1, 3, 3, 5, true)));
        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 1));
        when(singleChoiceQuestionRepo.findByQuestionId(3)).thenReturn(new SingleChoiceQuestionEntry(3, 3, 2));
        when(multipleChoiceQuestionRepo.findByQuestionId(2)).thenReturn(new MultipleChoiceQuestionEntry(2, 2, new Integer[]{2, 3}));
        when(singleChoiceSelectionRepo.findSingleChoiceSelections(1, 1)).thenReturn(Arrays.asList(
                new SingleChoiceSelection(1, 1, 1, 1, false, true),
                new SingleChoiceSelection(2, 1, 1, 2, true, false)));
        when(singleChoiceSelectionRepo.findSingleChoiceSelections(1, 3)).thenReturn(Arrays.asList(
                new SingleChoiceSelection(1, 1, 3, 1, false, false),
                new SingleChoiceSelection(2, 1, 3, 2, true, false)));
        when(multipleChoiceSelectionRepo.findMultipleChoiceSelections(1, 2)).thenReturn(Arrays.asList(
                new MultipleChoiceSelection(1, 1, 2, 1, false, true),
                new MultipleChoiceSelection(2, 1, 2, 2, true, false),
                new MultipleChoiceSelection(3, 1, 2, 3, true, false)));
        mvc.perform(get("/session/1/results"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0].sessionId").value(1))
                .andExpect(jsonPath("$[0][*].id").value(1))
                .andExpect(jsonPath("$[0].points").value(0))
                .andExpect(jsonPath("$[0].localId").value(1))
                .andExpect(jsonPath("$[1].sessionId").value(1))
                .andExpect(jsonPath("$[1][*].id").value(2))
                .andExpect(jsonPath("$[1].points").value(2))
                .andExpect(jsonPath("$[1].localId").value(2))
                .andExpect(jsonPath("$[2].sessionId").value(1))
                .andExpect(jsonPath("$[2][*].id").value(3))
                .andExpect(jsonPath("$[2].points").value(2))
                .andExpect(jsonPath("$[2].localId").value(3));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 40)
    public void getResultNotAllowed() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 12, 45, 45, 45, "STUD",  true)));
        when(baseQuestionRepo.findById(2)).thenReturn(java.util.Optional.of(new BaseQuestion(2, "Testfrage2", "multiple-choice", "Hier könnte ein Link stehen.", 2, 12, 45, 45, 45, "STUD",  true)));
        when(baseQuestionRepo.findById(3)).thenReturn(java.util.Optional.of(new BaseQuestion(3, "Testfrage3", "single-choice", "Hier könnte ein Link stehen.", 2, 12, 45, 45, 45, "IMPP",  true)));
        when(baseQuestionRepo.findAllBySession(1)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 12, 45, 45, 45, "STUD",  true),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", "Hier könnte ein Link stehen.", 2, 12, 45, 45, 45, "STUD",  true),
                new BaseQuestion(3, "Testfrage3", "single-choice", "Hier könnte ein Link stehen.", 2, 12, 45, 45, 45, "IMPP",  true)));
        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 1));
        when(singleChoiceQuestionRepo.findByQuestionId(3)).thenReturn(new SingleChoiceQuestionEntry(3, 3, 2));
        when(multipleChoiceQuestionRepo.findByQuestionId(2)).thenReturn(new MultipleChoiceQuestionEntry(2, 2, new Integer[]{2, 3}));
        when(singleChoiceSelectionRepo.findSingleChoiceSelections(1, 1)).thenReturn(Arrays.asList(
                new SingleChoiceSelection(1, 1, 1, 1, false, true),
                new SingleChoiceSelection(2, 1, 1, 2, true, false)));
        when(singleChoiceSelectionRepo.findSingleChoiceSelections(1, 3)).thenReturn(Arrays.asList(
                new SingleChoiceSelection(1, 1, 3, 1, false, false),
                new SingleChoiceSelection(2, 1, 3, 2, true, false)));
        when(multipleChoiceSelectionRepo.findMultipleChoiceSelections(1, 2)).thenReturn(Arrays.asList(
                new MultipleChoiceSelection(1, 1, 2, 1, false, true),
                new MultipleChoiceSelection(2, 1, 2, 2, true, false),
                new MultipleChoiceSelection(3, 1, 2, 3, true, false)));
        mvc.perform(get("/session/1/results"))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.msg").value("Nur der Ersteller der Session oder ein Administrator darf die Ergebnisse der Session einsehen."));


    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getSessionByUser() throws Exception {
        when(sessionRepo.getCountByUser(45)).thenReturn(3);
        when(sessionRepo.findAllPagination(45, 20, 0)).thenReturn(Arrays.asList(
                new Session(1, 45, "notes", "exam", "name", false, false),
                new Session(2, 45, "notes2", "exam2", "name2", false, false),
                new Session(3, 45, "notes3", "exam3", "name3", false, false)));
        mvc.perform(get("/user/45/session?limit=20&skip=0"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.entities.[0].id").value(1))
                .andExpect(jsonPath("$.entities.[0].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[0].notes").value("notes"))
                .andExpect(jsonPath("$.entities.[0].sessionType").value("exam"))
                .andExpect(jsonPath("$.entities.[0].name").value("name"))
                .andExpect(jsonPath("$.entities.[0].isRandom").value(false))
                .andExpect(jsonPath("$.entities.[0].isFinished").value(false))
                .andExpect(jsonPath("$.entities.[1].id").value(2))
                .andExpect(jsonPath("$.entities.[1].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[1].notes").value("notes2"))
                .andExpect(jsonPath("$.entities.[1].sessionType").value("exam2"))
                .andExpect(jsonPath("$.entities.[1].name").value("name2"))
                .andExpect(jsonPath("$.entities.[1].isRandom").value(false))
                .andExpect(jsonPath("$.entities.[1].isFinished").value(false))
                .andExpect(jsonPath("$.entities.[2].id").value(3))
                .andExpect(jsonPath("$.entities.[2].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[2].notes").value("notes3"))
                .andExpect(jsonPath("$.entities.[2].sessionType").value("exam3"))
                .andExpect(jsonPath("$.entities.[2].name").value("name3"))
                .andExpect(jsonPath("$.entities.[0].isRandom").value(false))
                .andExpect(jsonPath("$.entities.[0].isFinished").value(false));


    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 40)
    public void getSessionByUserNotAllowed() throws Exception {
        when(sessionRepo.getCountByUser(45)).thenReturn(3);
        when(sessionRepo.findAllPagination(45, 20, 0)).thenReturn(Arrays.asList(
                new Session(1, 45, "notes", "exam", "name", false, false),
                new Session(2, 45, "notes2", "exam2", "name2", false, false),
                new Session(3, 45, "notes3", "exam3", "name3", false, false)));
        mvc.perform(get("/user/45/session?limit=20&skip=0"))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.msg").value("Nur der Ersteller der Session oder ein Administrator darf die Anzahl der Sessions eines Users einsehen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void addSelectionSingle() throws Exception {
        String singleSelection = objectMapper.writeValueAsString(new SetSelectionRequest("single-choice", null, new Integer[]{2, 3}, 1));
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 1, true, false);
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 2, false, true);
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 3, false, true);
        when(sessionRepo.getSingleChoiceAnswerCount(1, 1)).thenReturn(3);
        when(singleChoiceSelectionRepo.findSingleChoiceSelections(1, 1)).thenReturn(Arrays.asList(
                new SingleChoiceSelection(1, 1, 1, 1, true, false),
                new SingleChoiceSelection(2, 1, 1, 2, false, true),
                new SingleChoiceSelection(3, 1, 1, 3, false, true)));
        mvc.perform(put("/session/1/question/1/selection").accept(MediaType.APPLICATION_JSON).content(singleSelection).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].localAnswerId").value(1))
                .andExpect(jsonPath("$.[0].isChecked").value(true))
                .andExpect(jsonPath("$.[0].isCrossed").value(false))
                .andExpect(jsonPath("$.[1].localAnswerId").value(2))
                .andExpect(jsonPath("$.[1].isChecked").value(false))
                .andExpect(jsonPath("$.[1].isCrossed").value(true))
                .andExpect(jsonPath("$.[2].localAnswerId").value(3))
                .andExpect(jsonPath("$.[2].isChecked").value(false))
                .andExpect(jsonPath("$.[2].isCrossed").value(true));
        verify(sessionRepo, times(1)).addSingleChoiceSelection(1, 1, 1, true, false);
        verify(sessionRepo, times(1)).addSingleChoiceSelection(1, 1, 2, false, true);
        verify(sessionRepo, times(1)).addSingleChoiceSelection(1, 1, 3, false, true);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void addSelectionMultiple() throws Exception {
        String multipleSelection = objectMapper.writeValueAsString(new SetSelectionRequest("multiple-choice", new Integer[]{1, 2}, new Integer[]{3, 4}, 2));
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        doNothing().when(sessionRepo).addMultipleChoiceSelection(1, 1, 1, true, false);
        doNothing().when(sessionRepo).addMultipleChoiceSelection(1, 1, 2, true, false);
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 3, false, true);
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 4, false, true);
        when(sessionRepo.getMultipleChoiceAnswerCount(1, 1)).thenReturn(4);
        when(multipleChoiceSelectionRepo.findMultipleChoiceSelections(1, 1)).thenReturn(Arrays.asList(
                new MultipleChoiceSelection(1, 1, 1, 1, true, false),
                new MultipleChoiceSelection(2, 1, 1, 2, true, false),
                new MultipleChoiceSelection(3, 1, 1, 3, false, true),
                new MultipleChoiceSelection(4, 1, 1, 4, false, true)));
        mvc.perform(put("/session/1/question/1/selection").accept(MediaType.APPLICATION_JSON).content(multipleSelection).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].localAnswerId").value(1))
                .andExpect(jsonPath("$.[0].isChecked").value(true))
                .andExpect(jsonPath("$.[0].isCrossed").value(false))
                .andExpect(jsonPath("$.[1].localAnswerId").value(2))
                .andExpect(jsonPath("$.[1].isChecked").value(true))
                .andExpect(jsonPath("$.[1].isCrossed").value(false))
                .andExpect(jsonPath("$.[2].localAnswerId").value(3))
                .andExpect(jsonPath("$.[2].isChecked").value(false))
                .andExpect(jsonPath("$.[2].isCrossed").value(true))
                .andExpect(jsonPath("$.[3].localAnswerId").value(4))
                .andExpect(jsonPath("$.[3].isChecked").value(false))
                .andExpect(jsonPath("$.[3].isCrossed").value(true));
        verify(sessionRepo, times(1)).addMultipleChoiceSelection(1, 1, 1, true, false);
        verify(sessionRepo, times(1)).addMultipleChoiceSelection(1, 1, 2, true, false);
        verify(sessionRepo, times(1)).addMultipleChoiceSelection(1, 1, 3, false, true);
        verify(sessionRepo, times(1)).addMultipleChoiceSelection(1, 1, 4, false, true);




    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void addSelectionCheckedAndCrossedSingle() throws Exception {
        String singleSelection = objectMapper.writeValueAsString(new SetSelectionRequest("single-choice", null, new Integer[]{2, 3}, 2));
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 1, true, false);
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 2, false, true);
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 3, false, true);
        when(sessionRepo.getSingleChoiceAnswerCount(1, 1)).thenReturn(3);
        when(singleChoiceSelectionRepo.findSingleChoiceSelections(1, 1)).thenReturn(Arrays.asList(
                new SingleChoiceSelection(1, 1, 1, 1, true, false),
                new SingleChoiceSelection(2, 1, 1, 2, false, true),
                new SingleChoiceSelection(3, 1, 1, 3, false, true)));
        mvc.perform(put("/session/1/question/1/selection").accept(MediaType.APPLICATION_JSON).content(singleSelection).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.msg").value("Eine Antwort kann nicht gleichzeitig gechecked und angekreuzt sein."));
        verify(sessionRepo, times(0)).addSingleChoiceSelection(1, 1, 1, true, false);
        verify(sessionRepo, times(0)).addSingleChoiceSelection(1, 1, 2, false, true);
        verify(sessionRepo, times(0)).addSingleChoiceSelection(1, 1, 3, false, true);

    }


    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void addSelectionCheckedAndCrossedMultiple() throws Exception {
        String multipleSelection = objectMapper.writeValueAsString(new SetSelectionRequest("multiple-choice", new Integer[]{1, 2}, new Integer[]{2, 3, 4}, null));
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        doNothing().when(sessionRepo).addMultipleChoiceSelection(1, 1, 1, true, false);
        doNothing().when(sessionRepo).addMultipleChoiceSelection(1, 1, 2, true, false);
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 3, false, true);
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 4, false, true);
        when(sessionRepo.getMultipleChoiceAnswerCount(1, 1)).thenReturn(4);
        when(multipleChoiceSelectionRepo.findMultipleChoiceSelections(1, 1)).thenReturn(Arrays.asList(
                new MultipleChoiceSelection(1, 1, 1, 1, true, false),
                new MultipleChoiceSelection(2, 1, 1, 2, true, false),
                new MultipleChoiceSelection(3, 1, 1, 3, false, true),
                new MultipleChoiceSelection(4, 1, 1, 4, false, true)));
        mvc.perform(put("/session/1/question/1/selection").accept(MediaType.APPLICATION_JSON).content(multipleSelection).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.msg").value("Eine Antwort kann nicht gleichzeitig gechecked und angekreuzt sein."));
        //JK: Gewollt oder nicht gewollt? Das ist hier die Frage.
        /*verify(sessionRepo, times(0)).addMultipleChoiceSelection(1, 1, 1, true, false);*/
        verify(sessionRepo, times(0)).addMultipleChoiceSelection(1, 1, 2, true, false);
        verify(sessionRepo, times(0)).addMultipleChoiceSelection(1, 1, 3, false, true);
        verify(sessionRepo, times(0)).addMultipleChoiceSelection(1, 1, 4, false, true);


    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 40)
    public void addSelectionSingleNotAllowed() throws Exception {
        String singleSelection = objectMapper.writeValueAsString(new SetSelectionRequest("SingleChoiceSelection", new Integer[]{1}, new Integer[]{2, 3}, 2));
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 1, true, false);
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 2, false, true);
        doNothing().when(sessionRepo).addSingleChoiceSelection(1, 1, 3, false, true);
        when(singleChoiceSelectionRepo.findSingleChoiceSelections(1, 1)).thenReturn(Arrays.asList(
                new SingleChoiceSelection(1, 1, 1, 1, true, false),
                new SingleChoiceSelection(2, 1, 1, 2, false, true),
                new SingleChoiceSelection(3, 1, 1, 3, false, true)));
        mvc.perform(put("/session/1/question/1/selection").accept(MediaType.APPLICATION_JSON).content(singleSelection).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.msg").value("Nur der Ersteller der Session oder ein Administrator darf eine Antwort hinzufügen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void addTime() throws Exception {
        String timeRequest = objectMapper.writeValueAsString(new SetTimeRequest(4));
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        doNothing().when(sessionRepo).addTime(1, 1, 4);
        when(sessionQuestionRepo.findBySessionIdAndLocalId(1, 1)).thenReturn(new SessionQuestion(1, 1, 1, 1, 4, true));

        mvc.perform(put("/session/1/question/1/time")
                .accept(MediaType.APPLICATION_JSON)
                .content(timeRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.questionId").value(1))
                .andExpect(jsonPath("$.localId").value(1))
                .andExpect(jsonPath("$.time").value(4))
                .andExpect(jsonPath("$.isSubmitted").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 405)
    public void addTimeNotAllowed() throws Exception {
        String timeRequest = objectMapper.writeValueAsString(new SetTimeRequest(4));
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        doNothing().when(sessionRepo).addTime(1, 1, 4);
        when(sessionQuestionRepo.findBySessionIdAndLocalId(1, 1)).thenReturn(new SessionQuestion(1, 1, 1, 1, 4, true));

        mvc.perform(put("/session/1/question/1/time")
                .accept(MediaType.APPLICATION_JSON)
                .content(timeRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur der Ersteller der Session oder ein Administrator darf Zeit zu einer Antwort hinzufügen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void submitQuestion() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        doNothing().when(sessionRepo).submitQuestion(1, 1);
        when(sessionQuestionRepo.findBySessionIdAndLocalId(1, 1)).thenReturn(new SessionQuestion(1, 1, 1, 1, 4, true));

        mvc.perform(patch("/session/1/question/1/submit"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.questionId").value(1))
                .andExpect(jsonPath("$.localId").value(1))
                .andExpect(jsonPath("$.time").value(4))
                .andExpect(jsonPath("$.isSubmitted").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 401)
    public void submitQuestionNotAllowed() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));

        mvc.perform(patch("/session/1/question/1/submit"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur der Session-Ersteller oder Administratoren dürfen eine Frage auf beantwortet setzen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void resetAnswers() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        // when(sessionRepo.resetSession(1)).thenReturn(new Session(1, 45, "notes", "exam", "name", false, false));
        mvc.perform(patch("/session/1/reset"))
                .andExpect(status().is(200));
        verify(sessionRepo, times(1)).resetSession(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 48)
    public void resetAnswersNotAllowed() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(java.util.Optional.of(new Session(1, 45, "notes", "exam", "name", false, false)));
        //when(sessionRepo.resetSession(1)).thenReturn(new Session(1,45, "notes", "exam", "name", false, false));
        mvc.perform(patch("/session/1/reset"))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.msg").value("Nur der Ersteller der Session oder ein Administrator darf die Ergebnisse der Session zurücksetzen."));
        verify(sessionRepo, times(0)).resetSession(1);
    }

}