package de.kreuzenonline.kreuzen.hint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("hint")
public class Hint {

    @Id
    private Integer id;
    private String text;
    private Boolean isActive;
}
