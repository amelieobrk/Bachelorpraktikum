package de.kreuzenonline.kreuzen.auth;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    int id() default 1;

    String username() default "t";

    String firstName() default "f";

    String lastName() default "l";

    String email() default "t@uni.de";

    String password() default "p";

    String role() default "USER";

    boolean emailConfirmed() default true;

    int universityId() default 1;
}
