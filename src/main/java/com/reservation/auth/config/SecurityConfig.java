package com.reservation.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.auth.api.AuthDTO.LoginResponse;
import com.reservation.common.exception.ErrorCode;
import com.reservation.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AuthenticationManager authenticationManager,
                                            ObjectMapper objectMapper,
                                            Validator validator,
                                            SecurityContextRepository securityContextRepository) throws Exception {
        LoginAuthenticationFilter loginFilter =
                new LoginAuthenticationFilter(
                        authenticationManager,
                        objectMapper,
                        validator
                );

        loginFilter.setSecurityContextRepository(securityContextRepository);
        loginFilter.setSessionAuthenticationStrategy(new SessionFixationProtectionStrategy());
        loginFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            response.setStatus(200);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(), new LoginResponse(authentication.getName()));
        });
        loginFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            ErrorCode errorCode = exception instanceof AuthenticationServiceException
                    ? ErrorCode.INVALID_REQUEST
                    : ErrorCode.INVALID_CREDENTIALS;

            response.setStatus(errorCode.getStatus().value());
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(), ErrorResponse.of(errorCode));
        });

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/v1/auth/signup", "/api/v1/auth/login")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                )
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository)
                        .requireExplicitSave(true)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()
                        .anyRequest().authenticated()
                ).addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        })
                );
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}
