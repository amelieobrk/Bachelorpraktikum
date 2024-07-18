package de.kreuzenonline.kreuzen.course.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCourseRequest {

    @NotNull(message = "CreateCourseRequest-semesterId-not-null")
    private Integer semesterId;
}
