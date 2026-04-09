package com.alem.GIA.scheduler;

import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.MembershipCharge;
import com.alem.GIA.repository.MemberRepository;
import com.alem.GIA.repository.MembershipChargeRepository;
import com.alem.GIA.service.BillingService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BillingScheduler {

    private final MemberRepository memberRepository;
    private final MembershipChargeRepository chargeRepository;
    private final BillingService billingService;

    // Runs every month on the 1st at 1:00 AM
    @Scheduled(cron = "0 0 1 1 * ?")
    public void generateMonthlyCharges() {

        List<Member> members = memberRepository.findAll();

        LocalDate billingMonth = LocalDate.now().withDayOfMonth(1);

        for (Member member : members) {

            BigDecimal monthlyFee = billingService.getMonthlyFee(member);

            // ✅ Prevent duplicate charges
            boolean exists = chargeRepository.existsByMemberAndBillingMonth(
                    member,
                    billingMonth
            );

            if (!exists) {

                MembershipCharge charge = new MembershipCharge();
                charge.setMember(member);
                charge.setBillingMonth(billingMonth);
                charge.setAmount(monthlyFee);
                charge.setStatus("UNPAID");

                chargeRepository.save(charge);
            }
        }
    }
}