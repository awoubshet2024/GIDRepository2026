package com.alem.GIA.DTO;


import com.alem.GIA.entity.Beneficiary;
import com.alem.GIA.enumes.Relationship;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BeneficiaryDto {

    private Integer beneficiaryId;

    private String firstName;

    private String lastName;

    private Relationship relationship;

    private Double percentageShare;

    private AddressDto address;

    public BeneficiaryDto(Beneficiary beneficiary) {

        this.beneficiaryId = beneficiary.getBeneficiaryId();
        this.firstName = beneficiary.getFirstName();
        this.lastName = beneficiary.getLastName();
        this.relationship = beneficiary.getRelationship();
        this.percentageShare = beneficiary.getPercentageShare();

        if (beneficiary.getAddress() != null) {
            this.address = new AddressDto(beneficiary.getAddress());
        }
    }
}