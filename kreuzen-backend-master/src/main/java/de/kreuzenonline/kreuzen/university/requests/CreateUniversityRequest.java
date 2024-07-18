package de.kreuzenonline.kreuzen.university.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateUniversityRequest {

    @NotNull(message = "CreateUniversityRequest-name-not-null")
    @Size(min = 3, message = "CreateUniversityRequest-name-too-short")
    @Size(max = 64, message = "CreateUniversityRequest-name-too-long")
    private String name;

    @NotNull(message = "CreateUniversityRequest-domains-not-null")
    private String[] allowedDomains;
}
