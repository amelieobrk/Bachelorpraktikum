package de.kreuzenonline.kreuzen.question;

import de.kreuzenonline.kreuzen.question.responses.BaseQuestionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("question_base")
public class BaseQuestion {

    @Id
    private Integer id;
    private String text;
    private String type;
    private String additionalInformation;
    private Integer points;
    private Integer examId;
    private Integer courseId;
    private Integer creatorId;
    private Integer updaterId;
    private String origin;
    private Boolean isApproved;

    public BaseQuestion(Integer id, String text, String type, String additionalInformation, Integer points, Integer examId, Integer courseId, Integer creatorId, String origin) {
    }
    public BaseQuestionResponse toResponse() {
        return new BaseQuestionResponse(this);
    }
}
