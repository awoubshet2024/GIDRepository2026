package com.alem.GIA.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.OneToOne;
import jakarta.persistence.*;
import lombok.*;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String streetName;
    private String city;
    private String state;
    private String zipCode;

    @OneToOne(mappedBy = "address")
   // @JsonBackReference
    private Member member;
    @OneToOne(mappedBy = "address")
    @JsonIgnore
    private Beneficiary beneficiary;

}