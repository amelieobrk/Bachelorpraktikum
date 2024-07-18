package de.kreuzenonline.kreuzen.university;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("university")
public class University {

    @Id
    private Integer id;
    private String name;
    private String[] allowedMailDomains;
}
