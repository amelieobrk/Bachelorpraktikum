package de.kreuzenonline.kreuzen.error;

import de.kreuzenonline.kreuzen.error.response.ErrorResponse;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.ResourceBundle;

@Service
public class ErrorServiceImpl implements ErrorService {

    private final ErrorRepo errorRepo;
    private final ResourceBundle resourceBundle;

    public ErrorServiceImpl(ErrorRepo errorRepo, ResourceBundle resourceBundle) {
        this.errorRepo = errorRepo;
        this.resourceBundle = resourceBundle;

    }

    @Override
    public Error getById(Integer id) {

        Optional<Error> errorReport = errorRepo.findById(id);

        if (errorReport.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("error-report-not-found"));
        }

        return errorReport.get();
    }

    @Override
    public Error create(String comment, String source, Integer questionId, Integer creatorId) {

        Error error = new Error();
        error.setComment(comment);
        error.setSource(source);
        error.setIsResolved(false);
        error.setQuestionId(questionId);
        error.setCreatorId(creatorId);
        return errorRepo.save(error);

    }

    @Override
    public Error update(Integer id, String comment, String source, Boolean isResolved, Integer lastAssignedModeratorId) {

        Error error = this.getById(id);

        if (comment != null) {
            error.setComment(comment);
        }
        if (source != null) {
            error.setSource(source);
        }


        if (isResolved != null) {
            error.setIsResolved(isResolved);
        }

        if (lastAssignedModeratorId != null) {
            error.setLastAssignedModeratorId(lastAssignedModeratorId);
        }

        return errorRepo.save(error);
    }

    @Override
    public void delete(Integer id) {
        errorRepo.deleteById(id);
    }

    @Override
    public Iterable<Error> getUnsolved() {
        return errorRepo.findAllByIsResolved(false);
    }
}
