package de.kreuzenonline.kreuzen.semester;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("semester")
public class Semester {

    @Id
    private Integer id;
    private String name;
    private Integer startYear;
    private Integer endYear;
}
