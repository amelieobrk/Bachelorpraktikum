package de.kreuzenonline.kreuzen.hint.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateHintRequest {

    @NotNull(message = "CreateHintRequest-text-not-null")
    @Size(max = 1000, message = "CreateHintRequest-text-too-long")
    private String text;

    @NotNull(message = "CreateHintRequest-isActive-not-null")
    private Boolean isActive;
}
