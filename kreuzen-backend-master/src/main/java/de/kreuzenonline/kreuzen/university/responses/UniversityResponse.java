package de.kreuzenonline.kreuzen.university.responses;

import de.kreuzenonline.kreuzen.university.University;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniversityResponse {

    private int id;
    private String name;
    private String[] allowedMailDomains;

    public UniversityResponse(University university) {
        this.id = university.getId();
        this.name = university.getName();
        this.allowedMailDomains = university.getAllowedMailDomains();
    }
}
