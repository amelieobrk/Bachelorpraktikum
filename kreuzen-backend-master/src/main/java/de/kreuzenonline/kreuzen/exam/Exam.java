package de.kreuzenonline.kreuzen.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("exam")
public class Exam {

    @Id
    private Integer id;
    private String name;
    private Integer courseId;
    private LocalDate date;
    private Boolean isComplete;
    private Boolean isRetry;


}
