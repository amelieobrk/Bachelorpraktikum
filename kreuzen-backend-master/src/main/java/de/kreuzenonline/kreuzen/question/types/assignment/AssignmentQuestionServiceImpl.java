package de.kreuzenonline.kreuzen.question.types.assignment;

import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.BaseQuestionRepo;
import de.kreuzenonline.kreuzen.question.types.QuestionTypeService;
import de.kreuzenonline.kreuzen.question.types.assignment.requests.CreateAssignmentQuestionRequest;
import de.kreuzenonline.kreuzen.question.types.assignment.requests.UpdateAssignmentQuestionRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Service("assignmentQuestion")
public class AssignmentQuestionServiceImpl extends QuestionTypeService<AssignmentQuestion, CreateAssignmentQuestionRequest, UpdateAssignmentQuestionRequest> implements AssignmentQuestionService {

    public static final String TYPE = "assignment";

    private final AssignmentQuestionRepo assignmentQuestionRepo;
    private final AssignmentIdentifierRepo assignmentIdentifierRepo;
    private final AssignmentAnswerRepo assignmentAnswerRepo;
    private final BaseQuestionRepo baseQuestionRepo;
    private final ResourceBundle resourceBundle;

    public AssignmentQuestionServiceImpl(AssignmentQuestionRepo assignmentQuestionRepo, AssignmentIdentifierRepo assignmentIdentifierRepo, AssignmentAnswerRepo assignmentAnswerRepo, BaseQuestionRepo baseQuestionRepo, ResourceBundle resourceBundle) {
        super(CreateAssignmentQuestionRequest.class,
                UpdateAssignmentQuestionRequest.class, resourceBundle);
        this.assignmentQuestionRepo = assignmentQuestionRepo;
        this.assignmentIdentifierRepo = assignmentIdentifierRepo;
        this.assignmentAnswerRepo = assignmentAnswerRepo;
        this.baseQuestionRepo = baseQuestionRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public AssignmentQuestion getByQuestionId(Integer questionId) {
        BaseQuestion baseQuestion = baseQuestionRepo.findById(questionId).orElseThrow(() -> new NotFoundException(resourceBundle.getString("question-not-found")));
        if (!assignmentQuestionRepo.existsByQuestionId(questionId)) {
            throw new NotFoundException(resourceBundle.getString("assignment-question-not-found"));
        }
        AssignmentQuestionEntry questionEntry = assignmentQuestionRepo.findByQuestionId(questionId);
        return new AssignmentQuestion(
                baseQuestion.getId(),
                baseQuestion.getText(),
                baseQuestion.getType(),
                baseQuestion.getAdditionalInformation(),
                baseQuestion.getPoints(),
                baseQuestion.getExamId(),
                baseQuestion.getCourseId(),
                baseQuestion.getCreatorId(),
                baseQuestion.getUpdaterId(),
                baseQuestion.getOrigin(),
                baseQuestion.getIsApproved(),
                assignmentIdentifierRepo.findAllByQuestionId(questionId),
                assignmentAnswerRepo.findAllByQuestionId(questionId)
        );
    }

    @Override
    public AssignmentQuestion create(CreateAssignmentQuestionRequest request, Integer questionId) {
        AssignmentQuestionEntry assignmentQuestionEntry = new AssignmentQuestionEntry();

        /*
         * Restrictions for the creation of assignment questions:
         * both identifiers and answers can't be empty.
         * the amounts of identifiers and answers have to be equal and is limited to 2-26.
         * there shouldn't be more identifiers than answers.
         * the amount of identifiers has to match with the amount of correct assignment ids.
         */
        if (request.getIdentifiers() == null) {
            throw new ConflictException(resourceBundle.getString("assignment-question-identifiers-empty"));
        }
        if (request.getIdentifiers().length < 2) {
            throw new ConflictException(resourceBundle.getString("assignment-question-identifiers-not-enough"));
        }
        if (request.getIdentifiers().length > 26) {
            throw new ConflictException(resourceBundle.getString("assignment-question-identifiers-too-many"));
        }
        if (request.getAnswers() == null) {
            throw new ConflictException(resourceBundle.getString("assignment-question-answers-empty"));
        }
        if (request.getAnswers().length < 2) {
            throw new ConflictException(resourceBundle.getString("assignment-question-answers-not-enough"));
        }
        if (request.getAnswers().length > 26) {
            throw new ConflictException(resourceBundle.getString("assignment-question-answers-too-many"));
        }
        if (request.getIdentifiers().length > request.getAnswers().length) {
            throw new ConflictException(resourceBundle.getString("assignment-question-more-identifiers-than-answers"));
        }
        if (request.getCorrectAssignmentIds() == null) {
            throw new ConflictException(resourceBundle.getString("assignment-question-correct-assignment-ids-null"));
        }
        if (request.getIdentifiers().length != request.getCorrectAssignmentIds().length) {
            throw new ConflictException(resourceBundle.getString("assignment-question-correct-assignment-ids-corrupt"));
        }

        /*
         * local ids are necessary to save the order of identifiers and answers to the database.
         * Therefore the following integers are initialized with 0 and later incremented by 1 with every option.
         */
        int identifierLocalId = 0;
        Integer answerLocalId = 0;

        List<AssignmentIdentifier> identifiers = new ArrayList<>();
        List<AssignmentAnswer> answers = new ArrayList<>();

        for (String identifier : request.getIdentifiers()) {
            identifierLocalId++;
            AssignmentIdentifier assignmentIdentifier = new AssignmentIdentifier(null, questionId, identifierLocalId, identifier, request.getCorrectAssignmentIds()[identifierLocalId - 1]);
            identifiers.add(assignmentIdentifierRepo.save(assignmentIdentifier));
        }

        for (String answer : request.getAnswers()) {
            answerLocalId++;
            AssignmentAnswer assignmentAnswer = new AssignmentAnswer(null, questionId, answerLocalId, answer);
            answers.add(assignmentAnswerRepo.save(assignmentAnswer));
        }

        assignmentQuestionEntry.setQuestionId(questionId);
        assignmentQuestionRepo.save(assignmentQuestionEntry);

        AssignmentQuestion enteredQuestion = this.getByQuestionId(questionId);
        enteredQuestion.setIdentifiers(identifiers);
        enteredQuestion.setAnswers(answers);

        return enteredQuestion;
    }

    @Override
    public AssignmentQuestion update(UpdateAssignmentQuestionRequest request, Integer questionId) {
        AssignmentQuestionEntry entry = assignmentQuestionRepo.findByQuestionId(questionId);

        /*
         * When an assignment question shall be updated, it has to be ensured that the restrictions won't be violated.
         * Therefore the following conditions are checked (in contrast to the create-function it's allowed to leave parameters empty).
         * both identifiers and answers can't be empty.
         * the amounts of identifiers and answers have to be equal and is limited to 2-26.
         * there shouldn't be more identifiers than answers.
         * the amount of identifiers has to match with the amount of correct assignment ids.
         *
         * If identifiers and/or answers shall be updated, the existing identifiers/answers will be deleted from the database and the new values will be inserted.
         */
        if (request.getIdentifiers() != null && request.getAnswers() != null && request.getCorrectAssignmentIds() != null) {
            throw new ConflictException(resourceBundle.getString("assignment-question-update-too-many-arguments"));
        }
        if (request.getIdentifiers() != null && request.getIdentifiers().length != assignmentIdentifierRepo.findAllByQuestionId(questionId).size()) {
            throw new ConflictException(resourceBundle.getString("assignment-question-update-identifier-length-difference"));
        }
        if (request.getCorrectAssignmentIds() != null && request.getCorrectAssignmentIds().length != assignmentIdentifierRepo.findAllByQuestionId(questionId).size()) {
            throw new ConflictException(resourceBundle.getString("assignment-question-update-correct-identifiers-length-difference"));
        }

        if (request.getIdentifiers() != null) {
            if (request.getIdentifiers().length < 2) {
                throw new ConflictException(resourceBundle.getString("assignment-question-identifiers-not-enough"));
            }
            if (request.getIdentifiers().length > 20) {
                throw new ConflictException(resourceBundle.getString("assignment-question-identifiers-too-many"));
            }

        }
        if (request.getAnswers() != null) {
            if (request.getAnswers().length < 2) {
                throw new ConflictException(resourceBundle.getString("assignment-question-answers-not-enough"));
            }
            if (request.getAnswers().length > 26) {
                throw new ConflictException(resourceBundle.getString("assignment-question-answers-too-many"));
            }
        }

        if (request.getIdentifiers() != null && request.getAnswers() != null && request.getIdentifiers().length > request.getAnswers().length) {
            throw new ConflictException(resourceBundle.getString("assignment-question-more-identifiers-than-answers"));
        }
        if (request.getIdentifiers() != null && request.getCorrectAssignmentIds() != null && request.getIdentifiers().length != request.getCorrectAssignmentIds().length) {
            throw new ConflictException(resourceBundle.getString("assignment-question-correct-assignment-ids-corrupt"));
        }

        int identifierLocalId = 0;
        if (request.getIdentifiers() != null) {
            for (AssignmentIdentifier ident : assignmentIdentifierRepo.findAllByQuestionId(questionId)) {
                ident.setIdentifier(request.getIdentifiers()[identifierLocalId]);
                assignmentIdentifierRepo.save(ident);
                identifierLocalId++;
            }

        }

        if (request.getAnswers() != null) {
            for (AssignmentAnswer answer : assignmentAnswerRepo.findAllByQuestionId(questionId)) {
                assignmentAnswerRepo.delete(answer);
            }

            Integer answerLocalId = 0;
            for (String answer : request.getAnswers()) {
                answerLocalId++;
                AssignmentAnswer assignAnswer = new AssignmentAnswer(null, questionId, answerLocalId, answer);
                assignmentAnswerRepo.save(assignAnswer);
            }
        }

        if (request.getCorrectAssignmentIds() != null) {
            List<AssignmentIdentifier> existingIdentifiers = assignmentIdentifierRepo.findAllByQuestionId(questionId);
            int correctAnswerLocalId = 0;

            if (request.getCorrectAssignmentIds().length != existingIdentifiers.size()) {
                throw new ConflictException("assignment-question-correct-assignment-ids-corrupt");
            } else {
                for (AssignmentIdentifier a : existingIdentifiers) {
                    a.setCorrectAnswerLocalId(request.getCorrectAssignmentIds()[correctAnswerLocalId]);
                    correctAnswerLocalId++;
                    assignmentIdentifierRepo.save(a);
                }
            }
        }

        AssignmentQuestion enteredQuestion = this.getByQuestionId(questionId);
        enteredQuestion.setIdentifiers(assignmentIdentifierRepo.findAllByQuestionId(questionId));
        enteredQuestion.setAnswers(assignmentAnswerRepo.findAllByQuestionId(questionId));
        return enteredQuestion;

    }
}
