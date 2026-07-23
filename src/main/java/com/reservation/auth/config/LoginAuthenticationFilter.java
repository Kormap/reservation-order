package com.reservation.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.auth.api.AuthDTO.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

public class LoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public LoginAuthenticationFilter(
            AuthenticationManager authenticationManager,
            ObjectMapper objectMapper,
            Validator validator
    ) {
        super(PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, LOGIN_URL));

        setAuthenticationManager(authenticationManager);

        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        LoginRequest loginRequest = readLoginRequest(request);
        validate(loginRequest);

        UsernamePasswordAuthenticationToken authenticationToken =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequest.email().trim().toLowerCase(Locale.ROOT),
                        loginRequest.password()
                );

        return getAuthenticationManager().authenticate(authenticationToken);
    }

    private LoginRequest readLoginRequest(HttpServletRequest request) {
        validateJsonContentType(request);

        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            if (loginRequest == null) {
                throw new AuthenticationServiceException("로그인 요청 본문이 비어 있습니다.");
            }
            return loginRequest;
        } catch (IOException exception) {
            throw new AuthenticationServiceException("로그인 요청 본문을 읽을 수 없습니다.", exception);
        }
    }

    private void validateJsonContentType(HttpServletRequest request) {
        String contentType = request.getContentType();

        if (contentType == null
                || !MediaType.parseMediaType(contentType)
                .isCompatibleWith(MediaType.APPLICATION_JSON)) {
            throw new AuthenticationServiceException("Content-Type은 application/json이어야 합니다.");
        }
    }

    private void validate(LoginRequest loginRequest) {
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        if (!violations.isEmpty()) {
            throw new AuthenticationServiceException("이메일 또는 비밀번호 형식이 올바르지 않습니다.");
        }
    }
}
