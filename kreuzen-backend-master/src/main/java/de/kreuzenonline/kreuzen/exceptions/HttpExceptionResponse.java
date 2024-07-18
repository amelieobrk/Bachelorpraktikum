package de.kreuzenonline.kreuzen.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpExceptionResponse {
    private String msg;
    private Instant time;
    private Integer status;
}
