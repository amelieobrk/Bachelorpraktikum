package de.kreuzenonline.kreuzen.session.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SetSelectionRequest {

    @NotNull(message = "SetSelectionRequest-type-not-null")
    private String type;

    private Integer[] checkedLocalAnswerIds;
    private Integer[] crossedLocalAnswerIds;
    private Integer checkedLocalAnswerId;
}
