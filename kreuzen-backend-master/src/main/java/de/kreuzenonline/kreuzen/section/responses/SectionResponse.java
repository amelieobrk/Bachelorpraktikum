package de.kreuzenonline.kreuzen.section.responses;

import de.kreuzenonline.kreuzen.section.Section;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionResponse {
    private Integer id;
    private Integer majorId;
    private String name;

    public SectionResponse(Section section) {
        this.id = section.getId();
        this.majorId = section.getMajorId();
        this.name = section.getName();
    }
}
