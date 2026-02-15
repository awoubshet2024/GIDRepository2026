package com.alem.GIA.entity;

import com.alem.GIA.permission.ApplicationUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles"})
public class Member {
    @SequenceGenerator(
            name="member_sequence",
            sequenceName = "member_sequence",
            allocationSize=10

    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "member_sequence"
    )
    @Id
    private Integer memberId;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private Date dateOfBirth;
    private Date dateOfReg;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] photo;

    private String imageName;
    private String imageType;

    @OneToMany( cascade = CascadeType.ALL,
            targetEntity = Dependent.class,
            fetch = FetchType.EAGER,orphanRemoval = true)
    @JoinColumn(name="memberId",referencedColumnName = "memberId")
    @JsonManagedReference
    private List<Dependent> dependents = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL,targetEntity = Payment.class,fetch = FetchType.EAGER)
    @JoinColumn(name="memberId",referencedColumnName = "memberId")
    @JsonManagedReference
    private List<Payment> payments = new ArrayList<>();


    @OneToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "user_id", unique = true)
    private ApplicationUser user;




}
