package de.kreuzenonline.kreuzen.auth.provider;

import de.kreuzenonline.kreuzen.auth.CustomUserDetails;
import de.kreuzenonline.kreuzen.auth.CustomUserDetailsService;
import de.kreuzenonline.kreuzen.auth.token.UserIdAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Authenticator to provide user details for jwt auth.
 */
@Component
public class UserIdAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;

    public UserIdAuthenticationProvider(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Loads user from database if the authentication is a UserIdAuthenticationToken as it is already confirmed
     * by the previous filter.
     *
     * @param authentication Authentication Context
     * @return Authentication Context
     */
    @Override
    public Authentication authenticate(Authentication authentication) {

        if (authentication instanceof UserIdAuthenticationToken) {

            UserIdAuthenticationToken userIdAuth = (UserIdAuthenticationToken) authentication;
            CustomUserDetails userDetails = userDetailsService.loadUserById(userIdAuth.getPrincipal());
            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UserIdAuthenticationToken.class);
    }
}
