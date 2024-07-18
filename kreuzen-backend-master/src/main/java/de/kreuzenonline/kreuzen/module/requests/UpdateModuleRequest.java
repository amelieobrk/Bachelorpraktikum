package de.kreuzenonline.kreuzen.module.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateModuleRequest {


    @Size(min = 3, message = "CreateModuleRequest-name-too-short")
    @Size(max = 64, message = "CreateModuleRequest-name-too-long")
    private String name;


    private Integer universityId;

    private Boolean isUniversityWide;
}

