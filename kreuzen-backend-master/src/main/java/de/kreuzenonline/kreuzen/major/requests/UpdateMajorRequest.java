package de.kreuzenonline.kreuzen.major.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateMajorRequest {
    @NotNull(message = "UpdateMajorRequest-name-not-null")
    @Size(min = 3, message = "UpdateMajorRequest-name-too-short")
    @Size(max = 64, message = "UpdateMajorRequest-name-too-long")
    private String name;
}
