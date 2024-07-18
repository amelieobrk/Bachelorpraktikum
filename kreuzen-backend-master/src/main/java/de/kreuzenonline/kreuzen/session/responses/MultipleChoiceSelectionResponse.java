package de.kreuzenonline.kreuzen.session.responses;

import de.kreuzenonline.kreuzen.session.selections.MultipleChoiceSelection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultipleChoiceSelectionResponse {

    private Integer localAnswerId;
    private Boolean isChecked;
    private Boolean isCrossed;

    public MultipleChoiceSelectionResponse(MultipleChoiceSelection selection) {
        this.localAnswerId = selection.getLocalAnswerId();
        this.isChecked = selection.getIsChecked();
        this.isCrossed = selection.getIsCrossed();
    }
}
