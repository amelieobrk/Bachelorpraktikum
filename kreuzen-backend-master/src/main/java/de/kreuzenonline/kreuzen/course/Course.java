package de.kreuzenonline.kreuzen.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("course_view")
public class Course {

    @Id
    private Integer id;
    private Integer semesterId;
    private Integer moduleId;
    private String name;
}
