package de.kreuzenonline.kreuzen.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("module")
public class Module {

    @Id
    private Integer id;
    private String name;
    private Integer universityId;
    private Boolean isUniversityWide;
}
