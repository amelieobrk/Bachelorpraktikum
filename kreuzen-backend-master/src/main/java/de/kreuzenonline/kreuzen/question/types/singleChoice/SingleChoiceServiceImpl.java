package de.kreuzenonline.kreuzen.question.types.singleChoice;

import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.BaseQuestionRepo;
import de.kreuzenonline.kreuzen.question.types.QuestionTypeService;
import de.kreuzenonline.kreuzen.question.types.singleChoice.requests.CreateSingleChoiceRequest;
import de.kreuzenonline.kreuzen.question.types.singleChoice.requests.UpdateSingleChoiceRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Service("singleChoice")
public class SingleChoiceServiceImpl extends QuestionTypeService<SingleChoiceQuestion, CreateSingleChoiceRequest, UpdateSingleChoiceRequest> implements SingleChoiceService {

    public static final String TYPE = "single-choice";

    private final SingleChoiceQuestionRepo singleChoiceQuestionRepo;
    private final SingleChoiceAnswerRepo singleChoiceAnswerRepo;
    private final BaseQuestionRepo baseQuestionRepo;
    private final ResourceBundle resourceBundle;

    public SingleChoiceServiceImpl(SingleChoiceQuestionRepo singleChoiceQuestionRepo, SingleChoiceAnswerRepo singleChoiceAnswerRepo, BaseQuestionRepo baseQuestionRepo, ResourceBundle resourceBundle) {
        super(CreateSingleChoiceRequest.class, UpdateSingleChoiceRequest.class, resourceBundle);
        this.singleChoiceQuestionRepo = singleChoiceQuestionRepo;
        this.singleChoiceAnswerRepo = singleChoiceAnswerRepo;
        this.baseQuestionRepo = baseQuestionRepo;
        this.resourceBundle = resourceBundle;
    }

    public SingleChoiceQuestion getByQuestionId(Integer questionId) {
        BaseQuestion baseQuestion = baseQuestionRepo.findById(questionId).orElseThrow(() -> new NotFoundException(resourceBundle.getString("question-not-found")));
        if (singleChoiceQuestionRepo.findByQuestionId(questionId) == null) {
            throw new NotFoundException(resourceBundle.getString("single-choice-question-not-found"));
        }
        SingleChoiceQuestionEntry questionEntry = singleChoiceQuestionRepo.findByQuestionId(questionId);
        return new SingleChoiceQuestion(
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
                questionEntry.getCorrectAnswerLocalId(),
                singleChoiceAnswerRepo.findAllByQuestionId(questionId)
        );
    }

    @Override
    public SingleChoiceQuestion create(CreateSingleChoiceRequest request, Integer questionId) {
        /*
         * Restrictions for the creation of single choice questions:
         * both answers and local id of the correct answer can't be empty.
         * the amount of answers should be in a range from 2 to 10.
         * the local id of the correct answer should neither be 0 nor higher than the total answers' count.
         */
        if (request.getAnswers() == null) {
            throw new ConflictException(resourceBundle.getString("CreateSingleChoiceRequest-answers-not-null"));
        }
        if (request.getAnswers().length < 2) {
            throw new ConflictException(resourceBundle.getString("CreateSingleChoiceRequest-answers-not-enough"));
        }
        if (request.getAnswers().length > 10) {
            throw new ConflictException(resourceBundle.getString("CreateSingleChoiceRequest-answers-too-many"));
        }
        if (request.getCorrectAnswerLocalId() == null) {
            throw new ConflictException(resourceBundle.getString("CreateSingleChoiceRequest-correct-answer-not-null"));
        }
        if (request.getCorrectAnswerLocalId() < 1) {
            throw new ConflictException(resourceBundle.getString("CreateSingleChoiceRequest-correct-answer-not-so-low"));
        }

        SingleChoiceQuestionEntry entry = new SingleChoiceQuestionEntry();

        if (request.getAnswers().length < request.getCorrectAnswerLocalId()) {
            throw new ConflictException(resourceBundle.getString("SingleChoiceQuestion-correct-answer-id-corrupt"));
        }

        entry.setQuestionId(questionId);
        entry.setCorrectAnswerLocalId(request.getCorrectAnswerLocalId());
        singleChoiceQuestionRepo.save(entry);

        /*
         * local id is necessary to save the order of the answers to the database.
         * Therefore the following integer is initialized with 0 and later incremented by 1 with every option.
         */
        Integer answerLocalId = 0;

        List<SingleChoiceAnswer> answers = new ArrayList<>();

        for (String answer : request.getAnswers()) {
            answerLocalId++;
            answers.add(singleChoiceAnswerRepo.save(new SingleChoiceAnswer(null, questionId, answerLocalId, answer)));
        }

        SingleChoiceQuestion enteredQuestion = this.getByQuestionId(questionId);
        enteredQuestion.setCorrectAnswerLocalId(request.getCorrectAnswerLocalId());
        enteredQuestion.setAnswers(answers);

        return enteredQuestion;
    }

