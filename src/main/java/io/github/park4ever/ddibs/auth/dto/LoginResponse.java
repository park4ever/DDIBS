package io.github.park4ever.ddibs.auth.dto;

import io.github.park4ever.ddibs.member.domain.Member;
import io.github.park4ever.ddibs.member.domain.Role;

public record LoginResponse(
        Long id,
        String email,
        String name,
        Role role
) {
    public static LoginResponse from(Member member) {
        return new LoginResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole()
        );
    }
}
