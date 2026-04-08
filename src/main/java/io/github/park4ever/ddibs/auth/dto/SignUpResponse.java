package io.github.park4ever.ddibs.auth.dto;

import io.github.park4ever.ddibs.member.domain.Member;
import io.github.park4ever.ddibs.member.domain.Role;

public record SignUpResponse(
        Long id,
        String email,
        String name,
        Role role
) {
    public static SignUpResponse from(Member member) {
        return new SignUpResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole()
        );
    }
}
