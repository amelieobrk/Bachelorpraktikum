package de.kreuzenonline.kreuzen.major.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMajorRequest {

    @NotNull(message = "CreateMajorRequest-name-not-null")
    @Size(min = 3, message = "CreateMajorRequest-name-too-short")
    @Size(max = 64, message = "CreateMajorRequest-name-too-long")
    private String name;
}
