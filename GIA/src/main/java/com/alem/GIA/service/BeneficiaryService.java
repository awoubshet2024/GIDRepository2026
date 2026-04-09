package com.alem.GIA.service;


import com.alem.GIA.DTO.AddressDto;
import com.alem.GIA.DTO.BeneficiaryDto;
import com.alem.GIA.DTO.MemberDto;
import com.alem.GIA.entity.Address;
import com.alem.GIA.entity.Beneficiary;
import com.alem.GIA.entity.Member;
import com.alem.GIA.repository.BeneficiaryRepository;
import com.alem.GIA.repository.FeeConfigRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;


    public Beneficiary save(Beneficiary beneficiary){

        return beneficiaryRepository.save(beneficiary);

    }
    @Transactional
    public List<BeneficiaryDto> getByMember(Member member){
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByMember(member);
        return beneficiaries
                .stream()
                .map(b ->{
                    BeneficiaryDto dto = new BeneficiaryDto();
                    dto.setBeneficiaryId(b.getBeneficiaryId());
                    dto.setFirstName(b.getFirstName());
                    dto.setLastName(b.getLastName());
                    dto.setRelationship(b.getRelationship());
                    dto.setPercentageShare(b.getPercentageShare());
                    dto.setAddress(b.getAddress() != null ? getAddressDto(b.getAddress()):null);
                    return dto;
                })
                .toList();

    }
    public AddressDto getAddressDto(Address address){
        return new AddressDto(address);
    }


    public List<Beneficiary> saveAll(Member member, List<Beneficiary> beneficiaries){

        double total = beneficiaries
                .stream()
                .mapToDouble(Beneficiary::getPercentageShare)
                .sum();

        if(Math.round(total) != 100){

            throw new RuntimeException(
                    "Total beneficiary percentage must equal 100%"
            );

        }

        beneficiaries.forEach(b -> b.setMember(member));

        return beneficiaryRepository.saveAll(beneficiaries);

    }
    public void validateBeneficiaryShares(Set<Beneficiary> beneficiaries){

        double total = beneficiaries
                .stream()
                .mapToDouble(Beneficiary::getPercentageShare)
                .sum();

        if(total != 100){
            throw new RuntimeException(
                    "Beneficiary percentage must equal 100%"
            );
        }
    }

}
