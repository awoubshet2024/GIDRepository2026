package com.alem.GIA.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillingSummary {


    private int monthsDue;
    private BigDecimal registrationFee;
    private BigDecimal monthlyFee;
    private int monthsActive;

    private BigDecimal totalDue;
    private BigDecimal totalPaid;
    private BigDecimal balanceDue;
    private BigDecimal creditBalance;

    private LocalDate nextBillingDate;
    private BigDecimal suggestedPayment;

    private String status; // PAID / DUE / PAST_DUE
}

