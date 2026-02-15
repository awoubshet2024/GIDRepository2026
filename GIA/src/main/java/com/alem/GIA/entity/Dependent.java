package com.alem.GIA.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Dependent {
    @Id
    private Integer dependentId;
    private String firstName;
    private String lastName;
    private String gender;
    private Date dateOfBirth;
    @JsonBackReference
    @ManyToOne
    private Member member;



}
