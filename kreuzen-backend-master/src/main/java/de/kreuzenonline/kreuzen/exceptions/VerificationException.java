package de.kreuzenonline.kreuzen.exceptions;

import org.springframework.http.HttpStatus;

public class VerificationException extends HttpException {
    public VerificationException(String message) {
        super(message, HttpStatus.NOT_ACCEPTABLE);
    }
}
