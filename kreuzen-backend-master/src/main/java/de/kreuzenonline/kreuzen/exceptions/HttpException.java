package de.kreuzenonline.kreuzen.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

/**
 * Base class for runtime exceptions which should be send to the user as status codes.
 * Specific exceptions should extend this class and set a status code in the super constructor.
 * The status is then send to the user along with the message.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class HttpException extends RuntimeException {

    protected final HttpStatus status;

    public HttpException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
