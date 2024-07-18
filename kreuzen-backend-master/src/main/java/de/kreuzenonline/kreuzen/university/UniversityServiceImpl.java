package de.kreuzenonline.kreuzen.university;

import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import de.kreuzenonline.kreuzen.exceptions.VerificationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Service
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepo universityRepo;
    private final ResourceBundle resourceBundle;

    public UniversityServiceImpl(UniversityRepo universityRepo, ResourceBundle resourceBundle) {
        this.universityRepo = universityRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Iterable<University> getAll() {
        return universityRepo.findAll();
    }

    @Override
    public University getById(Integer id) {

        Optional<University> university = universityRepo.findById(id);

        if (university.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("university-not-found"));
        }

        return university.get();
    }

    @Override
    public List<University> getByDomain(String domain) {
        return universityRepo.findByAllowedMailDomain(domain);
    }

    @Override
    public University create(String name, String[] allowedDomains) {

        University university = new University();
        university.setName(name);
        university.setAllowedMailDomains(allowedDomains);

        return universityRepo.save(university);
    }

    @Override
    public void delete(Integer id) {
        universityRepo.deleteById(id);
    }

    @Override
    public University update(Integer id, String name, String[] allowedDomains) {

        University university = this.getById(id);

        if (name != null) {
            university.setName(name);
        }
        if (allowedDomains != null) {
            university.setAllowedMailDomains(allowedDomains);
        }

        return universityRepo.save(university);
    }

    @Override
    public void checkEmailAllowed(Integer universityId, String email) {
        University university = this.getById(universityId);

        // Check email domain with allowed email domains by university
        String[] emailSplit = email.split("@");
        String domain = emailSplit[emailSplit.length - 1];

        boolean isAllowed = false;
        for (String allowedDomain : university.getAllowedMailDomains()) {
            if (allowedDomain.equalsIgnoreCase(domain)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            throw new VerificationException(resourceBundle.getString("uni-mail-required"));
        }
    }
}
