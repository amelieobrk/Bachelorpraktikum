package de.kreuzenonline.kreuzen.course;

public interface CourseService {

    /**
     * Creates a new course that is linked to a specific module in a specific semester.
     *
     * @param moduleId   id of the module where the course belongs to.
     * @param semesterId id of the semester in which the course will be held.
     * @return Created Course.
     */
    Course create(Integer moduleId, Integer semesterId);

    /**
     * Finds all courses that belong to a module.
     *
     * @param moduleId Module Id
     * @return List of courses
     */
    Iterable<Course> findAllByModule(Integer moduleId);

    /**
     * Finds all courses that are linked to a semester.
     *
     * @param semesterId Semester Id
     * @return List of courses
     */
    Iterable<Course> findAllBySemester(Integer semesterId);

    /**
     * Find a specific course by its id.
     *
     * @param id id of the course
     * @return Course
     */
    Course findById(Integer id);

    /**
     * Updates a course.
     *
     * @param id         id of the course
     * @param moduleId   id of the module
     * @param semesterId id of the semester
     * @return updated Course
     */
    Course update(Integer id, Integer moduleId, Integer semesterId);

    /**
     * Deletes a course by the given id.
     *
     * @param id id of the course.
     */
    void delete(Integer id);
}
