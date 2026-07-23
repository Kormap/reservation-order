package com.reservation.auth.application;

import com.reservation.auth.api.AuthDTO.SignUpRequest;
import com.reservation.common.exception.BusinessException;
import com.reservation.common.exception.ErrorCode;
import com.reservation.member.api.MemberDTO.MemberResponse;
import com.reservation.member.domain.Member;
import com.reservation.member.domain.MemberRepository;
import com.reservation.member.domain.MemberRole;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
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
}
