package de.kreuzenonline.kreuzen.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.exceptions.HttpExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ResourceBundle;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ResourceBundle resourceBundle;
    private final ObjectMapper mapper;

    public RestAccessDeniedHandler(ResourceBundle resourceBundle, ObjectMapper mapper) {
        this.resourceBundle = resourceBundle;
        this.mapper = mapper;
    }

    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException {

        httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
        httpServletResponse.getWriter().write(mapper.writeValueAsString(new HttpExceptionResponse(
                resourceBundle.getString("insufficient-permissions"),
                Instant.now(),
                HttpStatus.FORBIDDEN.value()
        )));

    }
}
