package de.kreuzenonline.kreuzen.exam;


import de.kreuzenonline.kreuzen.exceptions.ConflictException;
import de.kreuzenonline.kreuzen.exceptions.NotFoundException;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepo examRepo;
    private final ResourceBundle resourceBundle;

    public ExamServiceImpl(ExamRepo examRepo, ResourceBundle resourceBundle) {
        this.examRepo = examRepo;
        this.resourceBundle = resourceBundle;

    }

    @Override
    public Exam getById(Integer id) {

        Optional<Exam> exam = examRepo.findById(id);

        if (exam.isEmpty()) {
            throw new NotFoundException(resourceBundle.getString("exam-not-found"));
        }

        return exam.get();
    }


    @Override
    public Exam create(String name, Integer courseId, LocalDate date, Boolean isComplete, Boolean isRetry) {

        Exam exam = new Exam();
        exam.setName(name);
        exam.setCourseId(courseId);

        if (examRepo.existsByCourseIdAndDate(courseId, date)) {
            throw new ConflictException(resourceBundle.getString("exam-course-date-exists"));
        }

        exam.setDate(date);
        exam.setIsComplete(isComplete);
        exam.setIsRetry(isRetry);

        return examRepo.save(exam);

    }

    @Override
    public Exam update(Integer id, String name, Integer courseId, LocalDate date, Boolean isComplete, Boolean isRetry) {

        Exam exam = this.getById(id);

        if (name != null) {
            exam.setName(name);
        }
        if (courseId != null) {
            exam.setCourseId(courseId);
        }
        if (date != null) {

            if (examRepo.existsByCourseIdAndDate(courseId, date)) {
                throw new ConflictException(resourceBundle.getString("exam-course-date-exists"));
            }

            exam.setDate(date);
        }

        if (isComplete != null) {
            exam.setIsComplete(isComplete);
        }
        if (isRetry != null) {
            exam.setIsRetry(isRetry);
        }

        return examRepo.save(exam);
    }

    @Override
    public void delete(Integer id) {
        examRepo.deleteById(id);
    }

    @Override
    public Iterable<Exam> getExamsByUniversity(Integer universityId) {
        return examRepo.findAllByUniversityId(universityId);
    }

    @Override
    public Iterable<Exam> getExamsByCourse(Integer courseId) {
        return examRepo.findAllByCourseId(courseId);
    }

    @Override
    public Iterable<Exam> getExamsByModule(Integer moduleId) {
        return examRepo.findAllByModuleId(moduleId);
    }

    @Override
    public List<Exam> getExamsByUniversityAndMajor(Integer universityId, Integer majorId) {
        return examRepo.findAllByUniversityIdAndMajorId(universityId, majorId);
    }

    @Override
    public List<Exam> getExamsBySemesterAndUniversity(Integer semesterId, Integer universityId) {
        return examRepo.findAllByUniversityIdAndSemesterId(universityId, semesterId);
    }

    @Override
    public List<Exam> getExamsByUniversityAndMajorAndSemester(Integer universityId, Integer majorId, Integer semesterId) {
        return examRepo.findAllByUniversityIdAndMajorIdAndSemesterId(universityId, majorId, semesterId);
    }
}
