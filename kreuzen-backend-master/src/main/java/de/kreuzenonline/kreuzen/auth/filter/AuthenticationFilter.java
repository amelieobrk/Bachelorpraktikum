package de.kreuzenonline.kreuzen.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.auth.CustomUserDetailsService;
import de.kreuzenonline.kreuzen.auth.requests.LoginRequest;
import de.kreuzenonline.kreuzen.exceptions.HttpExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Filter used for login.
 * Upon successful authentication a jwt is sent to the user.
 */
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final ResourceBundle resourceBundle;
    private final ObjectMapper mapper;

    public AuthenticationFilter(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, ResourceBundle resourceBundle, ObjectMapper mapper) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.resourceBundle = resourceBundle;
        this.mapper = mapper;
        setFilterProcessesUrl("/auth/login");
    }

    /**
     * Read request, extract username and password and let the authenticationManager perform further auth.
     *
     * @param request  Login Request
     * @param response Server Response
     * @return Authentication
     * @throws AuthenticationException Exception for invalid credentials.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword(),
                            new ArrayList<>()
                    )
            );
        } catch (IOException e) {
            throw new AuthenticationServiceException(resourceBundle.getString("cant-read-body"), e);
        }
    }

    /**
     * Generate a jwt for the user and return it with the response.
     *
     * @param request    Login Request
     * @param response   Login Response
     * @param chain      Auth Chain
     * @param authResult Auth Result
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        String token = userDetailsService.generateJwt((CustomUserDetails) authResult.getPrincipal());
        response.addHeader("Authorization", "Bearer " + token);
    }

    /**
     * Handle failed authentication.
     *
     * @param request  Login Request
     * @param response Error Response
     * @param failed   Exception why auth failed
     * @throws IOException Error on writing response
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {

        if (failed instanceof DisabledException) {
            // User not activated yet
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(mapper.writeValueAsString(new HttpExceptionResponse(
                    resourceBundle.getString("Login-disabled"),
                    Instant.now(),
                    HttpStatus.UNAUTHORIZED.value()
            )));
        } else if (failed instanceof BadCredentialsException) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(mapper.writeValueAsString(new HttpExceptionResponse(
                    resourceBundle.getString("Login-bad-credentials"),
                    Instant.now(),
                    HttpStatus.UNAUTHORIZED.value()
            )));
        } else if (failed instanceof LockedException) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(mapper.writeValueAsString(new HttpExceptionResponse(
                    resourceBundle.getString("Login-locked"),
                    Instant.now(),
                    HttpStatus.UNAUTHORIZED.value()
            )));
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(mapper.writeValueAsString(new HttpExceptionResponse(
                    resourceBundle.getString("Login-bad-credentials"),
                    Instant.now(),
                    HttpStatus.UNAUTHORIZED.value()
            )));
        }
    }
}
