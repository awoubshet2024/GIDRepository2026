package com.alem.GIA.controller;

import com.alem.GIA.DTO.BeneficiaryDto;
import com.alem.GIA.DTO.MemberDto;
import com.alem.GIA.entity.Beneficiary;
import com.alem.GIA.entity.Member;
import com.alem.GIA.service.BeneficiaryService;
import com.alem.GIA.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final MemberService memberService;

    @GetMapping("/{memberId}/get")
    public List<BeneficiaryDto> getBeneficiaries(@PathVariable Integer memberId){


      Member member =   memberService.findMemberById(memberId).orElseThrow();

        return beneficiaryService.getByMember(member);
    }

    @PostMapping("/{memberId}/saveAll")
    public List<Beneficiary> saveAllBeneficiaries(
            @PathVariable Integer memberId,
            @RequestBody List<Beneficiary> beneficiaries){

        Member member = memberService.findMemberById(memberId).orElseThrow();

        return beneficiaryService.saveAll(member, beneficiaries);
    }

}

