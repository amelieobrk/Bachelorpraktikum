package de.kreuzenonline.kreuzen.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configures the password encoder for the application.
 * The DelegatingPasswordEncoder is used to be flexible with the encoding algorithm as this information is stored
 * with the password in the db. Therefore the algorithm can be changed at any point without locking users out.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder encoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
