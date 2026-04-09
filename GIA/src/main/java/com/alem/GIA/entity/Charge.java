package com.alem.GIA.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Charge {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Member member;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount = BigDecimal.ZERO;

    private String description;

    private LocalDate dueDate;

    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount);
    }

    public void applyPayment(BigDecimal amount) {
        this.paidAmount = this.paidAmount.add(amount);
    }

}

