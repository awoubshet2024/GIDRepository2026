package com.alem.GIA.service;

import com.alem.GIA.entity.Member;
import com.alem.GIA.enumes.BillingPeriod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FeeService {



        public BigDecimal calculateBaseFee(Member member) {
            if (member.getMaritalStatus() == null) {
                return BigDecimal.valueOf(20); // default fee
            }

            switch (member.getMaritalStatus()) {

                case SINGLE:
                    return BigDecimal.valueOf(20);

                case SINGLE_WITH_CHILD:
                    return BigDecimal.valueOf(25);

                case MARRIED_WITH_CHILD:
                    return BigDecimal.valueOf(30);

                case MARRIED_WITHOUT_CHILD:
                    return BigDecimal.valueOf(25);

                default:
                    return BigDecimal.valueOf(20);
            }
        }

        public BigDecimal applyBillingPeriod(BigDecimal baseFee, BillingPeriod period) {

            switch (period) {

                case MONTHLY:
                    return baseFee;

                case QUARTERLY:
                    return baseFee.multiply(BigDecimal.valueOf(3));

                case SEMIANNUAL:
                    return baseFee.multiply(BigDecimal.valueOf(6));

                case YEARLY:
                    return baseFee.multiply(BigDecimal.valueOf(12));

                default:
                    return baseFee;
            }
        }
    }

