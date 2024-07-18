package de.kreuzenonline.kreuzen.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("session_has_question")
public class SessionQuestion {

    @Id
    private Integer id;
    private Integer sessionId;
    private Integer questionId;
    private Integer localId;
    private Integer time;
    private Boolean isSubmitted;

}
