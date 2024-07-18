package de.kreuzenonline.kreuzen.auth.filter;

import de.kreuzenonline.kreuzen.auth.CustomUserDetailsService;
import de.kreuzenonline.kreuzen.auth.token.UserIdAuthenticationToken;
import de.kreuzenonline.kreuzen.exceptions.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Filter to extract jwt information from requests which can be used for auth.
 */
public class AuthorizationFilter extends BasicAuthenticationFilter {

    private final CustomUserDetailsService userDetailsService;

    public AuthorizationFilter(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService) {
        super(authenticationManager);
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filter which checks for a jwt in the header. If the authorization header is present and starts with "Bearer"
     * then the token is extracted and added to the context as a UserIdAuthenticationToken.
     *
     * @param request  Http request
     * @param response Http response
     * @param chain    FilterChain
     * @throws IOException      thrown by chain.doFilter
     * @throws ServletException thrown by chain.doFilter
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer")) {
            chain.doFilter(request, response);
            return;
        }
        UserIdAuthenticationToken authToken = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authToken);
        chain.doFilter(request, response);
    }

    /**
     * Extracts jwt from authorization header, reads the user id and then returns a UserIdAuthenticationToken.
     * If the token is not valid a runtime exception is thrown resulting in an auth error.
     *
     * @param request Http request with authorization header
     * @return UserId Auth Token
     */
    private UserIdAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasLength(token)) {
            try {
                String userId = userDetailsService.getJwtClaims(token)
                        .getBody()
                        .getSubject();

                if (StringUtils.hasLength(userId)) {

                    Integer uid = Integer.valueOf(userId);
                    return new UserIdAuthenticationToken(uid);
                }
            } catch (JwtException e) {
                return null;
            }
        }
        return null;
    }
}