    @Override
    public SingleChoiceQuestion update(UpdateSingleChoiceRequest request, Integer questionId) {
        SingleChoiceQuestionEntry scq = singleChoiceQuestionRepo.findByQuestionId(questionId);

        /*
         * When a single choice question shall be updated, it has to be ensured that the restrictions won't be violated.
         * Therefore the following conditions are checked (in contrast to the create-function it's allowed to leave parameters empty).
         * both answers and local id of the correct answer can't be empty.
         * the amount of answers should be in a range from 2 to 10.
         * the local id of the correct answer should neither be 0 nor higher than the total answers' count.
         */
        if (request.getAnswers() != null) {
            if (request.getAnswers().length < 2) {
                throw new ConflictException(resourceBundle.getString("CreateSingleChoiceRequest-answers-not-enough"));
            }
            if (request.getAnswers().length > 10) {
                throw new ConflictException(resourceBundle.getString("CreateSingleChoiceRequest-answers-too-many"));
            }
        }
        if (request.getCorrectAnswerLocalId() != null) {
            if (request.getCorrectAnswerLocalId() < 1) {
                throw new ConflictException(resourceBundle.getString("CreateSingleChoiceRequest-correct-answer-not-so-low"));
            }
        }

        if (request.getCorrectAnswerLocalId() != null && request.getAnswers() != null && request.getCorrectAnswerLocalId() > request.getAnswers().length
                || request.getCorrectAnswerLocalId() == null && request.getAnswers() != null && request.getCorrectAnswerLocalId() > request.getAnswers().length
                || request.getCorrectAnswerLocalId() != null && request.getAnswers() == null && request.getCorrectAnswerLocalId() > singleChoiceAnswerRepo.findAllByQuestionId(questionId).size()
        ) {
            throw new ConflictException(resourceBundle.getString("SingleChoiceQuestion-correct-answer-id-corrupt"));
        }

        if (request.getAnswers() != null) {
            /*
             * If there are new answers set in the update request, the existing ones will be deleted and
             * the new ones will be saved to the database.
             */
            for (SingleChoiceAnswer sca : singleChoiceAnswerRepo.findAllByQuestionId(questionId)) {
                singleChoiceAnswerRepo.delete(sca);
            }

            Integer answerLocalId = 0;

            for (String answer : request.getAnswers()) {
                answerLocalId++;
                singleChoiceAnswerRepo.save(new SingleChoiceAnswer(null, questionId, answerLocalId, answer));
            }
        }

        if (request.getCorrectAnswerLocalId() != null) {

            scq.setCorrectAnswerLocalId(request.getCorrectAnswerLocalId());
        }

        SingleChoiceQuestion updatedQuestion = this.getByQuestionId(questionId);
        updatedQuestion.setAnswers(singleChoiceAnswerRepo.findAllByQuestionId((questionId)));
        updatedQuestion.setCorrectAnswerLocalId(scq.getCorrectAnswerLocalId());

        return updatedQuestion;
    }
}
