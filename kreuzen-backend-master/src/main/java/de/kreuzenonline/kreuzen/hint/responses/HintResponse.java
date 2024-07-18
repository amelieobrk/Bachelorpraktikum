package de.kreuzenonline.kreuzen.hint.responses;

import de.kreuzenonline.kreuzen.hint.Hint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HintResponse {

    private Integer id;
    private String text;
    private Boolean isActive;

    public HintResponse(Hint hint) {
        this.id = hint.getId();
        this.text = hint.getText();
        this.isActive = hint.getIsActive();
    }

}
