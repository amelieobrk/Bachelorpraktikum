package de.kreuzenonline.kreuzen.question;

import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import de.kreuzenonline.kreuzen.question.origin.QuestionOriginRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Service
public class BaseQuestionServiceImpl implements BaseQuestionService {

    private final BaseQuestionRepo baseQuestionRepo;
    private final QuestionOriginRepo questionOriginRepo;
    private final ResourceBundle resourceBundle;

    public BaseQuestionServiceImpl(BaseQuestionRepo baseQuestionRepo, QuestionOriginRepo questionOriginRepo, ResourceBundle resourceBundle) {
        this.baseQuestionRepo = baseQuestionRepo;
        this.questionOriginRepo = questionOriginRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public BaseQuestion getById(Integer id) {

        Optional<BaseQuestion> base = baseQuestionRepo.findById(id);
        if (base.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("question-not-found"));
        }
        return base.get();
    }

    @Override
    public BaseQuestion create(String text, String type, String additionalInformation, Integer points, Integer examId, Integer courseId, Integer creatorId, String origin, Byte[] file) {
        if (type == null) {
            throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-type-not-null"));
        }
        if (text == null) {
            throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-text-not-null"));
        }
        if (text.length() < 8) {
            throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-text-too-short"));
        }
        if (text.length() > 512) {
            throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-text-too-long"));
        }
        if (additionalInformation != null && additionalInformation.length() > 1024) {
            throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-additionalInformation-too-long"));
        }
        if (points < 0) {
            throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-points-too-low"));
        }
        if (points > 10) {
            throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-points-too-high"));
        }
        if (courseId == null) {
            throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-courseId-not-null"));
        }
        if (origin == null) {
            throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-origin-not-null"));
        }

        BaseQuestion baseQuestion = new BaseQuestion();
        baseQuestion.setText(text);
        baseQuestion.setType(type);
        baseQuestion.setAdditionalInformation(additionalInformation);
        baseQuestion.setPoints(points);
        baseQuestion.setExamId(examId);
        baseQuestion.setCourseId(courseId);
        baseQuestion.setCreatorId(creatorId);

        if (!questionOriginRepo.existsByName(origin)) {
            throw new ConflictException(resourceBundle.getString("question-origin-does-not-exist"));
        }
        baseQuestion.setOrigin(origin);
        baseQuestion.setIsApproved(false);

        return baseQuestionRepo.save(baseQuestion);
    }

    @Override
    public BaseQuestion update(Integer id, String text, String additionalInformation, Integer points, Integer examId, Integer courseId, String origin, Byte[] file, Integer updaterId) {
        BaseQuestion baseQuestion = this.getById(id);
        if (text != null) {
            if (text.length() < 8) {
                throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-text-too-short"));
            }
            if (text.length() > 512) {
                throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-text-too-long"));
            }
            baseQuestion.setText(text);
        }
        if (additionalInformation != null) {
            if (additionalInformation.length() > 1024) {
                throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-additionalInformation-too-long"));
            }
            baseQuestion.setAdditionalInformation(additionalInformation);
        }
        if (points != null) {
            if (points < 0) {
                throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-points-too-low"));
            }
            if (points > 10) {
                throw new ConflictException(resourceBundle.getString("CreateQuestionRequest-points-too-high"));
            }
            baseQuestion.setPoints(points);
        }
        if (examId != null) {
            baseQuestion.setExamId(examId);
        }
        if (courseId != null) {
            baseQuestion.setCourseId(courseId);
        }
        if (origin != null) {
            if (!questionOriginRepo.existsByName(origin)) {
                throw new ConflictException(resourceBundle.getString("question-origin-does-not-exist"));
            }
            baseQuestion.setOrigin(origin);
        }

        return baseQuestionRepo.save(baseQuestion);
    }

    @Override
    public BaseQuestion approve(Integer id) {
        BaseQuestion baseQuestion = this.getById(id);
        baseQuestion.setIsApproved(true);
        return baseQuestionRepo.save(baseQuestion);
    }

    @Override
    public void disapprove(Integer id) {
        BaseQuestion baseQuestion = this.getById(id);
        baseQuestion.setIsApproved(false);
        baseQuestionRepo.save(baseQuestion);
    }

