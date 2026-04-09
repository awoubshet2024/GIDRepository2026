package com.alem.GIA.DTO;

import com.alem.GIA.enumes.MaritalStatus;

import java.math.BigDecimal;

public class PaymentAnalyticsDto {

        private MaritalStatus maritalStatus;
        private Long memberCount;
        private BigDecimal totalRevenue;
        private BigDecimal averagePayment;

        public PaymentAnalyticsDto(
                MaritalStatus maritalStatus,
                Long memberCount,
                BigDecimal totalRevenue,
                BigDecimal averagePayment) {

            this.maritalStatus = maritalStatus;
            this.memberCount = memberCount;
            this.totalRevenue = totalRevenue;
            this.averagePayment = averagePayment;
        }

        public MaritalStatus getMaritalStatus() {
            return maritalStatus;
        }

        public Long getMemberCount() {
            return memberCount;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public BigDecimal getAveragePayment() {
            return averagePayment;
        }
    }

