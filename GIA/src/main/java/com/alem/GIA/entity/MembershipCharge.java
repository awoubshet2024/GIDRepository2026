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
                        columnNames = {"member", "billingMonth"}
                )
        }
)
public class MembershipCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Member member;

    private LocalDate billingMonth;

    private BigDecimal amount;

    private String status; // PAID / UNPAID
}