package com.alem.GIA.model;

import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentResponse {
    String message;
    boolean result;
    Payment payment;
}
