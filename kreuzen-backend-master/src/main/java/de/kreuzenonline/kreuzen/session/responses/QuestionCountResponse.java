package de.kreuzenonline.kreuzen.session.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionCountResponse {

    private Integer count;

    public QuestionCountResponse(Integer count) {
        this.count = count;
    }
}
