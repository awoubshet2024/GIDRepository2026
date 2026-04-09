package com.alem.GIA.service;

import com.alem.GIA.entity.BillingSummary;
import com.alem.GIA.entity.FeeConfig;
import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.Payment;
import com.alem.GIA.repository.FeeConfigRepository;
import com.alem.GIA.repository.MemberRepository;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final MemberRepository memberRepository;
    private final FeeConfigRepository feeConfigRepository;

    private static final BigDecimal REGISTRATION_FEE = new BigDecimal("300");
    @Transactional(readOnly = true)
    public BillingSummary getBillingSummary(Integer memberId) {

        Member member = memberRepository.findMemberWithDependentsByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        return calculateBillingSummary(member);
    }


public BigDecimal getMonthlyFee(Member member) {

    if(member.getMaritalStatus() == null){
        return BigDecimal.ZERO;
    }

    return feeConfigRepository
            .findByMaritalStatus(member.getMaritalStatus())
            .map(FeeConfig::getMonthlyFee)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Fee configuration missing for marital status: "
                                    + member.getMaritalStatus()
                    )
            );
            //.orElse(BigDecimal.ZERO);
}
    @Transactional(readOnly = true)
    public BillingSummary calculateBillingSummary(Member member) {

        BillingSummary summary = new BillingSummary();

        BigDecimal monthlyFee = getMonthlyFee(member);
        summary.setMonthlyFee(monthlyFee);
        summary.setRegistrationFee(REGISTRATION_FEE);

        LocalDate regDate = member.getDateOfReg();
        LocalDate today = LocalDate.now();

        int monthsSinceRegistration = 0;

        if (regDate != null) {

            LocalDate startBilling = regDate.plusMonths(1).withDayOfMonth(1);

            Period period = Period.between(startBilling, today);

            monthsSinceRegistration =
                    period.getYears() * 12 + period.getMonths();

            monthsSinceRegistration = Math.max(monthsSinceRegistration, 0);
            if (monthsSinceRegistration == 0) {
                monthsSinceRegistration = 1;
            }
        }

        summary.setMonthsDue(monthsSinceRegistration);

        BigDecimal membershipRequired =
                monthlyFee.multiply(BigDecimal.valueOf(monthsSinceRegistration));

        BigDecimal totalRequired =
                REGISTRATION_FEE.add(membershipRequired);

        Set<Payment> payments = member.getPayments();

        BigDecimal totalPaid = BigDecimal.ZERO;

        if (payments != null) {
            totalPaid = payments.stream()
                    .map(Payment::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        summary.setTotalPaid(totalPaid);

        BigDecimal balanceDue = totalRequired.subtract(totalPaid);

        if (balanceDue.compareTo(BigDecimal.ZERO) < 0)
            balanceDue = BigDecimal.ZERO;

        summary.setBalanceDue(balanceDue);
        summary.setTotalDue(balanceDue);
        summary.setSuggestedPayment(balanceDue);

        BigDecimal credit = totalPaid.subtract(totalRequired);

        if (credit.compareTo(BigDecimal.ZERO) < 0)
            credit = BigDecimal.ZERO;

        summary.setCreditBalance(credit);

        BigDecimal membershipPaid = totalPaid.subtract(REGISTRATION_FEE);

        if (membershipPaid.compareTo(BigDecimal.ZERO) < 0)
            membershipPaid = BigDecimal.ZERO;


        int monthsCovered = 0;

        if(monthlyFee.compareTo(BigDecimal.ZERO) > 0){
            monthsCovered =
                    membershipPaid.divide(monthlyFee, 0, RoundingMode.DOWN).intValue();
        }

        summary.setMonthsActive(monthsCovered);


        LocalDate nextBilling = null;

        if(regDate != null){
            nextBilling = regDate
                    .plusMonths(1 + monthsCovered)
                    .withDayOfMonth(1);
        }

        summary.setNextBillingDate(nextBilling);

        String status = "DUE";

        if (balanceDue.compareTo(BigDecimal.ZERO) == 0)
            status = "PAID";

        if (nextBilling != null &&
                nextBilling.isBefore(today) &&
                balanceDue.compareTo(BigDecimal.ZERO) > 0) {

            status = "PAST_DUE";
        }

        summary.setStatus(status);

        return summary;
    }

}