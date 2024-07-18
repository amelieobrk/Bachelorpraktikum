package de.kreuzenonline.kreuzen.major;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("major")
public class Major {

    @Id
    private Integer id;
    private String name;
    private Integer universityId;
}
