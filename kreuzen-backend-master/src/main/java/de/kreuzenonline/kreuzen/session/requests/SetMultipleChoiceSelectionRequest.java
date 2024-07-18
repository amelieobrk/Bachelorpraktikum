package de.kreuzenonline.kreuzen.session.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SetMultipleChoiceSelectionRequest extends SetSelectionRequest {

    private String type;
    private Integer[] checkedLocalAnswerIds;
    private Integer[] crossedLocalAnswerIds;
}
