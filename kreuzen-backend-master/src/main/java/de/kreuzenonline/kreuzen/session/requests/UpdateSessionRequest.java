package de.kreuzenonline.kreuzen.session.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateSessionRequest {

    @Size(min = 3, message = "CreateSessionRequest-name-too-short")
    @Size(max = 64, message = "CreateSessionRequest-name-too-long")
    private String name;

    private String sessionType;

    private Boolean isRandom;

    @Size(max = 1000, message = "CreateSessionRequest-notes-too-long")
    private String notes;
}
