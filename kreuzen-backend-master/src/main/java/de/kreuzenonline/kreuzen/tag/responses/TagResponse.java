package de.kreuzenonline.kreuzen.tag.responses;

import de.kreuzenonline.kreuzen.tag.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagResponse {

    private Integer id;
    private String name;
    private Integer moduleId;

    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
        this.moduleId = tag.getModuleId();
    }

}
