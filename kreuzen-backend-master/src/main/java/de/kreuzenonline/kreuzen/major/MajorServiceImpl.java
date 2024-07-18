package de.kreuzenonline.kreuzen.major;

import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ResourceBundle;

@Service
public class MajorServiceImpl implements MajorService {

    private final MajorRepo majorRepo;
    private final ResourceBundle resourceBundle;

    public MajorServiceImpl(MajorRepo majorRepo, ResourceBundle resourceBundle) {
        this.majorRepo = majorRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Major create(Integer universityId, String name) {

        Major major = new Major();
        major.setName(name);
        major.setUniversityId(universityId);

        return majorRepo.save(major);
    }

    @Override
    public Iterable<Major> findAllByUniversity(Integer universityId) {
        return majorRepo.findAllByUniversityId(universityId);
    }

    @Override
    public Iterable<Major> findAllByModule(Integer moduleId) {
        return majorRepo.findAllByModuleId(moduleId);
    }

    @Override
    public Major findById(Integer id) {
        return majorRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(resourceBundle.getString("major-not-found")));
    }

    @Override
    public Major update(Integer id, String name) {

        Major major = findById(id);
        if (name != null) {
            major.setName(name);
        }

        return majorRepo.save(major);
    }

    @Override
    public void delete(Integer id) {
        majorRepo.deleteById(id);
    }

    @Override
    public List<Major> getMajorsByUser(Integer userId) {
        return majorRepo.findAllByUser(userId);
    }

    @Override
    public void addMajorToUser(Integer userId, Integer majorId) {
        majorRepo.addMajorToUser(userId, majorId);
    }

    @Override
    public void removeMajorFromUser(Integer userId, Integer majorId) {
        majorRepo.removeMajorFromUser(userId, majorId);

    }
}
