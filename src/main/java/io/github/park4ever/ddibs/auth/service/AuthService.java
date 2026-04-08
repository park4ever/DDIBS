package io.github.park4ever.ddibs.auth.service;

import io.github.park4ever.ddibs.auth.dto.SignUpRequest;
import io.github.park4ever.ddibs.auth.dto.SignUpResponse;
import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.member.domain.Member;
import io.github.park4ever.ddibs.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        validateDuplicateEmail(request.email());

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.createUser(
                request.email(),
                encodedPassword,
                request.name()
        );

        Member savedMember = memberRepository.save(member);

        return SignUpResponse.from(savedMember);
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_EXISTS);
        }
    }
}
