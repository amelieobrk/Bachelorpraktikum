package de.kreuzenonline.kreuzen.course.responses;

import de.kreuzenonline.kreuzen.course.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse {

    private Integer id;
    private Integer moduleId;
    private Integer semesterId;
    private String name;

    public CourseResponse(Course course) {
        this.id = course.getId();
        this.moduleId = course.getModuleId();
        this.semesterId = course.getSemesterId();
        this.name = course.getName();
    }
}
