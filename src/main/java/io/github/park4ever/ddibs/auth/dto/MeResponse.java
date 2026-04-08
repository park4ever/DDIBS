package io.github.park4ever.ddibs.auth.dto;

import io.github.park4ever.ddibs.auth.security.MemberPrincipal;
import io.github.park4ever.ddibs.member.domain.Role;

public record MeResponse(
        Long id,
        String email,
        String name,
        Role role
) {
    public static MeResponse from(MemberPrincipal principal) {
        return new MeResponse(
                principal.getId(),
                principal.getEmail(),
                principal.getName(),
                principal.getRole()
        );
    }
}
