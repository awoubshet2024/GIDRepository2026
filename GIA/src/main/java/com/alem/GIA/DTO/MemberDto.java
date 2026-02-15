package com.alem.GIA.DTO;

import com.alem.GIA.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;
import java.util.Date;
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
    private Date dateOfBirth;
    private Date dateOfReg;

    private String imageName;
    private String imageType;
    private String photoBase64; // Flattened base64 image
    // private byte[] photo;
    private List<DependentDto> dependents;
    private List<PaymentDto> payments;

    public MemberDto(Member member) {
        this.memberId = member.getMemberId();
        this.firstName = member.getFirstName();
        this.lastName = member.getLastName();
        this.email = member.getEmail();
        this.gender = member.getGender();
        this.dateOfBirth = member.getDateOfBirth();
        this.dateOfReg = member.getDateOfReg();

        this.imageName = member.getImageName();
        this.imageType = member.getImageType();

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
    }
}

