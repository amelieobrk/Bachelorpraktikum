package de.kreuzenonline.kreuzen.module.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateModuleRequest {

    @NotNull(message = "CreateModuleRequest-name-not-null")
    @Size(min = 3, message = "CreateModuleRequest-name-too-short")
    @Size(max = 64, message = "CreateModuleRequest-name-too-long")
    private String name;

    @NotNull(message = "CreateModuleRequest-universityId-not-null")
    private Integer universityId;

    @NotNull(message = "CreateModuleRequest-isUniversityWide-not-null")
    private Boolean isUniversityWide;
}



