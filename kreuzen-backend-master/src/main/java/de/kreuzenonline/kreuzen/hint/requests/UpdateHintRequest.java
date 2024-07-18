package de.kreuzenonline.kreuzen.hint.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateHintRequest {

    private String text;

    private Boolean isActive;
}
