package de.kreuzenonline.kreuzen.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@AllArgsConstructor
@Table("session")
public class Session {

    @Id
    private Integer id;
    private Integer creatorId;
    private String notes;
    private String type;
    private String name;
    private Boolean isRandom;
    private Boolean isFinished;
    private Instant createdAt;
    private Instant updatedAt;

    public Session() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Session(Integer id, Integer creatorId, String notes, String type, String name, Boolean isRandom, Boolean isFinished) {
        this.id = id;
        this.creatorId = creatorId;
        this.notes = notes;
        this.type = type;
        this.name = name;
        this.isRandom = isRandom;
        this.isFinished = isFinished;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
