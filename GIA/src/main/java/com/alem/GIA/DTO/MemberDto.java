package com.alem.GIA.DTO;

import com.alem.GIA.entity.Member;
import com.alem.GIA.enumes.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberDto {

    private Integer memberId;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private String phone;
    private LocalDate dateOfBirth;
    private LocalDate dateOfReg;
    private MaritalStatus maritalStatus;
    private byte[] photo;

    private String imageName;
    private String imageType;
    private String photoBase64; // Flattened base64 image
    private AddressDto address;
    private List<DependentDto> dependents;
    private List<PaymentDto> payments;
    private List<BeneficiaryDto> beneficiaries;


    public MemberDto(Member member) {
        this.memberId = member.getMemberId();
        this.firstName = member.getFirstName();
        this.lastName = member.getLastName();
        this.email = member.getEmail();
        this.gender = member.getGender();
        this.phone = member.getPhone();
        this.dateOfBirth = member.getDateOfBirth();
        this.dateOfReg = member.getDateOfReg();
        this.maritalStatus = member.getMaritalStatus();
        this.imageName = member.getImageName();
        this.imageType = member.getImageType();
        this.photo = member.getPhoto();
        if(member.getAddress() != null){
            this.address = new AddressDto(member.getAddress());

        }


        if (member.getPhoto() != null) {
            this.photoBase64 = Base64.getEncoder().encodeToString(member.getPhoto());
        }

        this.dependents = member.getDependents()
                .stream()
                .map(DependentDto::new)
                .collect(Collectors.toList());

        this.payments = member.getPayments()
                .stream()
                .map(PaymentDto::new)
                .collect(Collectors.toList());
        if(member.getBeneficiaries() != null){
            this.beneficiaries = member.getBeneficiaries()
                    .stream()
                    .map(BeneficiaryDto::new)
                    .toList();
        }

    }

}

