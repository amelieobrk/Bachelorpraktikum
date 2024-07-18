package de.kreuzenonline.kreuzen.session;

import de.kreuzenonline.kreuzen.exceptions.ForbiddenException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import de.kreuzenonline.kreuzen.question.types.multipleChoice.MultipleChoiceQuestion;
import de.kreuzenonline.kreuzen.question.types.singleChoice.SingleChoiceQuestion;
import de.kreuzenonline.kreuzen.session.responses.QuestionResultResponse;
import de.kreuzenonline.kreuzen.session.selections.MultipleChoiceSelection;
import de.kreuzenonline.kreuzen.session.selections.SingleChoiceSelection;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SessionServiceImpl implements SessionService {

    private final MultipleChoiceSelectionRepo multipleChoiceSelectionRepo;
    private final SingleChoiceSelectionRepo singleChoiceSelectionRepo;
    private final SessionQuestionRepo sessionQuestionRepo;
    private final SessionRepo sessionRepo;
    private final ResourceBundle resourceBundle;


    public SessionServiceImpl(SessionRepo sessionRepo, ResourceBundle resourceBundle, MultipleChoiceSelectionRepo multipleChoiceSelectionRepo, SingleChoiceSelectionRepo singleChoiceSelectionRepo, SessionQuestionRepo sessionQuestionRepo) {
        this.sessionRepo = sessionRepo;
        this.resourceBundle = resourceBundle;
        this.multipleChoiceSelectionRepo = multipleChoiceSelectionRepo;
        this.singleChoiceSelectionRepo = singleChoiceSelectionRepo;
        this.sessionQuestionRepo = sessionQuestionRepo;
    }

    @Override
    public Session getById(Integer id) {
        Optional<Session> session = sessionRepo.findById(id);

        if (session.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("session-not-found"));
        }
        return session.get();
    }

    @Override
    public SessionQuestion getQuestionBySessionAndLocalId(Integer sessionId, Integer localId) {
        return sessionQuestionRepo.findBySessionIdAndLocalId(sessionId, localId);
    }

    @Override
    public Session create(Integer creatorId,
                          String name,
                          String sessionType,
                          Boolean isRandom,
                          String notes,
                          Integer[] moduleIds,
                          Integer[] semesterIds,
                          Integer[] tagIds,
                          String[] questionTypes,
                          String[] questionOrigins,
                          String textFilter) {
        Session session = new Session();
        session.setCreatorId(creatorId);
        session.setName(name);
        session.setType(sessionType);
        session.setIsRandom(isRandom);
        session.setNotes(notes);
        session.setIsFinished(false);

        session = sessionRepo.save(session);

        if (moduleIds == null) moduleIds = new Integer[0];
        if (semesterIds == null) semesterIds = new Integer[0];
        if (tagIds == null) tagIds = new Integer[0];
        if (questionTypes == null) questionTypes = new String[0];
        if (questionOrigins == null) questionOrigins = new String[0];

        if (isRandom) {
            sessionRepo.assignQuestionsRandom(
                    session.getId(),
                    moduleIds,
                    semesterIds,
                    tagIds,
                    questionTypes,
                    questionOrigins,
                    textFilter
            );
        } else {
            sessionRepo.assignQuestions(
                    session.getId(),
                    moduleIds,
                    semesterIds,
                    tagIds,
                    questionTypes,
                    questionOrigins,
                    textFilter
            );
        }

        return session;
    }

    @Override
    public Session update(Integer id, String name, String sessionType, Boolean isRandom, String notes) {
        Session session = this.getById(id);

        if (name != null) {
            session.setName(name);
        }
        if (sessionType != null) {
            session.setType(sessionType);
        }
        if (isRandom != null) {
            session.setIsRandom(isRandom);
        }
        if (notes != null) {
            session.setNotes(notes);
        }
        return sessionRepo.save(session);
    }

    @Override
    public void delete(Integer id) {
        sessionRepo.deleteById(id);
    }

    @Override
    public Iterable<SingleChoiceSelection> findAllSingleChoiceSelections(Integer sessionId, Integer localQuestionId) {
        return singleChoiceSelectionRepo.findSingleChoiceSelections(sessionId, localQuestionId);
    }

    @Override
    public Iterable<MultipleChoiceSelection> findAllMultipleChoiceSelections(Integer sessionId, Integer localQuestionId) {
        return multipleChoiceSelectionRepo.findMultipleChoiceSelections(sessionId, localQuestionId);
    }

    @Override
    public QuestionResultResponse singleChoiceResult(Integer sessionId, SingleChoiceQuestion question, Integer localId) {
        Iterable<SingleChoiceSelection> selections = singleChoiceSelectionRepo.findSingleChoiceSelections(sessionId, localId);
        Integer points = 0;
        Integer checkedSelectionLocalId = -1;
        for (SingleChoiceSelection selection : selections) {
            if (selection.getIsChecked()) {
                checkedSelectionLocalId = selection.getLocalAnswerId();
                break;
            }
        }
        if (question.getCorrectAnswerLocalId().equals(checkedSelectionLocalId)) {
            points = question.getPoints();
        }


        return new QuestionResultResponse(sessionId, question, points, localId);
    }

    @Override
    public QuestionResultResponse multipleChoiceResult(Integer sessionId, MultipleChoiceQuestion question, Integer localId) {
        Iterable<MultipleChoiceSelection> selections = multipleChoiceSelectionRepo.findMultipleChoiceSelections(sessionId, localId);
        Integer points = 0;
        Integer correctAnswers = 0;
        for (MultipleChoiceSelection selection : selections) {
            if (selection.getIsChecked().equals(true)) {
                for (Integer correctAnswer : question.getCorrectAnswerLocalIds()) {
                    Boolean choice = selection.getLocalAnswerId().equals(correctAnswer);
                    if (choice.equals(true)) {
                        correctAnswers = correctAnswers + 1;
                        break;
                    }
                }
            }
        }
        if (correctAnswers.equals(question.getCorrectAnswerLocalIds().length)) {
            points = question.getPoints();
        }

        return new QuestionResultResponse(sessionId, question, points, localId);
    }

    @Override
    public List<SessionQuestion> getAllSessionQuestions(Integer sessionId) {
        return sessionQuestionRepo.findAllBySessionId(sessionId);
    }

    @Override
    public Session resetSelection(Integer sessionId) {
        sessionRepo.resetSession(sessionId);
        sessionRepo.resetSessionQuestions(sessionId);
        sessionRepo.resetSessionSingleChoiceSelections(sessionId);
        sessionRepo.resetSessionMultipleChoiceSelections(sessionId);
        return this.getById(sessionId);
    }

    @Override
    public Session finishSession(Integer sessionId) {
        sessionRepo.submitSession(sessionId);
        sessionRepo.submitAllQuestions(sessionId);
        return this.getById(sessionId);
    }

    @Override
    public List<Session> getByPagination(Integer userId, Integer limit, Integer skip) {
        return sessionRepo.findAllPagination(userId, limit, skip);
    }

    @Override
    public Integer getCountByUser(Integer userId) {
        return sessionRepo.getCountByUser(userId);

    }

    @Override
    public void addSingleChoiceSelection(Integer sessionId, Integer localId, Integer checkedLocalAnswerId, Integer[] crossedLocalAnswerIds) {

        Set<Integer> crossed = new HashSet<>(Arrays.asList(crossedLocalAnswerIds));
        Integer answerCount = sessionRepo.getSingleChoiceAnswerCount(sessionId, localId);
        for (int i = 1; i <= answerCount; i++) {
            boolean isChecked = false;
            if (checkedLocalAnswerId != null) {
                isChecked = i == checkedLocalAnswerId;
            }
            boolean isCrossed = crossed.contains(i);
            if (isChecked && isCrossed) {
                throw new ForbiddenException(resourceBundle.getString("answer-can't-be-checked-and-crossed"));
            }
            sessionRepo.addSingleChoiceSelection(sessionId, localId, i, isChecked, isCrossed);
        }
    }

    @Override
    public void addMultipleChoiceSelection(Integer sessionId, Integer localId, Integer[] checkedLocalAnswerIds, Integer[] crossedLocalAnswerIds) {

        Set<Integer> checked = new HashSet<>(Arrays.asList(checkedLocalAnswerIds));
        Set<Integer> crossed = new HashSet<>(Arrays.asList(crossedLocalAnswerIds));
        Integer answerCount = sessionRepo.getMultipleChoiceAnswerCount(sessionId, localId);
        boolean isChecked;
        boolean isCrossed;
        for(int i = 1; i<=answerCount;i++) {
            isChecked = checked.contains(i);
            isCrossed = crossed.contains(i);
            if (isChecked && isCrossed) {
                throw new ForbiddenException(resourceBundle.getString("answer-can't-be-checked-and-crossed"));
            }
        }
        for (int i = 1; i <= answerCount; i++) {
            isChecked = checked.contains(i);
            isCrossed = crossed.contains(i);
            sessionRepo.addMultipleChoiceSelection(sessionId, localId, i, isChecked, isCrossed);
        }
    }

    @Override
    public SessionQuestion addTime(Integer sessionId, Integer localId, Integer answerTime) {
        sessionRepo.addTime(sessionId, localId, answerTime);
        return sessionQuestionRepo.findBySessionIdAndLocalId(sessionId, localId);
    }

    @Override
    public Integer getCountByParameters(Integer[] moduleIds, Integer[] semesterIds, Integer[] tagIds, String[] questionTypes, String[] questionOrigins, String textFilter) {

        if (moduleIds == null) moduleIds = new Integer[0];
        if (semesterIds == null) semesterIds = new Integer[0];
        if (tagIds == null) tagIds = new Integer[0];
        if (questionTypes == null) questionTypes = new String[0];
        if (questionOrigins == null) questionOrigins = new String[0];

        return sessionRepo.getCountByParameters(moduleIds, semesterIds, tagIds, questionTypes, questionOrigins, textFilter);
    }

    @Override
    public SessionQuestion submitQuestion(Integer sessionId, Integer localId) {
        sessionRepo.submitQuestion(sessionId, localId);
        return sessionQuestionRepo.findBySessionIdAndLocalId(sessionId, localId);
    }

    @Override
    public Integer getCount(Integer id) {
        return sessionRepo.getQuestionCountBySessionId(id);
    }
}
