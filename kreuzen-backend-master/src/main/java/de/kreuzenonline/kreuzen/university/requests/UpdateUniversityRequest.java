package de.kreuzenonline.kreuzen.university.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateUniversityRequest {

    @Size(min = 3, message = "UpdateUniversityRequest-name-too-short")
    @Size(max = 64, message = "UpdateUniversityRequest-name-too-long")
    private String name;

    private String[] allowedDomains;
}
