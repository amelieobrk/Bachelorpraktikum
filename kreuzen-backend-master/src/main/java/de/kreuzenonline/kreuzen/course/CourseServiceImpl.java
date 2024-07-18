package de.kreuzenonline.kreuzen.course;

import de.kreuzenonline.kreuzen.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.ResourceBundle;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepo courseRepo;
    private final ResourceBundle resourceBundle;

    public CourseServiceImpl(CourseRepo courseRepo, ResourceBundle resourceBundle) {
        this.courseRepo = courseRepo;
        this.resourceBundle = resourceBundle;
    }

    /**
     * Implementation of the save functionality which reloads the generated values (name).
     * Dirty fix, but spring does not load generated values for some reason.
     *
     * @param course Course to be saved
     * @return Saved course
     */
    private Course save(Course course) {
        Course savedCourse = courseRepo.save(course);
        return courseRepo.findById(savedCourse.getId()).orElse(savedCourse);
    }

    @Override
    public Course create(Integer moduleId, Integer semesterId) {
        Course course = new Course();
        course.setModuleId(moduleId);
        course.setSemesterId(semesterId);
        return this.save(course); //
    }

    @Override
    public Iterable<Course> findAllByModule(Integer moduleId) {
        return courseRepo.findAllByModuleId(moduleId);
    }

    @Override
    public Iterable<Course> findAllBySemester(Integer semesterId) {
        return courseRepo.findAllBySemesterId(semesterId);
    }

    @Override
    public Course findById(Integer id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(resourceBundle.getString("course-not-found")));
    }

    @Override
    public Course update(Integer id, Integer moduleId, Integer semesterId) {
        Course course = findById(id);

        if (moduleId != null) {
            course.setModuleId(moduleId);
        }

        if (semesterId != null) {
            course.setSemesterId(semesterId);
        }
        return this.save(course); // Dirty fix, but spring does not load generated values for some reason
    }

    @Override
    public void delete(Integer id) {
        courseRepo.deleteById(id);
    }
}
