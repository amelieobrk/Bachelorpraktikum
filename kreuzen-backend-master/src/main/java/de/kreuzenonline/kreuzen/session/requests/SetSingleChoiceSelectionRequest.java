package de.kreuzenonline.kreuzen.session.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SetSingleChoiceSelectionRequest extends SetSelectionRequest {

    private String type;
    private Integer checkedLocalAnswerId;
    private Integer[] crossesLocalAnswerId;
}
