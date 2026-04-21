package de.calucon.esi.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import de.calucon.esi.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (Cross-Site Request Forgery) because we are using stateless
                // JWTs
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Set up our routing rules
                .authorizeHttpRequests(auth -> auth
                        // Make login and register endpoints completely public
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // OpenAPI endpoints
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()
                        // Any other request must be authenticated with a valid JWT
                        .anyRequest().authenticated())

                // 3. Make the session stateless (Spring shouldn't store session data, the JWT
                // holds everything)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Plug in the provider we created in ApplicationConfig
                .authenticationProvider(authenticationProvider)

                // 5. Insert our custom JWT Filter BEFORE the standard Username/Password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
