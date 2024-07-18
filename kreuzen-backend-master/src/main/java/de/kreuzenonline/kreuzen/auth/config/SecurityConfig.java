package de.kreuzenonline.kreuzen.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzenonline.kreuzen.auth.CustomUserDetailsService;
import de.kreuzenonline.kreuzen.auth.filter.AuthenticationFilter;
import de.kreuzenonline.kreuzen.auth.filter.AuthorizationFilter;
import de.kreuzenonline.kreuzen.auth.handler.RestAccessDeniedHandler;
import de.kreuzenonline.kreuzen.auth.handler.RestAuthenticationEntryPoint;
import de.kreuzenonline.kreuzen.auth.provider.UserIdAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // Urls that can be accessed by everyone with GET requests.
    private static final String[] PUBLIC_URL = {
            "/ping",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/v2/api-docs/**",
            "/auth/pre-register",
            "/university/*/major",
            "/major/*",
            "/major/*/section"
    };
    private final UserIdAuthenticationProvider userIdAuthenticationProvider;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final ResourceBundle resourceBundle;
    private final ObjectMapper mapper;

    public SecurityConfig(CustomUserDetailsService userDetailsService, UserIdAuthenticationProvider userIdAuthenticationProvider, RestAccessDeniedHandler restAccessDeniedHandler, RestAuthenticationEntryPoint restAuthenticationEntryPoint, PasswordEncoder passwordEncoder, ResourceBundle resourceBundle, ObjectMapper mapper) {
        this.userDetailsService = userDetailsService;
        this.userIdAuthenticationProvider = userIdAuthenticationProvider;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.passwordEncoder = passwordEncoder;
        this.resourceBundle = resourceBundle;
        this.mapper = mapper;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and() // Our Application may run on different domains therefore a custom cors config is enabled
                .csrf().disable() // CSRF is only used for HTML Forms, which we are not using
                .authorizeRequests() // Perform authorization pipeline

                // Allow public access on selected urls
                .antMatchers(PUBLIC_URL).permitAll()
                .antMatchers(HttpMethod.POST,
                        "/auth/register",
                        "/auth/login",
                        "/auth/request-pw-reset",
                        "/auth/confirm-pw-reset",
                        "/auth/confirm-email"
                ).permitAll()

                // Require auth on all other domains
                .anyRequest().authenticated()

                .and().exceptionHandling()
                .accessDeniedHandler(restAccessDeniedHandler)
                .authenticationEntryPoint(restAuthenticationEntryPoint)

                .and().addFilter(new AuthorizationFilter(authenticationManager(), userDetailsService))
                .addFilter(new AuthenticationFilter(authenticationManager(), userDetailsService, resourceBundle, mapper))
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(userIdAuthenticationProvider)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setExposedHeaders(Collections.singletonList("Authorization"));

        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001", "http://37.120.161.182"));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
