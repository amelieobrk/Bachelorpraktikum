package de.kreuzenonline.kreuzen.course.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCourseRequest {

    private Integer moduleId;

    private Integer semesterId;
}
