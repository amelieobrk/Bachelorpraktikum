package de.kreuzenonline.kreuzen.module;

import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Service
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepo moduleRepo;
    private final ResourceBundle resourceBundle;

    public ModuleServiceImpl(ModuleRepo moduleRepo, ResourceBundle resourceBundle) {
        this.moduleRepo = moduleRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Module getById(Integer id) {

        Optional<Module> module = moduleRepo.findById(id);

        if (module.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("module-not-found"));
        }

        return module.get();
    }

    @Override
    public Module create(String name, Integer universityId, Boolean isUniversityWide) {

        Module module = new Module();
        // Two modules with the same name are not allowed
        if (moduleRepo.existsByNameIgnoreCase(name)) {
            throw new ConflictException(resourceBundle.getString("module-name-taken"));
        }
        module.setName(name);
        module.setUniversityId(universityId);
        module.setIsUniversityWide(isUniversityWide);

        return moduleRepo.save(module);
    }

    @Override
    public Module update(Integer id, String name, Integer universityId, Boolean isUniversityWide) {

        Module module = this.getById(id);

        if (name != null) {
            if (!name.equalsIgnoreCase(module.getName()) && moduleRepo.existsByNameIgnoreCase(name)) {
                throw new ConflictException(resourceBundle.getString("module-name-taken"));
            }
            module.setName(name);
        }
        if (universityId != null) {
            module.setUniversityId(universityId);
        }
        if (isUniversityWide != null) {
            module.setIsUniversityWide(isUniversityWide);
        }

        return moduleRepo.save(module);
    }

    @Override
    public void delete(Integer id) {
        moduleRepo.deleteById(id);
    }

    @Override
    public Iterable<Module> getAll() {
        return moduleRepo.findAll();
    }

    @Override
    public Iterable<Module> getModulesByUniversity(Integer universityId) {
        return moduleRepo.findAllByUniversityId(universityId);
    }

    @Override
    public List<Module> getModulesByUser(Integer userId) {
        return moduleRepo.findAllByUserId(userId);
    }

    @Override
    public void addModuleToMajor(Integer moduleId, Integer majorId) {
        moduleRepo.addModuleToMajor(moduleId, majorId);
    }

    @Override
    public void removeModuleFromMajor(Integer moduleId, Integer majorId) {
        moduleRepo.removeModuleFromMajor(moduleId, majorId);

    }

    @Override
    public void addModuleToSection(Integer moduleId, Integer sectionId) {
        moduleRepo.addModuleToMajorSection(moduleId, sectionId);

    }

    @Override
    public void removeModuleFromSection(Integer moduleId, Integer sectionId) {
        moduleRepo.removeModuleFromMajorSection(moduleId, sectionId);

    }


}


