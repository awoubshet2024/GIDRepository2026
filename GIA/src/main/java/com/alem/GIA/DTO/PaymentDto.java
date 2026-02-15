package com.alem.GIA.DTO;

import com.alem.GIA.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private Integer paymentId;
    private Integer memberId;
    private double amount;
    private String reason;
    private Date paymentDate;
   public PaymentDto(Payment payment){
       this.amount = payment.getAmount();
       this.reason = payment.getReason();
       this.paymentDate = payment.getPaymentDate();
   }
}
