package com.reservation.auth.application;

import com.reservation.auth.api.AuthDTO.LoginRequest;
import com.reservation.auth.api.AuthDTO.SignUpRequest;
import com.reservation.common.exception.BusinessException;
import com.reservation.common.exception.ErrorCode;
import com.reservation.member.domain.Member;
import com.reservation.member.domain.MemberRepository;
import com.reservation.member.domain.MemberRole;
import com.reservation.member.api.MemberDTO.MemberResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        try {
            Member member = memberRepository.save(new Member(
                    email,
                    passwordEncoder.encode(request.password()),
                    request.name().trim(),
                    MemberRole.MEMBER
            ));
            return new MemberResponse(member.getId(), member.getEmail(), member.getName(), member.getRole().name());
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    public org.springframework.security.core.Authentication login(LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email().trim().toLowerCase(java.util.Locale.ROOT),
                            request.password()
                    )
            );
            return authentication;
        } catch (BadCredentialsException exception) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
