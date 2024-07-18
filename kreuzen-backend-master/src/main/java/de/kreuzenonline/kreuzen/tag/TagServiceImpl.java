package de.kreuzenonline.kreuzen.tag;

import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ResourceBundle;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepo tagRepo;
    private final ResourceBundle resourceBundle;

    public TagServiceImpl(TagRepo tagRepo, ResourceBundle resourceBundle) {
        this.tagRepo = tagRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Tag create(String name, Integer moduleId) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setModuleId(moduleId);
        return tagRepo.save(tag);
    }

    @Override
    public Tag findById(Integer id) {
        return tagRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(resourceBundle.getString("tag-not-found")));
    }

    @Override
    public Iterable<Tag> findAllByModule(Integer moduleId) {
        return tagRepo.findAllByModuleId(moduleId);
    }


    @Override
    public Tag update(Integer id, String name) {
        Tag tag = findById(id);

        tag.setName(name);

        return tagRepo.save(tag);
    }

    @Override
    public void delete(Integer id) {
        tagRepo.deleteById(id);
    }

    @Override
    public Iterable<Tag> findAllByQuestion(Integer questionId) {
        return tagRepo.findAllByQuestionId(questionId);
    }

    @Override
    public void addTagToQuestion(Integer questionId, Integer tagId) {
        tagRepo.addTagToQuestion(questionId, tagId);
    }

    @Override
    public void removeTagFromQuestion(Integer questionId, Integer tagId) {
        tagRepo.removeTagFromQuestion(questionId, tagId);
    }
}
