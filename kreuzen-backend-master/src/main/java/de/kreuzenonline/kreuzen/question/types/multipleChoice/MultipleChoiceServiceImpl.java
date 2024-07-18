package de.kreuzenonline.kreuzen.question.types.multipleChoice;

import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.BaseQuestionRepo;
import de.kreuzenonline.kreuzen.question.types.QuestionTypeService;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.requests.CreateMultipleChoiceRequest;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.requests.UpdateMultipleChoiceRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Service("multipleChoice")
public class MultipleChoiceServiceImpl extends QuestionTypeService<MultipleChoiceQuestion, CreateMultipleChoiceRequest, UpdateMultipleChoiceRequest> implements MultipleChoiceService {

    public static final String TYPE = "multiple-choice";

    private final MultipleChoiceQuestionRepo multipleChoiceQuestionRepo;
    private final MultipleChoiceAnswerRepo multipleChoiceAnswerRepo;
    private final BaseQuestionRepo baseQuestionRepo;
    private final ResourceBundle resourceBundle;

    public MultipleChoiceServiceImpl(MultipleChoiceQuestionRepo multipleChoiceQuestionRepo, MultipleChoiceAnswerRepo multipleChoiceAnswerRepo, BaseQuestionRepo baseQuestionRepo, ResourceBundle resourceBundle) {
        super(CreateMultipleChoiceRequest.class,
                UpdateMultipleChoiceRequest.class, resourceBundle);
        this.multipleChoiceQuestionRepo = multipleChoiceQuestionRepo;
        this.multipleChoiceAnswerRepo = multipleChoiceAnswerRepo;
        this.baseQuestionRepo = baseQuestionRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public MultipleChoiceQuestion getByQuestionId(Integer questionId) {
        BaseQuestion baseQuestion = baseQuestionRepo.findById(questionId).orElseThrow(() -> new NotFoundException(resourceBundle.getString("question-not-found")));
        if (multipleChoiceQuestionRepo.findByQuestionId(questionId) == null) {
            throw new NotFoundException(resourceBundle.getString("multiple-choice-question-not-found"));
        }
        MultipleChoiceQuestionEntry questionEntry = multipleChoiceQuestionRepo.findByQuestionId(questionId);
        return new MultipleChoiceQuestion(
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
                questionEntry.getCorrectAnswerLocalIds(),
                multipleChoiceAnswerRepo.findAllByQuestionId(questionId)
        );
    }

    @Override
    public MultipleChoiceQuestion create(CreateMultipleChoiceRequest request, Integer questionId) {
        MultipleChoiceQuestionEntry entry = new MultipleChoiceQuestionEntry();

        /*
         * Restrictions for the creation of multiple choice questions:
         * both answers and correct local ids can't be empty.
         * the amount of answers should be in the range from 3 to 10.
         * at least one option should be marked as correct.
         * If no option is correct, question creators should use something like "None of the options of correct".
         */
        if (request.getAnswers() == null) {
            throw new ConflictException(resourceBundle.getString("CreateMultipleChoiceRequest-answers-not-null"));
        }
        if (request.getAnswers().length < 3) {
            throw new ConflictException(resourceBundle.getString("CreateMultipleChoiceRequest-answers-not-enough"));
        }
        if (request.getAnswers().length > 10) {
            throw new ConflictException(resourceBundle.getString("CreateMultipleChoiceRequest-answers-too-many"));
        }
        if (request.getCorrectAnswerLocalIds() == null) {
            throw new ConflictException(resourceBundle.getString("CreateMultipleChoiceRequest-correct-answers-not-null"));
        }
        if (request.getCorrectAnswerLocalIds().length < 1) {
            throw new ConflictException(resourceBundle.getString("CreateMultipleChoiceRequest-correct-answers-not-enough"));
        }

        if (request.getAnswers().length < request.getCorrectAnswerLocalIds().length) {
            throw new ConflictException(resourceBundle.getString("MultipleChoiceQuestion-correct-answer-ids-corrupt"));
        }

        entry.setQuestionId(questionId);
        entry.setCorrectAnswerLocalIds(request.getCorrectAnswerLocalIds());
        multipleChoiceQuestionRepo.save(entry);

        /*
         * local id is necessary to save the order of the answers to the database.
         * Therefore the following integer is initialized with 0 and later incremented by 1 with every option.
         */
        Integer answerLocalId = 0;

        List<MultipleChoiceAnswer> answers = new ArrayList<>();

        for (String answer : request.getAnswers()) {
            answerLocalId++;
            answers.add(multipleChoiceAnswerRepo.save(new MultipleChoiceAnswer(null, questionId, answerLocalId, answer)));
        }

        MultipleChoiceQuestion enteredQuestion = this.getByQuestionId(questionId);
        enteredQuestion.setCorrectAnswerLocalIds(request.getCorrectAnswerLocalIds());
        enteredQuestion.setAnswers(answers);

        return enteredQuestion;
    }

    @Override
    public MultipleChoiceQuestion update(UpdateMultipleChoiceRequest request, Integer questionId) {
        MultipleChoiceQuestionEntry mcq = multipleChoiceQuestionRepo.findByQuestionId(questionId);

        /*
         * When a multiple choice question shall be updated, it has to be ensured that the restrictions won't be violated.
         * Therefore the following conditions are checked (in contrast to the create-function it's allowed to leave parameters empty).
         * both answers and correct local ids can't be empty.
         * the amount of answers should be in the range from 3 to 10.
         * at least one option should be marked as correct.
         * If no option is correct, question creators should use something like "None of the options of correct".
         */
        if (request.getAnswers() != null) {
            if (request.getAnswers().length < 3) {
                throw new ConflictException(resourceBundle.getString("CreateMultipleChoiceRequest-answers-not-enough"));
            }
            if (request.getAnswers().length > 10) {
                throw new ConflictException(resourceBundle.getString("CreateMultipleChoiceRequest-answers-too-many"));
            }
        }
        if (request.getCorrectAnswerLocalIds() != null && request.getCorrectAnswerLocalIds().length < 2) {
            throw new ConflictException(resourceBundle.getString("CreateMultipleChoiceRequest-correct-answers-not-enough"));
        }

        if (request.getCorrectAnswerLocalIds() != null && request.getAnswers() != null && request.getCorrectAnswerLocalIds().length > request.getAnswers().length
                || request.getCorrectAnswerLocalIds() == null && request.getAnswers() != null && request.getCorrectAnswerLocalIds().length > request.getAnswers().length
                || request.getCorrectAnswerLocalIds() != null && request.getAnswers() == null && request.getCorrectAnswerLocalIds().length > multipleChoiceAnswerRepo.findAllByQuestionId(questionId).size()
        ) {
            throw new ConflictException(resourceBundle.getString("MultipleChoiceQuestion-correct-answer-ids-corrupt"));
        }

        if (request.getAnswers() != null) {
            /*
             * If there are new answers set in the update request, the existing ones will be deleted and
             * the new ones will be saved to the database.
             */
            for (MultipleChoiceAnswer mca : multipleChoiceAnswerRepo.findAllByQuestionId(questionId)) {
                multipleChoiceAnswerRepo.delete(mca);
            }

            Integer answerLocalId = 0;

            for (String answer : request.getAnswers()) {
                answerLocalId++;
                multipleChoiceAnswerRepo.save(new MultipleChoiceAnswer(null, questionId, answerLocalId, answer));
            }
        }
        if (request.getCorrectAnswerLocalIds() != null) {
            mcq.setCorrectAnswerLocalIds(request.getCorrectAnswerLocalIds());
        }

        MultipleChoiceQuestion updatedQuestion = this.getByQuestionId(questionId);
        updatedQuestion.setAnswers(multipleChoiceAnswerRepo.findAllByQuestionId(questionId));
        updatedQuestion.setCorrectAnswerLocalIds(mcq.getCorrectAnswerLocalIds());

        return updatedQuestion;
    }
}
