package com.alem.GIA.entity;

import com.alem.GIA.enumes.BillingPeriod;
import com.alem.GIA.enumes.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;
    @EqualsAndHashCode.Include
    private BigDecimal amount;
    @EqualsAndHashCode.Include
    private String reason;
    @EqualsAndHashCode.Include
    private LocalDate paymentDate;
    @EqualsAndHashCode.Include
    private String checkNumber;
    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    private BillingPeriod billingPeriod = BillingPeriod.MONTHLY;

    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    private PaymentMethod paymentMethod;
    @EqualsAndHashCode.Include
    private String cardLast4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private Member member;
    @EqualsAndHashCode.Include
    private String stripePaymentIntentId;
    @EqualsAndHashCode.Include
    private String status; // SUCCEEDED, FAILED
    @EqualsAndHashCode.Include
    private String invoiceNumber;

    private Boolean invoiceIssued = false;
}