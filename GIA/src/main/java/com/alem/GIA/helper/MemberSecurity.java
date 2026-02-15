package com.alem.GIA.helper;

import com.alem.GIA.entity.Member;
import com.alem.GIA.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class MemberSecurity {
    private final MemberRepository memberRepository;

    public MemberSecurity(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public boolean isOwner(Integer memberId, Authentication auth) {
        Member member = memberRepository.findById(memberId).orElse(null);
        return member != null &&
                member.getUser().getUserName().equals(auth.getName());
    }
}
