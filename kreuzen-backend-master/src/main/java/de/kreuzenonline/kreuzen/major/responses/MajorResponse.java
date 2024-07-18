package de.kreuzenonline.kreuzen.major.responses;

import de.kreuzenonline.kreuzen.major.Major;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MajorResponse {

    private Integer id;
    private String name;
    private Integer universityId;

    public MajorResponse(Major major) {
        this.id = major.getId();
        this.name = major.getName();
        this.universityId = major.getUniversityId();
    }
}
