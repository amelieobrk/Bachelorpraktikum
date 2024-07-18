package de.kreuzenonline.kreuzen.session.responses;

import de.kreuzenonline.kreuzen.session.Session;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionResponse {

    private Integer id;
    private Integer creatorId;
    private String name;
    private String sessionType;
    private Boolean isRandom;
    private String notes;
    private Boolean isFinished;
    private Instant createdAt;
    private Instant updatedAt;

    public SessionResponse(Session session) {
        this.id = session.getId();
        this.creatorId = session.getCreatorId();
        this.name = session.getName();
        this.sessionType = session.getType();
        this.isRandom = session.getIsRandom();
        this.notes = session.getNotes();
        this.isFinished = session.getIsFinished();
        this.createdAt = session.getCreatedAt();
        this.updatedAt = session.getUpdatedAt();
    }
}
