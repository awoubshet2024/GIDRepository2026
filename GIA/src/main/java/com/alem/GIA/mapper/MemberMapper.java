package com.alem.GIA.mapper;

import com.alem.GIA.DTO.MemberDto;
import com.alem.GIA.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    public MemberDto toDto(Member member) {
        return MemberDto.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .build();
    }
}
