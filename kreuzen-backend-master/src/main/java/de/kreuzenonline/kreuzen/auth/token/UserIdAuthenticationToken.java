package de.kreuzenonline.kreuzen.auth.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.ArrayList;

/**
 * Token used for JWT auth.
 */
public class UserIdAuthenticationToken extends AbstractAuthenticationToken {

    private final Integer userId;

    public UserIdAuthenticationToken(Integer userId) {
        super(new ArrayList<>());
        this.userId = userId;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Integer getPrincipal() {
        return userId;
    }
}
