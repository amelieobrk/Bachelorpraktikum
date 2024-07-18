package de.kreuzenonline.kreuzen.semester.responses;

import de.kreuzenonline.kreuzen.semester.Semester;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SemesterResponse {

    private Integer id;
    private String name;
    private Integer startYear;
    private Integer endYear;

    public SemesterResponse(Semester semester) {
        this.id = semester.getId();
        this.name = semester.getName();
        this.startYear = semester.getStartYear();
        this.endYear = semester.getEndYear();
    }
}
