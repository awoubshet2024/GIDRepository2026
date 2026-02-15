package com.alem.GIA.DTO;

import com.alem.GIA.entity.Dependent;
import com.alem.GIA.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DependentDto {
    private Integer dependentId;
    private Integer memberId;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String gender;
    public DependentDto(Dependent dependent){
        this.firstName = dependent.getFirstName();
        this.lastName = dependent.getLastName();
        this.dateOfBirth = dependent.getDateOfBirth();
        this.gender = dependent.getGender();

    }

}
