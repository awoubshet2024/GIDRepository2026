package com.alem.GIA.service;

import com.alem.GIA.DTO.PaymentAnalyticsDto;
import com.alem.GIA.DTO.PaymentDto;
import com.alem.GIA.DTO.PaymentSummaryDto;
//import com.alem.GIA.entity.Charge;
import com.alem.GIA.entity.Charge;
import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.MembershipCharge;
import com.alem.GIA.entity.Payment;
import com.alem.GIA.enumes.MaritalStatus;

import com.alem.GIA.model.PaymentResponse;
import com.alem.GIA.repository.ChargeRepository;
import com.alem.GIA.repository.MemberRepository;
import com.alem.GIA.repository.MembershipChargeRepository;
import com.alem.GIA.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Value;
import com.stripe.Stripe; // <--- Add this line
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;


import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import com.stripe.model.PaymentIntent;
import com.stripe.param.ChargeListParams;
import com.stripe.model.ChargeCollection;


@Service
public class PaymentService {
    private final ChargeRepository chargeRepository;
    private final PaymentRepository paymentRepository;
    private final FeeService feeService;
    private final MemberRepository memberRepository;
    private final MembershipChargeRepository membershipChargeRepository;

    public PaymentService(ChargeRepository chargeRepository, PaymentRepository paymentRepository, FeeService feeService, MemberRepository memberRepository, MembershipChargeRepository membershipChargeRepository) {
        this.chargeRepository = chargeRepository;
        this.paymentRepository = paymentRepository;
        this.feeService = feeService;
        //this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.membershipChargeRepository = membershipChargeRepository;
    }
    public void applyPayment(Integer memberId, BigDecimal paymentAmount) {

        List<MembershipCharge> unpaid =
                membershipChargeRepository.findUnpaidByMember(memberId);

        for (MembershipCharge c : unpaid) {

            if (paymentAmount.compareTo(c.getAmount()) >= 0) {

                c.setStatus("PAID");

                paymentAmount = paymentAmount.subtract(c.getAmount());

                membershipChargeRepository.save(c);
            }
        }
    }
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    public PaymentResponse save(Payment payment) {
        paymentRepository.save(payment);
        return PaymentResponse.builder()
                .payment(payment)
                .result(true)
                .message("Payment successfully saved")
                .build();
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByMember(Integer memberId){
        return paymentRepository.findByMemberMemberId(memberId);
    }
    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    public String createPaymentIntent(PaymentDto dto) throws Exception {
        Stripe.apiKey = stripeSecretKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(dto.getAmount().multiply(new java.math.BigDecimal(100)).longValue()) // cents
                .setCurrency("usd")
                .putMetadata("memberId", String.valueOf(dto.getMemberId()))
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }
    @Transactional
    public String addPaymentToMember(Integer memberId, PaymentDto dto) throws StripeException {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Payment payment = new Payment();
        payment.setMember(member);
        payment.setAmount(dto.getAmount());
        payment.setReason(dto.getReason());
        payment.setBillingPeriod(dto.getBillingPeriod());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setStatus(dto.getStatus());
        payment.setCheckNumber(dto.getCheckNumber());

        if(dto.getStripePaymentIntentId() != null){
            payment.setStripePaymentIntentId(dto.getStripePaymentIntentId());
            Stripe.apiKey = stripeSecretKey;
            PaymentIntent intent = PaymentIntent.retrieve(dto.getStripePaymentIntentId());

            // Get charges via Charge.list
            ChargeListParams params = ChargeListParams.builder()
                    .setPaymentIntent(intent.getId())
                    .setLimit(1L)
                    .build();

            ChargeCollection charges = com.stripe.model.Charge.list(params);

            if (!charges.getData().isEmpty()) {
                com.stripe.model.Charge stripeCharge = charges.getData().get(0);
                String last4 = stripeCharge.getPaymentMethodDetails().getCard().getLast4();
                payment.setCardLast4(last4);
            }
        }

        payment.setPaymentDate(LocalDate.now());

        paymentRepository.save(payment);

        // Apply payment to unpaid charges
        applyPayment(member, payment.getAmount());

        return "Payment saved successfully";
    }
    public List<PaymentSummaryDto> getPaymentsGroupedByMaritalStatus() {

        List<Object[]> results = paymentRepository.sumPaymentsByMaritalStatus();

        return results.stream()
                .map(r -> new PaymentSummaryDto(
                        (MaritalStatus) r[0],
                        (BigDecimal) r[1]
                ))
                .toList();
    }


    public List<PaymentAnalyticsDto> getPaymentAnalytics() {

        List<Object[]> results = paymentRepository.getPaymentAnalytics();

        return results.stream()
                .map(r -> new PaymentAnalyticsDto(
                        (MaritalStatus) r[0],
                        ((Number) r[1]).longValue(),
                        (BigDecimal) r[2],
                        r[3] == null
                                ? BigDecimal.ZERO
                                : BigDecimal.valueOf(((Number) r[3]).doubleValue())
                ))
                .toList();
    }


public Optional<Payment>findById(Integer paymentId){
        return paymentRepository.findById(paymentId);
}

    @Transactional
    public void applyPayment(Member member, BigDecimal paymentAmount) {

        List<Charge> charges = chargeRepository.findUnpaidCharges(member);

        for (Charge charge : charges) {

            if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal remaining = charge.getRemainingAmount();

            BigDecimal applied = paymentAmount.min(remaining);

            // Apply payment to charge
            charge.applyPayment(applied);

            // Save updated charge
            chargeRepository.save(charge);

            paymentAmount = paymentAmount.subtract(applied);
        }

        // If payment still remains → overpayment (credit)
        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {

            Charge credit = new Charge();

            credit.setMember(member);
            credit.setTotalAmount(paymentAmount);
            credit.setPaidAmount(paymentAmount);
            credit.setDescription("Account Credit");
            credit.setDueDate(LocalDate.now());

            chargeRepository.save(credit);
        }
    }
    public BigDecimal getMemberBalance(Integer memberId) {

        List<Charge> charges = chargeRepository.findByMemberMemberId(memberId);

        return charges.stream()
                .map(Charge::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    }