    @Override
    public void delete(Integer questionId) {
        baseQuestionRepo.deleteById(questionId);
    }

    @Override
    public Iterable<BaseQuestion> findAllByExam(Integer examId, Boolean isAdmin) {
        if (isAdmin) {
            return baseQuestionRepo.findAllByExamId(examId);
        } else return baseQuestionRepo.findAllByExamIdAndIsApprovedTrue(examId);
    }

    @Override
    public Iterable<BaseQuestion> findAllByCourse(Integer courseId, Boolean isAdmin) {
        if (isAdmin) {
            return baseQuestionRepo.findAllByCourseId(courseId);
        } else return baseQuestionRepo.findAllByCourseIdAndIsApprovedTrue(courseId);
    }

    @Override
    public List<BaseQuestion> getByPagination(Boolean onlyApproved, String searchTerm, Integer semesterId, Integer moduleId, Integer courseId, Integer examId, Integer tagId, int limit, int skip, Boolean isAdmin) {

        // Add :* to search term for incomplete keyword. Not added when a trailing space exists
        if(searchTerm != null && searchTerm.charAt(searchTerm.length() - 1) != ' ') {
            searchTerm += ":*";
        }

        if (isAdmin) {
            if (onlyApproved != null) {
                return baseQuestionRepo.findBySearchTerm(searchTerm, semesterId, moduleId, courseId, examId, tagId, true, limit, skip);
            } else
                return baseQuestionRepo.findBySearchTerm(searchTerm, semesterId, moduleId, courseId, examId, tagId, null, limit, skip);
        } else {
            return baseQuestionRepo.findBySearchTerm(searchTerm, semesterId, moduleId, courseId, examId, tagId, true, limit, skip);
        }
    }

    @Override
    public long getCount(Boolean onlyApproved, String searchTerm, Integer semesterId, Integer moduleId, Integer courseId, Integer examId, Integer tagId, Boolean isAdmin) {

        // Add :* to search term for incomplete keyword. Not added when a trailing space exists
        if(searchTerm != null && searchTerm.charAt(searchTerm.length() - 1) != ' ') {
            searchTerm += ":*";
        }

        if (isAdmin) {
            if (onlyApproved != null) {
                return baseQuestionRepo.countBySearchTerm(searchTerm, semesterId, moduleId, courseId, examId, tagId, onlyApproved);
            } else
                return baseQuestionRepo.countBySearchTerm(searchTerm, semesterId, moduleId, courseId, examId, tagId, null);
        } else {
            return baseQuestionRepo.countBySearchTerm(searchTerm, semesterId, moduleId, courseId, examId, tagId, true);
        }
    }

    @Override
    public long getCount(String searchTerm, Boolean isAdmin) {
        if (isAdmin) {
            return baseQuestionRepo.countBySearchTerm(searchTerm);
        } else {
            return baseQuestionRepo.countBySearchTermAndIsApprovedTrue(searchTerm);
        }
    }

    @Override
    public void addQuestionToExam(Integer examId, Integer questionId) {
        baseQuestionRepo.addQuestionToExam(examId, questionId);
    }

    @Override
    public void removeQuestionFromExam(Integer examId, Integer questionId) {
        baseQuestionRepo.removeQuestionFromExam(examId, questionId);
    }

    @Override
    public Iterable<BaseQuestion> getAllUnapproved() {
        return baseQuestionRepo.findBySearchTerm("", null, null, null, null, null, false, 20, 0);
    }

    @Override
    public void addQuestionToSession(Integer sessionId, Integer questionId) {
        baseQuestionRepo.addQuestionToSession(sessionId, questionId);
    }

    @Override
    public void removeQuestionFromSession(Integer sessionId, Integer questionId) {
        baseQuestionRepo.removeQuestionFromSession(sessionId, questionId);
    }

    @Override
    public Iterable<BaseQuestion> findAllBySession(Integer sessionId) {
        return baseQuestionRepo.findAllBySession(sessionId);
    }

    @Override
    public BaseQuestion findBySessionLocalId(Integer sessionId, Integer localId) {
        return baseQuestionRepo.findBySessionAndLocalId(sessionId, localId);
    }
}
