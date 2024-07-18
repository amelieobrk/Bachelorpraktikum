package de.kreuzenonline.kreuzen.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.Instant;
import java.util.Date;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser withMockCustomUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        CustomUserDetails principal = new CustomUserDetails(
                withMockCustomUser.id(),
                withMockCustomUser.username(),
                withMockCustomUser.firstName(),
                withMockCustomUser.lastName(),
                withMockCustomUser.email(),
                PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(withMockCustomUser.password()),
                withMockCustomUser.role(),
                withMockCustomUser.universityId(),
                withMockCustomUser.emailConfirmed(),
                false,
                Instant.now(),
                Instant.now()
        );


        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                withMockCustomUser.password(),
                principal.getAuthorities()
        );
        context.setAuthentication(auth);

        SecurityContextHolder.getContext().setAuthentication(auth);
        return context;
    }
}