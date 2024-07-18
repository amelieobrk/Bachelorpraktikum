package de.kreuzenonline.kreuzen.question.responses;

import de.kreuzenonline.kreuzen.question.BaseQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseQuestionResponse {

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
    private Instant createdAt;
    private Instant updatedAt;

    public BaseQuestionResponse(BaseQuestion question) {
        this.id = question.getId();
        this.text = question.getText();
        this.type = question.getType();
        this.additionalInformation = question.getAdditionalInformation();
        this.points = question.getPoints();
        this.examId = question.getExamId();
        this.courseId = question.getCourseId();
        this.creatorId = question.getCreatorId();
        this.updaterId = question.getUpdaterId();
        this.origin = question.getOrigin();
        this.isApproved = question.getIsApproved();
    }
}
