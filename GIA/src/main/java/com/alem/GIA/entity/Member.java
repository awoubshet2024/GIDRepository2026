package com.alem.GIA.entity;

import com.alem.GIA.enumes.MaritalStatus;
import com.alem.GIA.permission.ApplicationUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles"})
public class Member {

    @Id
    @EqualsAndHashCode.Include
    @SequenceGenerator(
            name = "member_sequence",
            sequenceName = "member_sequence",
            allocationSize = 10
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "member_sequence"
    )
    private Integer memberId;

    private String firstName;
    private String lastName;
    @Column(unique = true,nullable = false)
    private String email;
    private String gender;
    private String phone;

    private LocalDate dateOfBirth;
    private LocalDate dateOfReg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaritalStatus maritalStatus;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "photo", columnDefinition = "BYTEA")
    @JsonIgnore
    private byte[] photo;

    private String imageName;
    private String imageType;

    @OneToMany(
            mappedBy = "member",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @JsonManagedReference
    private Set<Dependent> dependents = new HashSet<>();

    @OneToMany(
            mappedBy = "member",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private Set<Payment> payments = new HashSet<>();
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnore
    private ApplicationUser user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

//    public void setAddress(Address address) {
//        this.address = address;
//        if(address != null){
//            address.setMember(this);
//        }
//    }
    @OneToMany(
            mappedBy = "member",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @JsonManagedReference
    private Set<Beneficiary> beneficiaries = new HashSet<>();

    public void addBeneficiary(Beneficiary beneficiary) {

        beneficiaries.add(beneficiary);
        beneficiary.setMember(this);

    }
    public void removeBeneficiary(Beneficiary beneficiary) {

        beneficiaries.remove(beneficiary);
        beneficiary.setMember(null);

    }
    public void addDependent(Dependent dependent) {

        dependents.add(dependent);
        dependent.setMember(this);

    }

    public void removeDependent(Dependent dependent) {

        dependents.remove(dependent);
        dependent.setMember(null);

    }
    public void addPayment(Payment payment) {

        payments.add(payment);
        payment.setMember(this);

    }

    public void removePayment(Payment payment) {

        payments.remove(payment);
        payment.setMember(null);

    }
}