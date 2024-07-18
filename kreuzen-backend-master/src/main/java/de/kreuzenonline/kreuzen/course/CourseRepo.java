package de.kreuzenonline.kreuzen.course;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepo extends CrudRepository<Course, Integer> {

    List<Course> findAllByModuleId(Integer moduleId);

    List<Course> findAllBySemesterId(Integer semesterId);
}
