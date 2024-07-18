package de.kreuzenonline.kreuzen.section;

import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ResourceBundle;

@Service
public class SectionServiceImpl implements SectionService {

    private final SectionRepo sectionRepo;
    private final ResourceBundle resourceBundle;

    public SectionServiceImpl(SectionRepo sectionRepo, ResourceBundle resourceBundle) {
        this.sectionRepo = sectionRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Section create(Integer majorId, String name) {
        Section section = new Section();
        section.setMajorId(majorId);
        section.setName(name);

        return sectionRepo.save(section);
    }

    @Override
    public void addSectionToUser(Integer userId, Integer majorId, Integer sectionId) {
        sectionRepo.addUserToSection(userId, majorId, sectionId);
    }

    @Override
    public void addSectionToUser(Integer userId, Integer sectionId) {

        Section section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new NotFoundException(resourceBundle.getString("section-not-found")));

        sectionRepo.addUserToSection(userId, section.getMajorId(), sectionId);
    }

    @Override
    public Iterable<Section> findAllByMajor(Integer majorId) {
        return sectionRepo.getAllSectionsByMajorId(majorId);
    }

    @Override
    public Iterable<Section> findAllByModule(Integer moduleId) {
        return sectionRepo.findAllByModuleId(moduleId);
    }

    @Override
    public Section findById(Integer sectionId) {
        return sectionRepo.findById(sectionId)
                .orElseThrow(() -> new NotFoundException(resourceBundle.getString("section-not-found")));

    }

    @Override
    public List<Section> getSectionsByUserAndMajor(Integer userId, Integer majorId) {
        return sectionRepo.getSectionsByUserIdAndMajorId(userId, majorId);
    }

    @Override
    public Section update(Integer id, String name) {
        Section section = findById(id);
        if (name != null) {
            section.setName(name);
        }

        return sectionRepo.save(section);
    }

    @Override
    public void delete(Integer id) {
        sectionRepo.deleteById(id);
    }

    @Override
    public void removeSectionFromUser(Integer userId, Integer majorId, Integer sectionId) {
        sectionRepo.removeUserFromSection(userId, majorId, sectionId);
    }
}
