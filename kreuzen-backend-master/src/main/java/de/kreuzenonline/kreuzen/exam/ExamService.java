package de.kreuzenonline.kreuzen.exam;


import java.time.LocalDate;
import java.util.List;


public interface ExamService {


    /**
     * Get a specific exam by its id.
     *
     * @param id Id of exam
     * @return exam
     */
    Exam getById(Integer id);


    /**
     * Create a new exam.
     *
     * @param name       name of the exam
     * @param courseId   id of corresponding course
     * @param date       date the exam was written
     * @param isComplete declares if all questions are present
     * @param isRetry    specifies if exam is a retry
     * @return New Exam
     */
    Exam create(String name, Integer courseId, LocalDate date, Boolean isComplete, Boolean isRetry);

    /**
     * Update an exam. Except for the id, all values that should not be updated can be set to null.
     *
     * @param id         Id of the exam
     * @param name       Name of the exam
     * @param courseId   Id of the corresponding course
     * @param date       date the exam was written
     * @param isComplete declares if all questions are present
     * @param isRetry    specifies if exam is a retry
     * @return Updated exam
     */
    Exam update(Integer id, String name, Integer courseId, LocalDate date, Boolean isComplete, Boolean isRetry);

    /**
     * Deletes an exam by its given id.
     * Only users with the role Administrator or Moderator can delete module.
     * Before the module is deleted, the password needs to be entered for security purposes.
     *
     * @param id id of the exam.
     */
    void delete(Integer id);

    /**
     * Get all exams written by students of a specific university.
     *
     * @param universityId University Id
     * @return List of exams
     */
    Iterable<Exam> getExamsByUniversity(Integer universityId);

    /**
     * Get all exams written by students of a specific course.
     *
     * @param courseId Id of course
     * @return List of exams
     */
    Iterable<Exam> getExamsByCourse(Integer courseId);


    /**
     * Get all exams written by students of a specific module.
     *
     * @param moduleId Id of module
     * @return List of exams
     */
    Iterable<Exam> getExamsByModule(Integer moduleId);

    /**
     * Get all exams written by students of a specific semester at a specific university.
     *
     * @param semesterId Id of semester
     * @param universityId Id of university
     * @return List of exams
     */
    List<Exam> getExamsBySemesterAndUniversity(Integer universityId, Integer semesterId);

    /**
     * Get all exams written by students of a specific major.
     *
     * @param majorId Id of major
     * @param universityId Id of university
     * @return List of exams
     */
    List<Exam> getExamsByUniversityAndMajor(Integer universityId, Integer majorId);

    /**
     * Get all exams written by students of a specific major in a specific semester.
     *
     * @param majorId Id of major
     * @param universityId Id of university
     * @param semesterId Id of semester
     * @return List of exams
     */
    List<Exam> getExamsByUniversityAndMajorAndSemester(Integer universityId, Integer majorId, Integer semesterId);

}
