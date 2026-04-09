package com.alem.GIA.entity;


import com.alem.GIA.enumes.LedgerType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class BillingLedger {

    @Id
    @GeneratedValue
    private Integer id;

    private Integer memberId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private LedgerType type;

    private String description;

    private LocalDate date;
}