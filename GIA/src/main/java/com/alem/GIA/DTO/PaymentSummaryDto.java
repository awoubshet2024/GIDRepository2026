package com.alem.GIA.DTO;

import com.alem.GIA.enumes.MaritalStatus;

import java.math.BigDecimal;

public class PaymentSummaryDto {


    private MaritalStatus maritalStatus;
    private BigDecimal totalAmount;

    public PaymentSummaryDto(MaritalStatus maritalStatus, BigDecimal totalAmount) {
        this.maritalStatus = maritalStatus;
        this.totalAmount = totalAmount;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

}
