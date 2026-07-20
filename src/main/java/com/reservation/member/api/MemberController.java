package com.reservation.member.api;

import com.reservation.member.api.MemberDTO.MemberResponse;
import com.reservation.member.application.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/me")
    public MemberResponse getMe(Authentication authentication) {
        return memberService.getMe(authentication.getName());
    }
}
