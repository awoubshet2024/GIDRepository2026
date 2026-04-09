package com.alem.GIA.DTO;

import com.alem.GIA.entity.Payment;
import com.alem.GIA.enumes.BillingPeriod;
import com.alem.GIA.enumes.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private Integer paymentId;
    private Integer memberId;
    private BigDecimal amount;
    private String reason;
    private LocalDate paymentDate;
    private BillingPeriod billingPeriod;
    private String status;
    private PaymentMethod paymentMethod;
    private String cardLast4;
    private String checkNumber;
    private String stripePaymentIntentId;
   public PaymentDto(Payment payment){
       this.paymentId = payment.getPaymentId();
       this.amount = payment.getAmount();
       this.reason = payment.getReason();
       this.paymentDate = payment.getPaymentDate();
       this.billingPeriod = payment.getBillingPeriod();
       this.status = payment.getStatus();
       this.paymentMethod = payment.getPaymentMethod();
       this.cardLast4 = payment.getCardLast4();
       this.checkNumber = payment.getCheckNumber();
       this.stripePaymentIntentId = payment.getStripePaymentIntentId();
   }
}
