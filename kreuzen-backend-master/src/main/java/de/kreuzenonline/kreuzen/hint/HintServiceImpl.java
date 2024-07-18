package de.kreuzenonline.kreuzen.hint;

import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;

@Service
public class HintServiceImpl implements HintService {

    private final HintRepo hintRepo;
    private final ResourceBundle resourceBundle;

    public HintServiceImpl(HintRepo hintRepo, ResourceBundle resourceBundle) {
        this.hintRepo = hintRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Iterable<Hint> getAll() {
        return hintRepo.findAll();
    }

    @Override
    public Hint create(String text, Boolean isActive) {
        Hint hint = new Hint();
        // To avoid redudant data, there shouldn't be two hints with the same text.
        if (hintRepo.existsByTextIgnoreCase(text)) {
            throw new ConflictException(resourceBundle.getString("hint-exists-already"));
        }

        hint.setText(text);
        hint.setIsActive(isActive);
        return hintRepo.save(hint);
    }

    @Override
    public Hint getById(Integer id) {
        Optional<Hint> hint = hintRepo.findById(id);

        if (hint.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("hint-not-found"));
        }
        return hint.get();
    }

    @Override
    public Hint getRandomHint() {
        List<Hint> activeHints = hintRepo.findAllByIsActiveTrue();

        if (activeHints.isEmpty()) {
            throw new ConflictException(resourceBundle.getString("no-active-hints"));
        }
        Random r = new Random();
        return activeHints.get(r.nextInt(activeHints.size()));
    }

    @Override
    public void delete(Integer id) {
        hintRepo.deleteById(id);

    }

    @Override
    public Hint update(Integer id, String text, Boolean isActive) {
        Hint hint = getById(id);
        if (text != null) {
            if (!text.equalsIgnoreCase(hint.getText()) && hintRepo.existsByTextIgnoreCase(text)) {
                throw new ConflictException(resourceBundle.getString("hint-exists-already"));
            }
            hint.setText(text);
        }

        if (isActive != null) {
            hint.setIsActive(isActive);
        }

        return hintRepo.save(hint);
    }
}
