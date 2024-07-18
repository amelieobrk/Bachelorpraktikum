package de.kreuzenonline.kreuzen.question.types;

import de.kreuzenonline.kreuzen.question.BaseQuestion;
import de.kreuzenonline.kreuzen.question.requests.CreateQuestionRequest;
import de.kreuzenonline.kreuzen.question.requests.UpdateQuestionRequest;
import de.kreuzenonline.kreuzen.question.types.assignment.AssignmentQuestionServiceImpl;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceServiceImpl;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QuestionTypeMapperServiceImpl implements QuestionTypeMapperService {

    @Autowired
    private MultipleChoiceServiceImpl multipleChoiceService;
    @Autowired
    private SingleChoiceServiceImpl singleChoiceService;
    @Autowired
    private AssignmentQuestionServiceImpl assignmentQuestionService;

    @Override
    public Optional<QuestionTypeService<? extends BaseQuestion, ? extends CreateQuestionRequest, ? extends UpdateQuestionRequest>> getServiceByType(String type) {

        switch (type) {
            case MultipleChoiceServiceImpl.TYPE:
                return Optional.of(multipleChoiceService);
            case SingleChoiceServiceImpl.TYPE:
                return Optional.of(singleChoiceService);
            case AssignmentQuestionServiceImpl.TYPE:
                return Optional.of(assignmentQuestionService);
            default:
                return Optional.empty();
        }
    }
}
