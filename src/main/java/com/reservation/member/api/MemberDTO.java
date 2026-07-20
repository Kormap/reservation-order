package com.reservation.member.api;

public final class MemberDTO {

    private MemberDTO() {
    }

    public record MemberResponse(Long id, String email, String name, String role) {
    }
}
