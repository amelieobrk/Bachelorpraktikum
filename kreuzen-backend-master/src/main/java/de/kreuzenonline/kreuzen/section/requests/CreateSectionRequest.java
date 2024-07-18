package de.kreuzenonline.kreuzen.section.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSectionRequest {
    @NotNull(message = "CreateSectionRequest-name-not-null")
    @Size(min = 3, message = "CreateSectionRequest-name-too-short")
    @Size(max = 64, message = "CreateSectionRequest-name-too-long")
    private String name;

}
