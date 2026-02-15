package com.alem.GIA.model;

import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class MemberRequest {

    private MemberModel member;

}
