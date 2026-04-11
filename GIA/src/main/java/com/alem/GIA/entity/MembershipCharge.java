package com.alem.GIA.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_month",
                        columnNames = "member_id", "billing_month"}
                )
        }
)
public class MembershipCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;
    @Column(name="billing_maonth")
    private LocalDate billingMonth;

    private BigDecimal amount;

    private String status; // PAID / UNPAID
}