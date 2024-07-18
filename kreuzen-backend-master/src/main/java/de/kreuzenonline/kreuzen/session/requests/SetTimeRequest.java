package de.kreuzenonline.kreuzen.session.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetTimeRequest {

    @NotNull(message = "SetTimeRequest-time-not-null")
    @Min(value = 0, message = "SetTimeRequest-time-not-negative")
    private Integer time;
}
