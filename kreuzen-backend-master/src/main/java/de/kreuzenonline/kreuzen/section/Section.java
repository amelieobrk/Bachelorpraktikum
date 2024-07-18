package de.kreuzenonline.kreuzen.section;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("major_section")
public class Section {

    @Id
    private Integer id;
    private Integer majorId;
    private String name;
}
