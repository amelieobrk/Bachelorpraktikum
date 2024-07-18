package de.kreuzenonline.kreuzen.tag.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTagRequest {

    @NotNull(message = "CreateTagRequest-name-not-null")
    @Size(min = 3, message = "CreateTagRequest-name-too-short")
    @Size(max = 32, message = "CreateTagRequest-name-too-long")
    private String name;
}
