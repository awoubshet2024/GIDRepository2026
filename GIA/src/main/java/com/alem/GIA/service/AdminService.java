package com.alem.GIA.service;

import com.alem.GIA.DTO.MemberDto;
import com.alem.GIA.annotation.Auditable;
import com.alem.GIA.entity.Member;
import com.alem.GIA.exception.AccessDeniedException;
import com.alem.GIA.mapper.MemberMapper;
import com.alem.GIA.model.MemberResponse;
import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.repository.MemberRepository;
import com.alem.GIA.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MemberMapper memberMapper;

    public AdminService(MemberRepository memberRepository, UserRepository userRepository, MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.memberMapper = memberMapper;
    }
    @Transactional
    @Auditable(
            action = "ADMIN_MEMBER_REGISTER",
            entity = "Member",
            idField = "memberId",
            captureBefore = false,
            captureAfter = true
    )
    public MemberResponse adminMemberRegister(Member member) {

        ApplicationUser targetUser =
                userRepository.findByEmail(member.getEmail())
                        .orElse(null);

        if (targetUser != null && targetUser.getMember() != null) {
            return MemberResponse.builder()
                    .member(memberMapper.toDto(targetUser.getMember()))
                    .result(true)
                    .message("Member already exists for this user")
                    .build();
        }

        if (targetUser != null) {
            member.setUser(targetUser);
            member.setEmail(targetUser.getEmail());
            targetUser.setMember(member);   // ✅ bi-directional link
        }

        Member savedMember = memberRepository.save(member);

        return MemberResponse.builder()
                .member(memberMapper.toDto(savedMember))
                .result(true)
                .message("Member saved successfully")
                .build();
    }


}
