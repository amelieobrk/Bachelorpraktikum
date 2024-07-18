package de.kreuzenonline.kreuzen.section.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateSectionRequest {

    @NotNull(message = "UpdateSectionRequest-name-not-null")
    @Size(min = 3, message = "UpdateSectionRequest-name-too-short")
    @Size(max = 64, message = "UpdateSectionRequest-name-too-long")
    private String name;
}
