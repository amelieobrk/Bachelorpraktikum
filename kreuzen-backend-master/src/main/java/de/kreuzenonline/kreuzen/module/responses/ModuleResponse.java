package de.kreuzenonline.kreuzen.module.responses;


import de.kreuzenonline.kreuzen.module.Module;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleResponse {

    // Code Review JP: Würde als Typen hier auch Integer/Boolean statt int/boolean nehmen
    private int id;
    private String name;
    private int universityId;
    private boolean isUniversityWide;

    public ModuleResponse(Module module) {
        this.id = module.getId();
        this.name = module.getName();
        this.universityId = module.getUniversityId();
        this.isUniversityWide = module.getIsUniversityWide();

    }


}




