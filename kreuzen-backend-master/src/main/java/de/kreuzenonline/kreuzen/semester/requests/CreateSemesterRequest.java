package de.kreuzenonline.kreuzen.semester.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSemesterRequest {
    @NotNull(message = "CreateSemesterRequest-name-not-null")
    @Size(min = 3, message = "CreateSemesterRequest-name-too-short")
    @Size(max = 64, message = "CreateSemesterRequest-name-too-long")
    private String name;

    @NotNull(message = "CreateSemesterRequest-startyear-not-null")
    @Min(value = 1950, message = "CreateSemesterRequest-startyear-too-low")
    @Max(value = 2050, message = "CreateSemesterRequest-startyear-too-high")
    private Integer startYear;

    @NotNull(message = "CreateSemesterRequest-endyear-not-null")
    @Min(value = 1950, message = "CreateSemesterRequest-endyear-too-low")
    @Max(value = 2050, message = "CreateSemesterRequest-endyear-too-high")
    private Integer endYear;
}
