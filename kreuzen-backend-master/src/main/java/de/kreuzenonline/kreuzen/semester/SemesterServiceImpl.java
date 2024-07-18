package de.kreuzenonline.kreuzen.semester;

import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.ResourceBundle;

@Service
public class SemesterServiceImpl implements SemesterService {

    private final SemesterRepo semesterRepo;
    private final ResourceBundle resourceBundle;

    public SemesterServiceImpl(SemesterRepo semesterRepo, ResourceBundle resourceBundle) {
        this.semesterRepo = semesterRepo;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public Iterable<Semester> getAll() {
        return semesterRepo.findAll();
    }

    @Override
    public Semester getById(Integer id) {
        Optional<Semester> semester = semesterRepo.findById(id);

        if (semester.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("semester-not-found"));
        }
        return semester.get();
    }

    @Override
    public void delete(Integer id) {
        semesterRepo.deleteById(id);
    }

    @Override
    public Semester create(String name, Integer startYear, Integer endYear) {
        Semester semester = new Semester();
        // Two semesters with the same name are not allowed
        if (semesterRepo.existsByNameIgnoreCase(name)) {
            throw new ConflictException(resourceBundle.getString("semester-name-taken"));
        }
        semester.setName(name);
        // Difference between start year and end year can just be 0 or 1 (in years).
        if (endYear < startYear) {
            throw new ConflictException(resourceBundle.getString("semester-duration-negative"));
        }
        if (endYear - startYear > 1) {
            throw new ConflictException(resourceBundle.getString("semester-duration-too-long"));
        }
        semester.setStartYear(startYear);
        semester.setEndYear(endYear);
        return semesterRepo.save(semester);
    }

    @Override
    public Semester update(Integer id, String name, Integer startYear, Integer endYear) {
        Semester semester = getById(id);
        if (name != null) {
            if (!name.equalsIgnoreCase(semester.getName()) && semesterRepo.existsByNameIgnoreCase(name)) {
                throw new ConflictException(resourceBundle.getString("semester-name-taken"));
            }
            semester.setName(name);
        }

        if (startYear != null && endYear != null) {
            // Difference between start year and end year can just be 0 or 1 (in years).
            if (endYear < startYear) {
                throw new ConflictException(resourceBundle.getString("semester-duration-negative"));
            }
            if (endYear - startYear > 1) {
                throw new ConflictException(resourceBundle.getString("semester-duration-too-long"));
            }
        }

        if (startYear != null) {
            semester.setStartYear(startYear);
        }
        if (endYear != null) {
            semester.setEndYear(endYear);
        }

        return semesterRepo.save(semester);
    }

}
