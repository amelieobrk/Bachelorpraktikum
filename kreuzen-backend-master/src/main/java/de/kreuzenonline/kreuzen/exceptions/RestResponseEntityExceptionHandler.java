package de.kreuzenonline.kreuzen.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.ResourceBundle;

@ControllerAdvice
public class RestResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {

    private final ResourceBundle resourceBundle;

    public RestResponseEntityExceptionHandler(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Handles HttpExceptions by extracting their message and status. This information is then send to the user.
     *
     * @param ex      Exception that occurred
     * @param request Request that triggered the exception
     * @return Response to the user
     */
    @ExceptionHandler(HttpException.class)
    protected ResponseEntity<Object> handleConflict(HttpException ex, WebRequest request) {

        HttpExceptionResponse response = new HttpExceptionResponse(
                ex.getMessage(),
                Instant.now(),
                ex.getStatus().value()
        );

        return handleExceptionInternal(ex, response, new HttpHeaders(), ex.getStatus(), request);
    }

    /**
     * This overwrites the default behavior in case of a invalid method argument (mostly triggered through bad input).
     * The goal is to show the exceptions in the same format as {@link HttpException}.
     *
     * @param ex      Exception
     * @param headers Headers
     * @param status  Status
     * @param request Request of the user
     * @return Response
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request
    ) {

        StringBuilder message = new StringBuilder();

        boolean isFirst = true;
        for (FieldError error : ex.getFieldErrors()) {
            if (error.getDefaultMessage() != null && resourceBundle.containsKey(error.getDefaultMessage())) {
                message.append(resourceBundle.getString(error.getDefaultMessage()));
            }
            if (!isFirst) {
                message.append("\n");
            }
            isFirst = false;
        }

        HttpExceptionResponse response = new HttpExceptionResponse(
                message.toString(),
                Instant.now(),
                HttpStatus.BAD_REQUEST.value()
        );

        return handleExceptionInternal(ex, response, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}
