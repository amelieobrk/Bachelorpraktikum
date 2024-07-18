package de.kreuzenonline.kreuzen.exam;


import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamRepo extends CrudRepository<Exam, Integer> {

    boolean existsByCourseIdAndDate(Integer courseId, LocalDate date);

    @Query("SELECT e.* FROM exam e JOIN course c on e.course_id = c.id " +
            " JOIN module m ON c.module_id = m.id WHERE m.university_id = :universityId"
    )
    Iterable<Exam> findAllByUniversityId(Integer universityId);

    Iterable<Exam> findAllByCourseId(Integer courseId);

    @Query("SELECT e.* FROM exam e JOIN course c on e.course_id = c.id " +
            " JOIN module m ON c.module_id = m.id WHERE m.university_id = :universityId AND c.semester_id = :semesterId"
    )
    List<Exam> findAllByUniversityIdAndSemesterId(Integer universityId, Integer semesterId);
    @Query("SELECT e.* FROM exam e JOIN course c on e.course_id = c.id " +
            " JOIN module m ON c.module_id = m.id JOIN major_has_module mhm on m.id = mhm.module_id " +
            " WHERE mhm.major_id = :majorId AND c.semester_id = :semesterId AND m.university_id = :universityId")
    List<Exam> findAllByUniversityIdAndMajorIdAndSemesterId(Integer universityId, Integer majorId, Integer semesterId);
    @Query("SELECT e.* FROM exam e JOIN course c on e.course_id = c.id " +
            " JOIN module m ON c.module_id = m.id JOIN major_has_module mhm on m.id = mhm.module_id " +
            " WHERE mhm.major_id = :majorId AND m.university_id = :universityId")
    List<Exam> findAllByUniversityIdAndMajorId(Integer universityId, Integer majorId);


    @Query("SELECT e.* FROM exam e JOIN course c on e.course_id = c.id WHERE c.module_id = :moduleId")
    Iterable<Exam> findAllByModuleId(Integer moduleId);
}
