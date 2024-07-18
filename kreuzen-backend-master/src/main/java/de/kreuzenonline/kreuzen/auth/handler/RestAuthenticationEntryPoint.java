package de.kreuzenonline.kreuzen.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.exceptions.HttpExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ResourceBundle;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ResourceBundle resourceBundle;
    private final ObjectMapper mapper;


    public RestAuthenticationEntryPoint(ResourceBundle resourceBundle, ObjectMapper mapper) {
        this.resourceBundle = resourceBundle;
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {

        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.getWriter().write(mapper.writeValueAsString(new HttpExceptionResponse(
                resourceBundle.getString("unauthorized"),
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value()
        )));
    }
}
