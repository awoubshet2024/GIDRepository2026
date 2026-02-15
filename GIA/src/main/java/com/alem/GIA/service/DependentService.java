package com.alem.GIA.service;

import com.alem.GIA.DTO.DependentDto;
import com.alem.GIA.entity.Dependent;
import com.alem.GIA.entity.Member;
import com.alem.GIA.repository.DependentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DependentService {
    private final DependentRepository dependentRepository;
    private final MemberService memberService;

    public DependentService(DependentRepository dependentRepository, MemberService memberService) {
        this.dependentRepository = dependentRepository;
        this.memberService = memberService;
    }
    public List<Dependent> getAlldependents(){
        return dependentRepository.findAll();
    }

    public String addDependentToMember(DependentDto dto) {
        Optional<Member> member = memberService.findMemberById(dto.getMemberId());
        if(member.isPresent()){
            Dependent dependent = new Dependent();
            dependent.setDependentId(dto.getDependentId());
            dependent.setFirstName(dto.getFirstName());
            dependent.setLastName(dto.getLastName());
            dependent.setGender(dto.getGender());
            dependent.setDateOfBirth(dto.getDateOfBirth());
            member.get().getDependents().add(dependent);
            memberService.saveMember(member.get());
            member.get().getDependents().forEach(d -> {
                System.out.printf("Dependent Id: %d%n" +
                                "First Name: %s%n" +
                                "Last Name: %s%n " +
                                "Gender: %s%n" +
                                "Date of birth: %s",
                        d.getDependentId(),
                        d.getFirstName(),
                        d.getLastName(),
                        d.getGender(),
                        d.getDateOfBirth());
            });
            return "Dependent is added";

        }

        return "Dependent is not added";
    }
}
