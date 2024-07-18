package de.kreuzenonline.kreuzen.session.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateSessionRequest {

    @NotNull(message = "CreateSessionRequest-name-not-null")
    @Size(min = 3, message = "CreateSessionRequest-name-too-short")
    @Size(max = 64, message = "CreateSessionRequest-name-too-long")
    private String name;

    @NotNull(message = "CreateSessionRequest-sessionType-not-null")
    private String sessionType;

    @NotNull(message = "CreateSessionRequest-isRandom-not-null")
    private Boolean isRandom;

    @Size(max = 1000, message = "CreateSessionRequest-notes-too-long")
    private String notes;

    private Integer[] moduleIds;
    private Integer[] semesterIds;
    private Integer[] tagIds;
    private String[] questionTypes;
    private String[] questionOrigins;
    private String filterTerm;
}
