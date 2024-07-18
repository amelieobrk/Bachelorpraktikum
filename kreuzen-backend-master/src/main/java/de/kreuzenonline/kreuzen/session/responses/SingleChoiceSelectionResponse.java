package de.kreuzenonline.kreuzen.session.responses;

import de.kreuzenonline.kreuzen.session.selections.SingleChoiceSelection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleChoiceSelectionResponse {

    private Integer localAnswerId;
    private Boolean isChecked;
    private Boolean isCrossed;

    public SingleChoiceSelectionResponse(SingleChoiceSelection selection) {
        this.localAnswerId = selection.getLocalAnswerId();
        this.isChecked = selection.getIsChecked();
        this.isCrossed = selection.getIsCrossed();
    }

}
