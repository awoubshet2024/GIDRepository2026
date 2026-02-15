package com.alem.GIA.model;

import com.alem.GIA.DTO.MemberDto;
import com.alem.GIA.entity.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class MemberResponse {

    String message;
    Boolean result;
    MemberDto member;


}
