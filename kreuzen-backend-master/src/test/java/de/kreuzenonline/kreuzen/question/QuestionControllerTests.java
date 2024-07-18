package de.kreuzenonline.kreuzen.question;

import de.kreuzenonline.kreuzen.auth.WithMockCustomUser;
import de.kreuzenonline.kreuzen.question.origin.QuestionOriginRepo;
import de.kreuzenonline.kreuzen.question.types.assignment.*;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceAnswer;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceAnswerRepo;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceQuestionEntry;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceQuestionRepo;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceAnswer;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceAnswerRepo;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceQuestionEntry;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceQuestionRepo;
import de.kreuzenonline.kreuzen.session.Session;
import de.kreuzenonline.kreuzen.session.SessionRepo;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@AutoConfigureMockMvc
public class QuestionControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BaseQuestionRepo baseQuestionRepo;
    @MockBean
    private SingleChoiceQuestionRepo singleChoiceQuestionRepo;
    @MockBean
    private MultipleChoiceQuestionRepo multipleChoiceQuestionRepo;
    @MockBean
    private AssignmentQuestionRepo assignmentQuestionRepo;
    @MockBean
    private SingleChoiceAnswerRepo singleChoiceAnswerRepo;
    @MockBean
    private MultipleChoiceAnswerRepo multipleChoiceAnswerRepo;
    @MockBean
    private AssignmentIdentifierRepo assignmentIdentifierRepo;
    @MockBean
    private AssignmentAnswerRepo assignmentAnswerRepo;
    @MockBean
    private QuestionOriginRepo questionOriginRepo;
    @MockBean
    private SessionRepo sessionRepo;

    @Test
    public void nothingVisibleForNonUsers() throws Exception {
        mvc.perform(get("/question/1")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(get("/question")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(get("/exam/1/question")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(get("/course/1/question")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(get("/question/toApprove")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(get("/question?limit=20&skip=40&searchTerm=test")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(get("/session/1/question")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
        mvc.perform(get("/session/1/question/1")).andExpect(status().isUnauthorized());
        mvc.perform(patch("/question/1")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Authentifizierung ist erforderlich."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getQuestionNotFoundMessages() throws Exception {
        mvc.perform(get("/question/1"))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.msg").value("Die gewünschte Frage konnte nicht gefunden werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getSingleChoiceQuestion() throws Exception {
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", true)));
        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 1));
        when(singleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new SingleChoiceAnswer(1, 1, 1, "Antwort1"),
                new SingleChoiceAnswer(2, 1, 2, "Antwort2"),
                new SingleChoiceAnswer(3, 1, 3, "Antwort3"))
        );
        mvc.perform(get("/question/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Testfrage"))
                .andExpect(jsonPath("$.type").value("single-choice"))
                .andExpect(jsonPath("$.points").value(2))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.origin").value("STUD"))
                .andExpect(jsonPath("$.correctAnswerLocalId").value(1))
                .andExpect(jsonPath("$.answers[0].id").value(1))
                .andExpect(jsonPath("$.answers[0].text").value("Antwort1"))
                .andExpect(jsonPath("$.answers[1].text").value("Antwort2"))
                .andExpect(jsonPath("$.answers[2].text").value("Antwort3"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getMultipleChoiceQuestion() throws Exception {
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "multiple-choice", "extra", 2, 1, 1, 45, 45, "STUD", true)));
        when(multipleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{2, 3}));
        when(multipleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new MultipleChoiceAnswer(1, 1, 1, "Antwort1"),
                new MultipleChoiceAnswer(2, 1, 2, "Antwort2"),
                new MultipleChoiceAnswer(3, 1, 3, "Antwort3"))
        );
        mvc.perform(get("/question/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Testfrage"))
                .andExpect(jsonPath("$.type").value("multiple-choice"))
                .andExpect(jsonPath("$.additionalInformation").value("extra"))
                .andExpect(jsonPath("$.points").value(2))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.updaterId").value(45))
                .andExpect(jsonPath("$.origin").value("STUD"))
                .andExpect(jsonPath("$.answers[0].id").value(1))
                .andExpect(jsonPath("$.answers[0].text").value("Antwort1"))
                .andExpect(jsonPath("$.answers[1].text").value("Antwort2"))
                .andExpect(jsonPath("$.answers[2].text").value("Antwort3"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.correctAnswerLocalIds[0]").value(2))
                .andExpect(jsonPath("$.correctAnswerLocalIds[1]").value(3));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getAssignmentQuestion() throws Exception {
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "assignment", null, 2, 1, 1, 45, 45, "STUD", true)));
        when(assignmentQuestionRepo.existsByQuestionId(1)).thenReturn(true);
        when(assignmentQuestionRepo.findByQuestionId(1)).thenReturn(new AssignmentQuestionEntry(1, 1));
        when(assignmentIdentifierRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(new AssignmentIdentifier(1, 1, 1, "A", 3),
                new AssignmentIdentifier(2, 1, 2, "B", 1),
                new AssignmentIdentifier(3, 1, 3, "C", 2)));
        when(assignmentAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(new AssignmentAnswer(1, 1, 1, "Zuordnung1"),
                new AssignmentAnswer(2, 1, 2, "Zuordnung2"),
                new AssignmentAnswer(3, 1, 3, "Zuordnung3")));
        mvc.perform(get("/question/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Testfrage"))
                .andExpect(jsonPath("$.type").value("assignment"))
                .andExpect(jsonPath("$.points").value(2))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.origin").value("STUD"))
                .andExpect(jsonPath("$.identifiers[0].identifier").value("A"))
                .andExpect(jsonPath("$.identifiers[0].localId").value(1))
                .andExpect(jsonPath("$.identifiers[0].correctAnswerLocalId").value(3))
                .andExpect(jsonPath("$.identifiers[1].identifier").value("B"))
                .andExpect(jsonPath("$.identifiers[1].localId").value(2))
                .andExpect(jsonPath("$.identifiers[1].correctAnswerLocalId").value(1))
                .andExpect(jsonPath("$.identifiers[2].identifier").value("C"))
                .andExpect(jsonPath("$.identifiers[2].localId").value(3))
                .andExpect(jsonPath("$.identifiers[2].correctAnswerLocalId").value(2))
                .andExpect(jsonPath("$.answers[0].answer").value("Zuordnung1"))
                .andExpect(jsonPath("$.answers[1].answer").value("Zuordnung2"))
                .andExpect(jsonPath("$.answers[2].localId").value(3))
                .andExpect(jsonPath("$.answers[2].answer").value("Zuordnung3"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 44)
    public void deleteQuestionNotAllowed() throws Exception {
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", false)));
        mvc.perform(delete("/question/1"))
                .andExpect(status().is(403));
        verify(baseQuestionRepo, times(0)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void deleteQuestionByCreator() throws Exception {
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", false)));
        mvc.perform(delete("/question/1"))
                .andExpect(status().is(204));
        verify(baseQuestionRepo, times(1)).deleteById(1);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createSingleChoice() throws Exception {

        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);
        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));

        when(singleChoiceQuestionRepo.save(Mockito.any(SingleChoiceQuestionEntry.class))).thenReturn(new SingleChoiceQuestionEntry(1, 1, 2));
        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 2));

        when(singleChoiceAnswerRepo.save(Mockito.any(SingleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        when(singleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new SingleChoiceAnswer(1, 1, 1, "Antwort1"),
                new SingleChoiceAnswer(2, 1, 2, "Antwort2"),
                new SingleChoiceAnswer(3, 1, 3, "Antwort3")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Testfrage"))
                .andExpect(jsonPath("$.type").value("single-choice"))
                .andExpect(jsonPath("$.points").value(2))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.origin").value("ORIG"))
                .andExpect(jsonPath("$.additionalInformation").value("Sonderzeichen gehen auch."))
                .andExpect(jsonPath("$.answers[0].text").value("Antwort1"))
                .andExpect(jsonPath("$.answers[1].text").value("Antwort2"))
                .andExpect(jsonPath("$.answers[2].text").value("Antwort3"))
                .andExpect(jsonPath("$.correctAnswerLocalId").value(1));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createMultipleChoice() throws Exception {

        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"multiple-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalIds\" : [1,2]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "multiple-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "multiple-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));

        when(multipleChoiceQuestionRepo.save(Mockito.any(MultipleChoiceQuestionEntry.class))).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{1, 2}));
        when(multipleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{1, 2}));

        when(multipleChoiceAnswerRepo.save(Mockito.any(MultipleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        when(multipleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new MultipleChoiceAnswer(1, 1, 1, "Antwort1"),
                new MultipleChoiceAnswer(2, 1, 2, "Antwort2"),
                new MultipleChoiceAnswer(3, 1, 3, "Antwort3")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Testfrage"))
                .andExpect(jsonPath("$.type").value("multiple-choice"))
                .andExpect(jsonPath("$.points").value(2))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.origin").value("ORIG"))
                .andExpect(jsonPath("$.additionalInformation").value("Sonderzeichen gehen auch."))
                .andExpect(jsonPath("$.answers[0].text").value("Antwort1"))
                .andExpect(jsonPath("$.answers[1].text").value("Antwort2"))
                .andExpect(jsonPath("$.answers[2].text").value("Antwort3"))
                .andExpect(jsonPath("$.correctAnswerLocalIds[0]").value(1))
                .andExpect(jsonPath("$.correctAnswerLocalIds[1]").value(2));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createAssignmentQuestion() throws Exception {
        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"assignment\",\r\n    \"additionalInformation\" : \"Zuordnung ist das halbe Leben\",\r\n    \"points\": 7,\r\n    \"courseId\" : 117,\r\n    \"origin\": \"ORIG\",\r\n    \"identifiers\" : [\"A\", \"B\", \"C\"],\r\n    \"answers\" : [\"Zuordnung1\", \"Zuordnung2\", \"Zuordnung3\"],\r\n    \"correctAssignmentIds\" : [3, 1, 2]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false)));

        when(assignmentQuestionRepo.existsByQuestionId(1)).thenReturn(true);
        when(assignmentQuestionRepo.save(Mockito.any(AssignmentQuestionEntry.class))).thenReturn(new AssignmentQuestionEntry(1, 1));
        when(assignmentQuestionRepo.findByQuestionId(1)).thenReturn(new AssignmentQuestionEntry(1, 1));

        when(assignmentIdentifierRepo.save(Mockito.any(AssignmentIdentifier.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentIdentifierRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new AssignmentIdentifier(1, 1, 1, "A", 3),
                new AssignmentIdentifier(2, 1, 2, "B", 1),
                new AssignmentIdentifier(3, 1, 3, "C", 2)
        ));
        when(assignmentAnswerRepo.save(Mockito.any(AssignmentAnswer.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new AssignmentAnswer(1, 1, 1, "Zuordnung1"),
                new AssignmentAnswer(2, 1, 2, "Zuordnung2"),
                new AssignmentAnswer(3, 1, 3, "Zuordnung3")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Testfrage"))
                .andExpect(jsonPath("$.type").value("assignment"))
                .andExpect(jsonPath("$.points").value(7))
                .andExpect(jsonPath("$.courseId").value(117))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.origin").value("ORIG"))
                .andExpect(jsonPath("$.additionalInformation").value("Zuordnung ist das halbe Leben"))
                .andExpect(jsonPath("$.identifiers[0].identifier").value("A"))
                .andExpect(jsonPath("$.identifiers[0].localId").value(1))
                .andExpect(jsonPath("$.identifiers[0].correctAnswerLocalId").value(3))
                .andExpect(jsonPath("$.identifiers[1].identifier").value("B"))
                .andExpect(jsonPath("$.identifiers[1].localId").value(2))
                .andExpect(jsonPath("$.identifiers[1].correctAnswerLocalId").value(1))
                .andExpect(jsonPath("$.identifiers[2].identifier").value("C"))
                .andExpect(jsonPath("$.identifiers[2].localId").value(3))
                .andExpect(jsonPath("$.identifiers[2].correctAnswerLocalId").value(2))
                .andExpect(jsonPath("$.answers[0].answer").value("Zuordnung1"))
                .andExpect(jsonPath("$.answers[1].answer").value("Zuordnung2"))
                .andExpect(jsonPath("$.answers[2].answer").value("Zuordnung3"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createBaseQuestionConflicts() throws Exception {
        String createBaseQuestionWithFalseType = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"many-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String createBaseQuestionWithTextTooShort = "{\r\n    \"text\": \"Test\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String createBaseQuestionWithAdditionalInformationTooLong = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String createBaseQuestionWithPointsTooHigh = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 11,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String createBaseQuestionWithPointsTooLow = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": -1,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String createBaseQuestionWithNoType = "{\r\n    \"text\": \"Testfrage\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String createBaseQuestionWithNoText = "{\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String createBaseQuestionWithTextTooLong = "{\r\n    \"text\": \"11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String createBaseQuestionWithNoCourseId = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String createBaseQuestionWithNoOrigin = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";

        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);
        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, null, 1, 45, 45, "ORIG", false));

        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(createBaseQuestionWithFalseType).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die eingegebene Frage entspricht keinem der definierten Typen."));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(createBaseQuestionWithTextTooShort).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die eingegebene Frage sollte mindestens 8 Zeichen haben."));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(createBaseQuestionWithAdditionalInformationTooLong).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die zusätzlichen Informationen sollten höchstens 1024 Zeichen haben."));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(createBaseQuestionWithPointsTooHigh).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die Frage sollte nicht mehr als 10 Punkte wert sein."));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(createBaseQuestionWithPointsTooLow).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die Frage sollte bei richtiger Lösung keine Minuspunkte bringen."));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(createBaseQuestionWithNoType).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Es muss ein Fragentyp für die Frage angegeben werden."));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(createBaseQuestionWithNoText).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Es muss ein Fragentext eingegeben werden."));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(createBaseQuestionWithTextTooLong).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die eingegebene Frage sollte höchstens 512 Zeichen haben."));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(createBaseQuestionWithNoCourseId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die Frage muss einem Kurs zugeordnet werden."));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(createBaseQuestionWithNoOrigin).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Es muss angegeben werden, woher die Frage stammt."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateBaseQuestionConflicts() throws Exception {
        String updateBaseQuestionWithTextTooShort = "{\r\n    \"text\": \"Test\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String updateBaseQuestionWithAdditionalInformationTooLong = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String updateBaseQuestionWithPointsTooHigh = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 11,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String updateBaseQuestionWithPointsTooLow = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": -1,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        String updateBaseQuestionWithTextTooLong = "{\r\n    \"text\": \"11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";

        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, null, 1, 45, 45, "ORIG", false)));
        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenAnswer(u -> u.getArgument(0));

        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 2));
        when(singleChoiceQuestionRepo.save(Mockito.any(SingleChoiceQuestionEntry.class))).thenAnswer(u -> u.getArgument(0));

        when(singleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new SingleChoiceAnswer(1, 1, 1, "Antwort19"),
                new SingleChoiceAnswer(2, 1, 2, "Antwort2"),
                new SingleChoiceAnswer(3, 1, 3, "Antwort3")
        ));

        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(updateBaseQuestionWithTextTooShort).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die eingegebene Frage sollte mindestens 8 Zeichen haben."));
        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(updateBaseQuestionWithAdditionalInformationTooLong).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die zusätzlichen Informationen sollten höchstens 1024 Zeichen haben."));
        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(updateBaseQuestionWithPointsTooHigh).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die Frage sollte nicht mehr als 10 Punkte wert sein."));
        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(updateBaseQuestionWithPointsTooLow).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die Frage sollte bei richtiger Lösung keine Minuspunkte bringen."));
        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(updateBaseQuestionWithTextTooLong).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die eingegebene Frage sollte höchstens 512 Zeichen haben."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createBaseQuestionConflictOriginNotValid() throws Exception {
        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 1,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(false);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 1, 1, 1, 45, 45, "test", false));

        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Dieser Fragenursprung ist nicht vorgesehen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createSingleChoiceConflictNotEnoughAnswers() throws Exception {
        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\" \r\n     ],\r\n    \"correctAnswerLocalId\" : 1\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));

        when(singleChoiceQuestionRepo.save(Mockito.any(SingleChoiceQuestionEntry.class))).thenReturn(new SingleChoiceQuestionEntry(1, 1, 2));
        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 2));

        when(singleChoiceAnswerRepo.save(Mockito.any(SingleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        when(singleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new SingleChoiceAnswer(1, 1, 1, "Antwort1")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Zu einer Single Choice-Frage müssen mindestens 2 Antwortmöglichkeiten angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createSingleChoiceConflictTooManyAnswers() throws Exception {
        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\", \r\n    \"Antwort4\", \n" +
                "        \"Antwort5\",\n" +
                "        \"Antwort6\",\n\"Antwort7\", \n" +
                "        \"Antwort8\",\n" +
                "        \"Antwort9\",\n\"Antwort10\", \n" +
                "        \"Antwort11\"], \"correctAnswerLocalId\" : 1\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));

        when(singleChoiceQuestionRepo.save(Mockito.any(SingleChoiceQuestionEntry.class))).thenReturn(new SingleChoiceQuestionEntry(1, 1, 2));
        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 2));

        when(singleChoiceAnswerRepo.save(Mockito.any(SingleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        when(singleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new SingleChoiceAnswer(1, 1, 1, "Antwort1"),
                new SingleChoiceAnswer(2, 1, 2, "Antwort2"),
                new SingleChoiceAnswer(3, 1, 3, "Antwort3"),
                new SingleChoiceAnswer(4, 1, 4, "Antwort4"),
                new SingleChoiceAnswer(5, 1, 5, "Antwort5"),
                new SingleChoiceAnswer(6, 1, 6, "Antwort6"),
                new SingleChoiceAnswer(7, 1, 7, "Antwort7"),
                new SingleChoiceAnswer(8, 1, 8, "Antwort8"),
                new SingleChoiceAnswer(9, 1, 9, "Antwort9"),
                new SingleChoiceAnswer(10, 1, 10, "Antwort10"),
                new SingleChoiceAnswer(11, 1, 11, "Antwort11")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Zu einer Single Choice-Frage sollten höchstens 10 Antwortmöglichkeiten angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createSingleChoiceConflictCorrectAnswerLocalIdCorrupt() throws Exception {

        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 0\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));

        when(singleChoiceQuestionRepo.save(Mockito.any(SingleChoiceQuestionEntry.class))).thenReturn(new SingleChoiceQuestionEntry(1, 1, 0));
        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 0));

        when(singleChoiceAnswerRepo.save(Mockito.any(SingleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        when(singleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new SingleChoiceAnswer(1, 1, 1, "Antwort1"),
                new SingleChoiceAnswer(2, 1, 2, "Antwort2"),
                new SingleChoiceAnswer(3, 1, 3, "Antwort3")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die Position der richtigen Antwort muss mindestens 1 sein."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createSingleChoiceConflictCorrectAnswerLocalIdTooHigh() throws Exception {

        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 23\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));

        when(singleChoiceQuestionRepo.save(Mockito.any(SingleChoiceQuestionEntry.class))).thenReturn(new SingleChoiceQuestionEntry(1, 1, 23));
        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 23));

        when(singleChoiceAnswerRepo.save(Mockito.any(SingleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        when(singleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new SingleChoiceAnswer(1, 1, 1, "Antwort1"),
                new SingleChoiceAnswer(2, 1, 2, "Antwort2"),
                new SingleChoiceAnswer(3, 1, 3, "Antwort3")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die Position der richtigen Antwort muss als Ganzzahl zwischen 1-n (wobei n die Gesamtzahl der Antwortmöglichkeiten ist) angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createMultipleChoiceConflictNotEnoughAnswers() throws Exception {

        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"multiple-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n            \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalIds\" : [1,2]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "multiple-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "multiple-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));

        when(multipleChoiceQuestionRepo.save(Mockito.any(MultipleChoiceQuestionEntry.class))).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{1, 2}));
        when(multipleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{1, 2}));

        when(multipleChoiceAnswerRepo.save(Mockito.any(MultipleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        when(multipleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new MultipleChoiceAnswer(1, 1, 1, "Antwort1"),
                new MultipleChoiceAnswer(2, 1, 2, "Antwort3")
        ));

        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Zu einer Multiple Choice-Frage müssen mindestens 3 Antwortmöglichkeiten angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createMultipleChoiceConflictCorrectAnswerLocalIdTooHigh() throws Exception {

        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"multiple-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\",\r\n\"Antwort4\", \r\n        \"Antwort5\",\r\n        \"Antwort6\",\"Antwort7\", \r\n        \"Antwort8\",\r\n        \"Antwort9\",\"Antwort10\", \r\n        \"Antwort11\",\r\n        \"Antwort12\"    ],\r\n    \"correctAnswerLocalIds\" : [1,2]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "multiple-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "multiple-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));

        when(multipleChoiceQuestionRepo.save(Mockito.any(MultipleChoiceQuestionEntry.class))).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{1, 2}));
        when(multipleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{1, 2}));

        when(multipleChoiceAnswerRepo.save(Mockito.any(MultipleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        when(multipleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new MultipleChoiceAnswer(1, 1, 1, "Antwort1"),
                new MultipleChoiceAnswer(2, 1, 2, "Antwort2"),
                new MultipleChoiceAnswer(3, 1, 3, "Antwort3"),
                new MultipleChoiceAnswer(4, 1, 4, "Antwort4"),
                new MultipleChoiceAnswer(5, 1, 5, "Antwort5"),
                new MultipleChoiceAnswer(6, 1, 6, "Antwort6"),
                new MultipleChoiceAnswer(7, 1, 7, "Antwort7"),
                new MultipleChoiceAnswer(8, 1, 8, "Antwort8"),
                new MultipleChoiceAnswer(9, 1, 9, "Antwort9"),
                new MultipleChoiceAnswer(10, 1, 10, "Antwort10"),
                new MultipleChoiceAnswer(11, 1, 11, "Antwort11"),
                new MultipleChoiceAnswer(12, 1, 12, "Antwort12")
        ));

        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Zu einer Multiple Choice-Frage sollten höchstens 10 Antwortmöglichkeiten angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createMultipleChoiceConflictTooManyCorrectAnswerLocalIds() throws Exception {

        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"multiple-choice\",\r\n    \"points\": 2,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalIds\" : [1,2,3,4]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "multiple-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "multiple-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));

        when(multipleChoiceQuestionRepo.save(Mockito.any(MultipleChoiceQuestionEntry.class))).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{1, 2, 3, 4}));
        when(multipleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{1, 2, 3, 4}));

        when(multipleChoiceAnswerRepo.save(Mockito.any(MultipleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        when(multipleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new MultipleChoiceAnswer(1, 1, 1, "Antwort1"),
                new MultipleChoiceAnswer(2, 1, 2, "Antwort2"),
                new MultipleChoiceAnswer(3, 1, 3, "Antwort3")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Es dürfen nicht mehr richtige Antwortmöglichkeiten als Antwortmöglichkeiten insgesamt angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createAssignmentQuestionConflictNotEnoughIdentifiers() throws Exception {
        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"assignment\",\r\n    \"additionalInformation\" : \"Zuordnung ist das halbe Leben\",\r\n    \"points\": 7,\r\n    \"courseId\" : 117,\r\n    \"origin\": \"ORIG\",\r\n    \"identifiers\" : [\"A\"],\r\n    \"answers\" : [\"Zuordnung1\", \"Zuordnung2\", \"Zuordnung3\"],\r\n    \"correctAssignmentIds\" : [3, 1, 2]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false)));

        when(assignmentQuestionRepo.save(Mockito.any(AssignmentQuestionEntry.class))).thenReturn(new AssignmentQuestionEntry(1, 1));
        when(assignmentQuestionRepo.findByQuestionId(1)).thenReturn(new AssignmentQuestionEntry(1, 1));

        when(assignmentIdentifierRepo.save(Mockito.any(AssignmentIdentifier.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentIdentifierRepo.findAllByQuestionId(1)).thenReturn(Collections.singletonList(
                new AssignmentIdentifier(1, 1, 1, "A", 3)
        ));
        when(assignmentAnswerRepo.save(Mockito.any(AssignmentAnswer.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new AssignmentAnswer(1, 1, 1, "Zuordnung1"),
                new AssignmentAnswer(2, 1, 2, "Zuordnung2"),
                new AssignmentAnswer(3, 1, 3, "Zuordnung3")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Zu einer Zuordnungsfrage müssen mindestens 2 Identifizierer angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createAssignmentQuestionConflictTooManyAnswers() throws Exception {
        String body = "{\r\n    \"text\": \"Testfrage2\",\r\n    \"type\": \"assignment\",\r\n    \"additionalInformation\" : \"Zuordnung ist das halbe Leben\",\r\n    \"points\": 7,\r\n    \"courseId\" : 117,\r\n    \"origin\": \"ORIG\",\r\n    \"identifiers\" : [\"A\", \"B\", \"C\"],\r\n    \"answers\" : [\"Zuordnung1\", \"Zuordnung2\", \"Zuordnung3\", \"Zuordnung4\", \"Zuordnung5\", \"Zuordnung6\", \"Zuordnung7\", \"Zuordnung8\", \"Zuordnung9\", \"Zuordnung10\", \"Zuordnung11\", \"Zuordnung12\", \"Zuordnung13\", \"Zuordnun14\", \"Zuordnung15\", \"Zuordnung16\", \"Zuordnung17\", \"Zuordnung18\", \"Zuordnung19\", \"Zuordnung20\", \"Zuordnung21\", \"Zuordnung22\", \"Zuordnung23\", \"Zuordnung24\", \"Zuordnung25\", \"Zuordnung26\", \"Zuordnung27\"],\r\n    \"correctAssignmentIds\" : [3, 1, 2]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false)));

        when(assignmentQuestionRepo.save(Mockito.any(AssignmentQuestionEntry.class))).thenReturn(new AssignmentQuestionEntry(1, 1));
        when(assignmentQuestionRepo.findByQuestionId(1)).thenReturn(new AssignmentQuestionEntry(1, 1));

        when(assignmentIdentifierRepo.save(Mockito.any(AssignmentIdentifier.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentIdentifierRepo.findAllByQuestionId(1)).thenReturn(Collections.singletonList(
                new AssignmentIdentifier(1, 1, 1, "A", 3)
        ));
        when(assignmentAnswerRepo.save(Mockito.any(AssignmentAnswer.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new AssignmentAnswer(1, 1, 1, "Zuordnung1"),
                new AssignmentAnswer(2, 1, 2, "Zuordnung2"),
                new AssignmentAnswer(3, 1, 3, "Zuordnung3")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Zu einer Zuordnungsfrage sollten höchstens 26 Antwortmöglichkeiten angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createAssignmentQuestionConflictMoreIdentifiersThanAnswers() throws Exception {
        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"assignment\",\r\n    \"additionalInformation\" : \"Zuordnung ist das halbe Leben\",\r\n    \"points\": 7,\r\n    \"courseId\" : 117,\r\n    \"origin\": \"ORIG\",\r\n    \"identifiers\" : [\"A\", \"B\", \"C\"],\r\n    \"answers\" : [\"Zuordnung1\", \"Zuordnung2\"],\r\n    \"correctAssignmentIds\" : [3, 1, 2]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false)));

        when(assignmentQuestionRepo.save(Mockito.any(AssignmentQuestionEntry.class))).thenReturn(new AssignmentQuestionEntry(1, 1));
        when(assignmentQuestionRepo.findByQuestionId(1)).thenReturn(new AssignmentQuestionEntry(1, 1));

        when(assignmentIdentifierRepo.save(Mockito.any(AssignmentIdentifier.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentIdentifierRepo.findAllByQuestionId(1)).thenReturn(Collections.singletonList(
                new AssignmentIdentifier(1, 1, 1, "A", 3)
        ));
        when(assignmentAnswerRepo.save(Mockito.any(AssignmentAnswer.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new AssignmentAnswer(1, 1, 1, "Zuordnung1"),
                new AssignmentAnswer(2, 1, 2, "Zuordnung2"),
                new AssignmentAnswer(3, 1, 3, "Zuordnung3")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Es darf nicht mehr Identifizierer als Antwortmöglichkeiten geben."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void createAssignmentQuestionConflictIdentifiersDoesNotMatchIds() throws Exception {
        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"assignment\",\r\n    \"additionalInformation\" : \"Zuordnung ist das halbe Leben\",\r\n    \"points\": 7,\r\n    \"courseId\" : 117,\r\n    \"origin\": \"ORIG\",\r\n    \"identifiers\" : [\"A\", \"B\", \"C\"],\r\n    \"answers\" : [\"Zuordnung1\", \"Zuordnung2\", \"Zuordnung3\"],\r\n    \"correctAssignmentIds\" : [3, 1]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false)));

        when(assignmentQuestionRepo.save(Mockito.any(AssignmentQuestionEntry.class))).thenReturn(new AssignmentQuestionEntry(1, 1));
        when(assignmentQuestionRepo.findByQuestionId(1)).thenReturn(new AssignmentQuestionEntry(1, 1));

        when(assignmentIdentifierRepo.save(Mockito.any(AssignmentIdentifier.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentIdentifierRepo.findAllByQuestionId(1)).thenReturn(Collections.singletonList(
                new AssignmentIdentifier(1, 1, 1, "A", 3)
        ));
        when(assignmentAnswerRepo.save(Mockito.any(AssignmentAnswer.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new AssignmentAnswer(1, 1, 1, "Zuordnung1"),
                new AssignmentAnswer(2, 1, 2, "Zuordnung2"),
                new AssignmentAnswer(3, 1, 3, "Zuordnung3")
        ));
        mvc.perform(post("/question/").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die Anzahl der Zuordnungen passt nicht zur Länge der Identifizierer."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateSingleChoice() throws Exception {

        String body = "{\r\n    \"text\": \"Update Test\",\r\n    \"type\": \"single-choice\",\r\n    \"points\": 0,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort19\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalId\" : 2\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));
        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenAnswer(u -> u.getArgument(0));

        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 2));
        when(singleChoiceQuestionRepo.save(Mockito.any(SingleChoiceQuestionEntry.class))).thenAnswer(u -> u.getArgument(0));

        when(singleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new SingleChoiceAnswer(1, 1, 1, "Antwort19"),
                new SingleChoiceAnswer(2, 1, 2, "Antwort2"),
                new SingleChoiceAnswer(3, 1, 3, "Antwort3")
        ));
        when(singleChoiceAnswerRepo.save(Mockito.any(SingleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Update Test"))
                .andExpect(jsonPath("$.type").value("single-choice"))
                .andExpect(jsonPath("$.points").value(0))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.origin").value("ORIG"))
                .andExpect(jsonPath("$.additionalInformation").value("Sonderzeichen gehen auch."))
                .andExpect(jsonPath("$.answers[0].text").value("Antwort19"))
                .andExpect(jsonPath("$.answers[1].text").value("Antwort2"))
                .andExpect(jsonPath("$.answers[2].text").value("Antwort3"))
                .andExpect(jsonPath("$.correctAnswerLocalId").value(2));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateSingleChoiceConflicts() throws Exception {
        String notEnoughAnswers = "{\r\n \"answers\" : [\r\n\"Antwort1\"]}";
        String tooManyAnswers = "{\"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\", \r\n    \"Antwort4\", \n" +
                "        \"Antwort5\",\n" +
                "        \"Antwort6\",\n\"Antwort7\", \n" +
                "        \"Antwort8\",\n" +
                "        \"Antwort9\",\n\"Antwort10\", \n" +
                "        \"Antwort11\"]}";
        String correctAnswerLocalIdCorrupt = "{ \"correctAnswerLocalId\" : 0}";
        String correctAnswerLocalIdTooHigh = "{\"correctAnswerLocalId\" : 23}";

        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));
        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenAnswer(u -> u.getArgument(0));

        when(singleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new SingleChoiceQuestionEntry(1, 1, 2));
        when(singleChoiceQuestionRepo.save(Mockito.any(SingleChoiceQuestionEntry.class))).thenAnswer(u -> u.getArgument(0));

        when(singleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new SingleChoiceAnswer(1, 1, 1, "Antwort19"),
                new SingleChoiceAnswer(2, 1, 2, "Antwort2"),
                new SingleChoiceAnswer(3, 1, 3, "Antwort3")
        ));

        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(notEnoughAnswers).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Zu einer Single Choice-Frage müssen mindestens 2 Antwortmöglichkeiten angegeben werden."));

        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(tooManyAnswers).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Zu einer Single Choice-Frage sollten höchstens 10 Antwortmöglichkeiten angegeben werden."));

        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(correctAnswerLocalIdCorrupt).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die Position der richtigen Antwort muss mindestens 1 sein."));

        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(correctAnswerLocalIdTooHigh).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Die Position der richtigen Antwort muss als Ganzzahl zwischen 1-n (wobei n die Gesamtzahl der Antwortmöglichkeiten ist) angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateMultipleChoice() throws Exception {

        String body = "{\r\n    \"text\": \"Fragetext\",\r\n    \"type\": \"multiple-choice\",\r\n    \"points\": 0,\r\n    \"courseId\": 1,\r\n    \"origin\": \"ORIG\",\r\n    \"additionalInformation\" : \"Sonderzeichen gehen auch.\",\r\n    \"answers\" : [\r\n        \"Antwort12\", \r\n        \"Antwort2\",\r\n        \"Antwort3\"\r\n    ],\r\n    \"correctAnswerLocalIds\" : [2,3]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "multiple-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));
        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenAnswer(u -> u.getArgument(0));

        when(multipleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{1, 2}));
        when(multipleChoiceQuestionRepo.save(Mockito.any(MultipleChoiceQuestionEntry.class))).thenAnswer(u -> u.getArgument(0));

        when(multipleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new MultipleChoiceAnswer(1, 1, 1, "Antwort12"),
                new MultipleChoiceAnswer(2, 1, 2, "Antwort2"),
                new MultipleChoiceAnswer(3, 1, 3, "Antwort3")
        ));
        when(multipleChoiceAnswerRepo.save(Mockito.any(MultipleChoiceAnswer.class))).thenAnswer(a -> a.getArgument(0));
        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Fragetext"))
                .andExpect(jsonPath("$.type").value("multiple-choice"))
                .andExpect(jsonPath("$.points").value(0))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.origin").value("ORIG"))
                .andExpect(jsonPath("$.additionalInformation").value("Sonderzeichen gehen auch."))
                .andExpect(jsonPath("$.answers[0].text").value("Antwort12"))
                .andExpect(jsonPath("$.answers[1].text").value("Antwort2"))
                .andExpect(jsonPath("$.answers[2].text").value("Antwort3"))
                .andExpect(jsonPath("$.correctAnswerLocalIds[0]").value(2))
                .andExpect(jsonPath("$.correctAnswerLocalIds[1]").value(3));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateMultipleChoiceConflicts() throws Exception {
        String notEnoughAnswers = "{\r\n \"answers\" : [\r\n\"Antwort1\"]}";
        String tooManyAnswers = "{\"answers\" : [\r\n        \"Antwort1\", \r\n        \"Antwort2\",\r\n        \"Antwort3\", \r\n    \"Antwort4\", \n" +
                "        \"Antwort5\",\n" +
                "        \"Antwort6\",\n\"Antwort7\", \n" +
                "        \"Antwort8\",\n" +
                "        \"Antwort9\",\n\"Antwort10\", \n" +
                "        \"Antwort11\"]}";
        String correctAnswerLocalIdsCorrupt = "{ \"correctAnswerLocalIds\" : [1,2,3,4]}";

        when(baseQuestionRepo.findById(1)).thenReturn(Optional.of(new BaseQuestion(1, "Testfrage", "multiple-choice", "Sonderzeichen gehen auch.", 2, 1, 1, 45, 45, "ORIG", false)));
        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenAnswer(u -> u.getArgument(0));

        when(multipleChoiceQuestionRepo.findByQuestionId(1)).thenReturn(new MultipleChoiceQuestionEntry(1, 1, new Integer[]{1, 2}));
        when(multipleChoiceQuestionRepo.save(Mockito.any(MultipleChoiceQuestionEntry.class))).thenAnswer(u -> u.getArgument(0));

        when(singleChoiceAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new SingleChoiceAnswer(1, 1, 1, "Antwort19"),
                new SingleChoiceAnswer(2, 1, 2, "Antwort2"),
                new SingleChoiceAnswer(3, 1, 3, "Antwort3")
        ));

        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(notEnoughAnswers).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Zu einer Multiple Choice-Frage müssen mindestens 3 Antwortmöglichkeiten angegeben werden."));

        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(tooManyAnswers).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Zu einer Multiple Choice-Frage sollten höchstens 10 Antwortmöglichkeiten angegeben werden."));

        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(correctAnswerLocalIdsCorrupt).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Es dürfen nicht mehr richtige Antwortmöglichkeiten als Antwortmöglichkeiten insgesamt angegeben werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateAssignmentQuestion() throws Exception {
        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"assignment\",\r\n    \"additionalInformation\" : \"Zuordnung ist das halbe Leben\",\r\n    \"points\": 7,\r\n    \"courseId\" : 117,\r\n    \"origin\": \"ORIG\",\r\n    \"identifiers\" : [\"1\", \"2\", \"3\"],\r\n    \"answers\" : [\"neueZuordnung1\", \"neueZuordnung2\", \"neueZuordnung3\"]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false)));

        when(assignmentQuestionRepo.existsByQuestionId(1)).thenReturn(true);
        when(assignmentQuestionRepo.save(Mockito.any(AssignmentQuestionEntry.class))).thenReturn(new AssignmentQuestionEntry(1, 1));
        when(assignmentQuestionRepo.findByQuestionId(1)).thenReturn(new AssignmentQuestionEntry(1, 1));

        when(assignmentIdentifierRepo.save(Mockito.any(AssignmentIdentifier.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentIdentifierRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new AssignmentIdentifier(1, 1, 1, "1", 3),
                new AssignmentIdentifier(2, 1, 2, "2", 1),
                new AssignmentIdentifier(3, 1, 3, "3", 2)
        ));
        when(assignmentAnswerRepo.save(Mockito.any(AssignmentAnswer.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new AssignmentAnswer(1, 1, 1, "neueZuordnung1"),
                new AssignmentAnswer(2, 1, 2, "neueZuordnung2"),
                new AssignmentAnswer(3, 1, 3, "neueZuordnung3")
        ));
        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Testfrage"))
                .andExpect(jsonPath("$.type").value("assignment"))
                .andExpect(jsonPath("$.points").value(7))
                .andExpect(jsonPath("$.courseId").value(117))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.origin").value("ORIG"))
                .andExpect(jsonPath("$.additionalInformation").value("Zuordnung ist das halbe Leben"))
                .andExpect(jsonPath("$.identifiers[0].identifier").value("1"))
                .andExpect(jsonPath("$.identifiers[0].localId").value(1))
                .andExpect(jsonPath("$.identifiers[0].correctAnswerLocalId").value(3))
                .andExpect(jsonPath("$.identifiers[1].identifier").value("2"))
                .andExpect(jsonPath("$.identifiers[1].localId").value(2))
                .andExpect(jsonPath("$.identifiers[1].correctAnswerLocalId").value(1))
                .andExpect(jsonPath("$.identifiers[2].identifier").value("3"))
                .andExpect(jsonPath("$.identifiers[2].localId").value(3))
                .andExpect(jsonPath("$.identifiers[2].correctAnswerLocalId").value(2))
                .andExpect(jsonPath("$.answers[0].answer").value("neueZuordnung1"))
                .andExpect(jsonPath("$.answers[1].answer").value("neueZuordnung2"))
                .andExpect(jsonPath("$.answers[2].answer").value("neueZuordnung3"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void updateAssignmentQuestionConflictTooManyUpdates() throws Exception {
        String body = "{\r\n    \"text\": \"Testfrage\",\r\n    \"type\": \"assignment\",\r\n    \"additionalInformation\" : \"Zuordnung ist das halbe Leben\",\r\n    \"points\": 7,\r\n    \"courseId\" : 117,\r\n    \"origin\": \"ORIG\",\r\n    \"identifiers\" : [\"1\", \"2\", \"3\"],\r\n    \"answers\" : [\"neueZuordnung1\", \"neueZuordnung2\", \"neueZuordnung3\"], \"correctAssignmentIds\" : [2, 3]\r\n}";
        when(questionOriginRepo.existsByName("ORIG")).thenReturn(true);

        when(baseQuestionRepo.save(Mockito.any(BaseQuestion.class))).thenReturn(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false));
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "assignment", "Zuordnung ist das halbe Leben", 7, 1, 117, 45, 45, "ORIG", false)));

        when(assignmentQuestionRepo.save(Mockito.any(AssignmentQuestionEntry.class))).thenReturn(new AssignmentQuestionEntry(1, 1));
        when(assignmentQuestionRepo.findByQuestionId(1)).thenReturn(new AssignmentQuestionEntry(1, 1));

        when(assignmentIdentifierRepo.save(Mockito.any(AssignmentIdentifier.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentIdentifierRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new AssignmentIdentifier(1, 1, 1, "A", 3),
                new AssignmentIdentifier(2, 1, 2, "B", 1),
                new AssignmentIdentifier(3, 1, 3, "C", 2)
        ));
        when(assignmentAnswerRepo.save(Mockito.any(AssignmentAnswer.class))).thenAnswer(i -> i.getArgument(0));
        when(assignmentAnswerRepo.findAllByQuestionId(1)).thenReturn(Arrays.asList(
                new AssignmentAnswer(1, 1, 1, "Zuordnung1"),
                new AssignmentAnswer(2, 1, 2, "Zuordnung2"),
                new AssignmentAnswer(3, 1, 3, "Zuordnung3")
        ));
        mvc.perform(patch("/question/1").accept(MediaType.APPLICATION_JSON).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("Bei so vielen Änderungen sollte eine neue Frage erstellt werden."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getQuestionsByCourse() throws Exception {

        when(baseQuestionRepo.findAllByCourseIdAndIsApprovedTrue(12)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 1, 12, 45, 45, "STUD", true),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", null, 3, 1, 12, 45, 45, "STUD", true),
                new BaseQuestion(3, "Testfrage3", "single-choice", null, 2, 1, 12, 45, 45, "IMPP", true)
        ));

        mvc.perform(get("/course/12/question").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].text").value("Testfrage1"))
                .andExpect(jsonPath("$.[0].type").value("single-choice"))
                .andExpect(jsonPath("$.[0].additionalInformation").value("Hier könnte ein Link stehen."))
                .andExpect(jsonPath("$.[0].points").value(2))
                .andExpect(jsonPath("$.[0].courseId").value(12))
                .andExpect(jsonPath("$.[0].creatorId").value(45))
                .andExpect(jsonPath("$.[0].origin").value("STUD"))

                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].text").value("Testfrage2"))
                .andExpect(jsonPath("$.[1].type").value("multiple-choice"))
                .andExpect(jsonPath("$.[1].points").value(3))
                .andExpect(jsonPath("$.[1].courseId").value(12))
                .andExpect(jsonPath("$.[1].creatorId").value(45))
                .andExpect(jsonPath("$.[1].origin").value("STUD"))

                .andExpect(jsonPath("$.[2].id").value(3))
                .andExpect(jsonPath("$.[2].text").value("Testfrage3"))
                .andExpect(jsonPath("$.[2].type").value("single-choice"))
                .andExpect(jsonPath("$.[2].points").value(2))
                .andExpect(jsonPath("$.[2].courseId").value(12))
                .andExpect(jsonPath("$.[2].creatorId").value(45))
                .andExpect(jsonPath("$.[2].origin").value("IMPP"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getQuestionsByExamUser() throws Exception {
        when(baseQuestionRepo.findAllByExamIdAndIsApprovedTrue(4)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 4, 12, 45, 45, "STUD", true),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", null, 3, 4, 12, 45, 45, "STUD", true),
                new BaseQuestion(3, "Testfrage3", "single-choice", null, 2, 4, 12, 45, 45, "IMPP", true)
        ));

        mvc.perform(get("/exam/4/question").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].text").value("Testfrage1"))
                .andExpect(jsonPath("$.[0].type").value("single-choice"))
                .andExpect(jsonPath("$.[0].additionalInformation").value("Hier könnte ein Link stehen."))
                .andExpect(jsonPath("$.[0].points").value(2))
                .andExpect(jsonPath("$.[0].courseId").value(12))
                .andExpect(jsonPath("$.[0].creatorId").value(45))
                .andExpect(jsonPath("$.[0].origin").value("STUD"))

                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].text").value("Testfrage2"))
                .andExpect(jsonPath("$.[1].type").value("multiple-choice"))
                .andExpect(jsonPath("$.[1].points").value(3))
                .andExpect(jsonPath("$.[1].courseId").value(12))
                .andExpect(jsonPath("$.[1].creatorId").value(45))
                .andExpect(jsonPath("$.[1].origin").value("STUD"))

                .andExpect(jsonPath("$.[2].id").value(3))
                .andExpect(jsonPath("$.[2].text").value("Testfrage3"))
                .andExpect(jsonPath("$.[2].type").value("single-choice"))
                .andExpect(jsonPath("$.[2].points").value(2))
                .andExpect(jsonPath("$.[2].courseId").value(12))
                .andExpect(jsonPath("$.[2].creatorId").value(45))
                .andExpect(jsonPath("$.[2].origin").value("IMPP"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void getQuestionsByExamAdmin() throws Exception {
        when(baseQuestionRepo.findAllByExamId(4)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 4, 12, 45, 45, "STUD", true),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", null, 3, 4, 12, 45, 45, "STUD", false),
                new BaseQuestion(3, "Testfrage3", "single-choice", null, 2, 4, 12, 45, 45, "IMPP", true)
        ));

        mvc.perform(get("/exam/4/question").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].text").value("Testfrage1"))
                .andExpect(jsonPath("$.[0].type").value("single-choice"))
                .andExpect(jsonPath("$.[0].additionalInformation").value("Hier könnte ein Link stehen."))
                .andExpect(jsonPath("$.[0].points").value(2))
                .andExpect(jsonPath("$.[0].courseId").value(12))
                .andExpect(jsonPath("$.[0].creatorId").value(45))
                .andExpect(jsonPath("$.[0].origin").value("STUD"))

                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].text").value("Testfrage2"))
                .andExpect(jsonPath("$.[1].type").value("multiple-choice"))
                .andExpect(jsonPath("$.[1].points").value(3))
                .andExpect(jsonPath("$.[1].courseId").value(12))
                .andExpect(jsonPath("$.[1].creatorId").value(45))
                .andExpect(jsonPath("$.[1].origin").value("STUD"))

                .andExpect(jsonPath("$.[2].id").value(3))
                .andExpect(jsonPath("$.[2].text").value("Testfrage3"))
                .andExpect(jsonPath("$.[2].type").value("single-choice"))
                .andExpect(jsonPath("$.[2].points").value(2))
                .andExpect(jsonPath("$.[2].courseId").value(12))
                .andExpect(jsonPath("$.[2].creatorId").value(45))
                .andExpect(jsonPath("$.[2].origin").value("IMPP"));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 42, role = "ADMIN")
    public void approveQuestionForAdmin() throws Exception {
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", false)));
        when(baseQuestionRepo.save(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", true)))
                .thenReturn(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", true));
        mvc.perform(patch("/question/1/approve").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200));
        verify(baseQuestionRepo, times(1)).save(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "MOD")
    public void approveQuestionNotAfterOwnUpdate() throws Exception {
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", false)));
        when(baseQuestionRepo.save(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", true)))
                .thenReturn(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", true));
        mvc.perform(patch("/question/1/approve").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nach einer Bearbeitung der Frage sollten die Änderungen durch einen anderen Moderator/Administrator bestätigt werden."));
        verify(baseQuestionRepo, times(0)).save(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void approveQuestionNotForUser() throws Exception {
        when(baseQuestionRepo.findById(1)).thenReturn(java.util.Optional.of(new BaseQuestion(1, "Testfrage", "single-choice", null, 2, 1, 1, 45, 45, "STUD", false)));
        mvc.perform(patch("/question/1/approve").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur Moderatoren und Administratoren können eine Frage freigeben."));
        verify(baseQuestionRepo, times(0)).save(any());
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void listUnapprovedQuestionsAsUser() throws Exception {
        when(baseQuestionRepo.findBySearchTerm(null, null, null, null, null, null, false, 20, 0)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 1, 12, 45, 45, "STUD", false),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", null, 3, 1, 12, 45, 45, "STUD", false),
                new BaseQuestion(3, "Testfrage3", "single-choice", null, 2, 1, 12, 45, 45, "IMPP", false)
        ));
        mvc.perform(get("/question?onlyApproved=false").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void listUnapprovedQuestionsAsAdmin() throws Exception {
        when(baseQuestionRepo.findBySearchTerm(null, null, null, null, null, null, null, 20, 0)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 1, 12, 45, 45, "STUD", false),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", null, 3, 1, 12, 45, 45, "STUD", false),
                new BaseQuestion(3, "Testfrage3", "single-choice", null, 2, 1, 12, 45, 45, "IMPP", false)
        ));
        when(baseQuestionRepo.countBySearchTerm(null, null, null, null, null, null, null)).thenReturn(3);
        mvc.perform(get("/question").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.count").value(3))

                .andExpect(jsonPath("$.entities.[0].id").value(1))
                .andExpect(jsonPath("$.entities.[0].text").value("Testfrage1"))
                .andExpect(jsonPath("$.entities.[0].type").value("single-choice"))
                .andExpect(jsonPath("$.entities.[0].additionalInformation").value("Hier könnte ein Link stehen."))
                .andExpect(jsonPath("$.entities.[0].points").value(2))
                .andExpect(jsonPath("$.entities.[0].courseId").value(12))
                .andExpect(jsonPath("$.entities.[0].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[0].origin").value("STUD"))
                .andExpect(jsonPath("$.entities.[0].isApproved").value(false))

                .andExpect(jsonPath("$.entities.[1].id").value(2))
                .andExpect(jsonPath("$.entities.[1].text").value("Testfrage2"))
                .andExpect(jsonPath("$.entities.[1].type").value("multiple-choice"))
                .andExpect(jsonPath("$.entities.[1].points").value(3))
                .andExpect(jsonPath("$.entities.[1].courseId").value(12))
                .andExpect(jsonPath("$.entities.[1].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[1].origin").value("STUD"))
                .andExpect(jsonPath("$.entities.[1].isApproved").value(false))

                .andExpect(jsonPath("$.entities.[2].id").value(3))
                .andExpect(jsonPath("$.entities.[2].text").value("Testfrage3"))
                .andExpect(jsonPath("$.entities.[2].type").value("single-choice"))
                .andExpect(jsonPath("$.entities.[2].points").value(2))
                .andExpect(jsonPath("$.entities.[2].courseId").value(12))
                .andExpect(jsonPath("$.entities.[2].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[2].origin").value("IMPP"))
                .andExpect(jsonPath("$.entities.[2].isApproved").value(false));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45, role = "ADMIN")
    public void searchQuestionsAsAdmin() throws Exception {
        when(baseQuestionRepo.findBySearchTerm("test:*", null, null, null, null, null, null, 20, 40)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 1, 12, 45, 45, "STUD", false),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", null, 3, 1, 12, 45, 45, "STUD", false),
                new BaseQuestion(3, "Testfrage3", "single-choice", null, 2, 1, 12, 45, 45, "IMPP", false)
        ));
        when(baseQuestionRepo.countBySearchTerm("test:*", null, null, null, null, null, null)).thenReturn(1234);

        mvc.perform(get("/question?limit=20&skip=40&searchTerm=test"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.count").value(1234))
                .andExpect(jsonPath("$.entities.[0].id").value(1))
                .andExpect(jsonPath("$.entities.[0].text").value("Testfrage1"))
                .andExpect(jsonPath("$.entities.[0].type").value("single-choice"))
                .andExpect(jsonPath("$.entities.[0].points").value(2))
                .andExpect(jsonPath("$.entities.[0].courseId").value(12))
                .andExpect(jsonPath("$.entities.[0].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[0].origin").value("STUD"))
                .andExpect(jsonPath("$.entities.[0].isApproved").value(false))
                .andExpect(jsonPath("$.entities.[1].id").value(2))
                .andExpect(jsonPath("$.entities.[1].text").value("Testfrage2"))
                .andExpect(jsonPath("$.entities.[1].type").value("multiple-choice"))
                .andExpect(jsonPath("$.entities.[1].points").value(3))
                .andExpect(jsonPath("$.entities.[1].courseId").value(12))
                .andExpect(jsonPath("$.entities.[1].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[1].origin").value("STUD"))
                .andExpect(jsonPath("$.entities.[1].isApproved").value(false))
                .andExpect(jsonPath("$.entities.[2].id").value(3))
                .andExpect(jsonPath("$.entities.[2].text").value("Testfrage3"))
                .andExpect(jsonPath("$.entities.[2].type").value("single-choice"))
                .andExpect(jsonPath("$.entities.[2].points").value(2))
                .andExpect(jsonPath("$.entities.[2].courseId").value(12))
                .andExpect(jsonPath("$.entities.[2].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[2].origin").value("IMPP"))
                .andExpect(jsonPath("$.entities.[2].isApproved").value(false));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void searchQuestionsAsUser() throws Exception {
        when(baseQuestionRepo.findBySearchTerm("test:*", null, null, null, null, null, true, 20, 40)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 1, 12, 45, 45, "STUD", true),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", null, 3, 1, 12, 45, 45, "STUD", true),
                new BaseQuestion(3, "Testfrage3", "single-choice", null, 2, 1, 12, 45, 45, "IMPP", true)
        ));
        when(baseQuestionRepo.countBySearchTerm("test:*", null, null, null, null, null, true)).thenReturn(1234);

        mvc.perform(get("/question?limit=20&skip=40&searchTerm=test"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.count").value(1234))
                .andExpect(jsonPath("$.entities.[0].id").value(1))
                .andExpect(jsonPath("$.entities.[0].text").value("Testfrage1"))
                .andExpect(jsonPath("$.entities.[0].type").value("single-choice"))
                .andExpect(jsonPath("$.entities.[0].points").value(2))
                .andExpect(jsonPath("$.entities.[0].courseId").value(12))
                .andExpect(jsonPath("$.entities.[0].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[0].origin").value("STUD"))
                .andExpect(jsonPath("$.entities.[0].isApproved").value(true))
                .andExpect(jsonPath("$.entities.[1].id").value(2))
                .andExpect(jsonPath("$.entities.[1].text").value("Testfrage2"))
                .andExpect(jsonPath("$.entities.[1].type").value("multiple-choice"))
                .andExpect(jsonPath("$.entities.[1].points").value(3))
                .andExpect(jsonPath("$.entities.[1].courseId").value(12))
                .andExpect(jsonPath("$.entities.[1].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[1].origin").value("STUD"))
                .andExpect(jsonPath("$.entities.[1].isApproved").value(true))
                .andExpect(jsonPath("$.entities.[2].id").value(3))
                .andExpect(jsonPath("$.entities.[2].text").value("Testfrage3"))
                .andExpect(jsonPath("$.entities.[2].type").value("single-choice"))
                .andExpect(jsonPath("$.entities.[2].points").value(2))
                .andExpect(jsonPath("$.entities.[2].courseId").value(12))
                .andExpect(jsonPath("$.entities.[2].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[2].origin").value("IMPP"))
                .andExpect(jsonPath("$.entities.[2].isApproved").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void searchQuestionsAsUserWithoutTerm() throws Exception {
        when(baseQuestionRepo.findBySearchTerm(null, null, null, null, null, null, true, 20, 40)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 1, 12, 45, 45, "STUD", true),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", null, 3, 1, 12, 45, 45, "STUD", true),
                new BaseQuestion(3, "Testfrage3", "single-choice", null, 2, 1, 12, 45, 45, "IMPP", true)
        ));
        when(baseQuestionRepo.countBySearchTerm(null, null, null, null, null, null, true)).thenReturn(1234);

        mvc.perform(get("/question?limit=20&skip=40"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.count").value(1234))
                .andExpect(jsonPath("$.entities.[0].id").value(1))
                .andExpect(jsonPath("$.entities.[0].text").value("Testfrage1"))
                .andExpect(jsonPath("$.entities.[0].type").value("single-choice"))
                .andExpect(jsonPath("$.entities.[0].points").value(2))
                .andExpect(jsonPath("$.entities.[0].courseId").value(12))
                .andExpect(jsonPath("$.entities.[0].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[0].origin").value("STUD"))
                .andExpect(jsonPath("$.entities.[0].isApproved").value(true))
                .andExpect(jsonPath("$.entities.[1].id").value(2))
                .andExpect(jsonPath("$.entities.[1].text").value("Testfrage2"))
                .andExpect(jsonPath("$.entities.[1].type").value("multiple-choice"))
                .andExpect(jsonPath("$.entities.[1].points").value(3))
                .andExpect(jsonPath("$.entities.[1].courseId").value(12))
                .andExpect(jsonPath("$.entities.[1].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[1].origin").value("STUD"))
                .andExpect(jsonPath("$.entities.[1].isApproved").value(true))
                .andExpect(jsonPath("$.entities.[2].id").value(3))
                .andExpect(jsonPath("$.entities.[2].text").value("Testfrage3"))
                .andExpect(jsonPath("$.entities.[2].type").value("single-choice"))
                .andExpect(jsonPath("$.entities.[2].points").value(2))
                .andExpect(jsonPath("$.entities.[2].courseId").value(12))
                .andExpect(jsonPath("$.entities.[2].creatorId").value(45))
                .andExpect(jsonPath("$.entities.[2].origin").value("IMPP"))
                .andExpect(jsonPath("$.entities.[2].isApproved").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void addQuestionToSessionSuccessful() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(Optional.of(new Session(1,45,null, "exam", "testSession", false, false)));
        when(baseQuestionRepo.findById(12)).thenReturn(Optional.of(new BaseQuestion(12, "Question in a session", "single-choice", null, 1, 1, 23, 45, 45, "STUD", true)));
        doNothing().when(baseQuestionRepo).addQuestionToSession(1, 12);

        mvc.perform(put("/session/1/question/12"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.text").value("Question in a session"))
                .andExpect(jsonPath("$.type").value("single-choice"))
                .andExpect(jsonPath("$.points").value(1))
                .andExpect(jsonPath("$.courseId").value(23))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.origin").value("STUD"))
                .andExpect(jsonPath("$.isApproved").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 451)
    public void addQuestionToSessionNotAllowed() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(Optional.of(new Session(1,45,null, "exam", "testSession", false, false)));
        when(baseQuestionRepo.findById(12)).thenReturn(Optional.of(new BaseQuestion(1, "Question in a session", "single-choice", null, 1, 1, 23, 45, 45, "STUD", true)));
        doNothing().when(baseQuestionRepo).addQuestionToSession(1, 12);

        mvc.perform(put("/session/1/question/12"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur der Session-Ersteller sowie Administratoren dürfen Fragen zu einer Session hinzufügen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void removeQuestionFromSessionSuccessful() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(Optional.of(new Session(1,45,null, "exam", "testSession", false, false)));
        doNothing().when(baseQuestionRepo).removeQuestionFromSession(1, 12);
        mvc.perform(delete("/session/1/question/12").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
        verify(baseQuestionRepo, times(1)).removeQuestionFromSession(1, 12);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 451)
    public void removeQuestionFromSessionNotAllowed() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(Optional.of(new Session(1,45,null, "exam", "testSession", false, false)));
        doNothing().when(baseQuestionRepo).removeQuestionFromSession(1, 12);
        mvc.perform(delete("/session/1/question/12").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur der Session-Ersteller sowie Administratoren dürfen Fragen aus einer Session entfernen."));
        verify(baseQuestionRepo, times(0)).removeQuestionFromSession(1, 12);
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getQuestionsBySession() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(Optional.of(new Session(1,45,null, "exam", "testSession", false, false)));
        when(baseQuestionRepo.findAllBySession(1)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 1, 12, 45, 45, "STUD", true),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", null, 3, 1, 12, 45, 45, "STUD", true),
                new BaseQuestion(3, "Testfrage3", "single-choice", null, 2, 1, 12, 45, 45, "IMPP", true)));
        mvc.perform(get("/session/1/question").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].text").value("Testfrage1"))
                .andExpect(jsonPath("$.[0].type").value("single-choice"))
                .andExpect(jsonPath("$.[0].points").value(2))
                .andExpect(jsonPath("$.[0].courseId").value(12))
                .andExpect(jsonPath("$.[0].creatorId").value(45))
                .andExpect(jsonPath("$.[0].origin").value("STUD"))
                .andExpect(jsonPath("$.[0].isApproved").value(true))
                .andExpect(jsonPath("$.[1].id").value(2))
                .andExpect(jsonPath("$.[1].text").value("Testfrage2"))
                .andExpect(jsonPath("$.[1].type").value("multiple-choice"))
                .andExpect(jsonPath("$.[1].points").value(3))
                .andExpect(jsonPath("$.[1].courseId").value(12))
                .andExpect(jsonPath("$.[1].creatorId").value(45))
                .andExpect(jsonPath("$.[1].origin").value("STUD"))
                .andExpect(jsonPath("$.[1].isApproved").value(true))
                .andExpect(jsonPath("$.[2].id").value(3))
                .andExpect(jsonPath("$.[2].text").value("Testfrage3"))
                .andExpect(jsonPath("$.[2].type").value("single-choice"))
                .andExpect(jsonPath("$.[2].points").value(2))
                .andExpect(jsonPath("$.[2].courseId").value(12))
                .andExpect(jsonPath("$.[2].creatorId").value(45))
                .andExpect(jsonPath("$.[2].origin").value("IMPP"))
                .andExpect(jsonPath("$.[2].isApproved").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 110)
    public void getQuestionsBySessionNotAllowed() throws Exception {
        when(sessionRepo.findById(1)).thenReturn(Optional.of(new Session(1,45,null, "exam", "testSession", false, false)));
        when(baseQuestionRepo.findAllBySession(1)).thenReturn(Arrays.asList(
                new BaseQuestion(1, "Testfrage1", "single-choice", "Hier könnte ein Link stehen.", 2, 1, 12, 45, 45, "STUD", true),
                new BaseQuestion(2, "Testfrage2", "multiple-choice", null, 3, 1, 12, 45, 45, "STUD", true),
                new BaseQuestion(3, "Testfrage3", "single-choice", null, 2, 1, 12, 45, 45, "IMPP", true)));

        mvc.perform(get("/session/1/question").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur der Session-Ersteller sowie Administratoren dürfen die Fragen einer Session ansehen."));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 45)
    public void getQuestionFromASession() throws Exception {
        when(baseQuestionRepo.findBySessionAndLocalId(1, 1)).thenReturn(new BaseQuestion(12, "Question in a session", "single-choice", null, 1, 1, 23, 45, 45, "STUD", true));
        when(singleChoiceQuestionRepo.findByQuestionId(12)).thenReturn(new SingleChoiceQuestionEntry(1,1,1));
        when(baseQuestionRepo.findById(12)).thenReturn(Optional.of(new BaseQuestion(12, "Question in a session", "single-choice", null, 1, 1, 23, 45, 45, "STUD", true)));
        when(sessionRepo.findById(1)).thenReturn(Optional.of(new Session(1,45,null, "exam", "testSession", false, false)));

        mvc.perform(get("/session/1/question/1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.text").value("Question in a session"))
                .andExpect(jsonPath("$.type").value("single-choice"))
                .andExpect(jsonPath("$.points").value(1))
                .andExpect(jsonPath("$.courseId").value(23))
                .andExpect(jsonPath("$.creatorId").value(45))
                .andExpect(jsonPath("$.origin").value("STUD"))
                .andExpect(jsonPath("$.isApproved").value(true));
    }

    @Test
    @WithMockCustomUser(username = "test", email = "test@uni.de", id = 87)
    public void getQuestionFromASessionNotAllowed() throws Exception {
        when(baseQuestionRepo.findBySessionAndLocalId(1, 1)).thenReturn(new BaseQuestion(12, "Question in a session", "single-choice", null, 1, 1, 23, 45, 45, "STUD", true));
        when(sessionRepo.findById(1)).thenReturn(Optional.of(new Session(1,45,null, "exam", "testSession", false, false)));

        mvc.perform(get("/session/1/question/1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.msg").value("Nur der Session-Ersteller sowie Administratoren dürfen die Fragen einer Session ansehen."));
    }

}
