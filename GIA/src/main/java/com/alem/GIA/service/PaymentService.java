package com.alem.GIA.service;

import com.alem.GIA.DTO.PaymentDto;
import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.Payment;
import com.alem.GIA.exception.MemberNotFoundException;
import com.alem.GIA.model.PaymentResponse;
import com.alem.GIA.repository.MemberRepository;
import com.alem.GIA.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    public PaymentService(PaymentRepository paymentRepository, MemberService memberService, MemberRepository memberRepository) {
        this.paymentRepository = paymentRepository;
        this.memberService = memberService;
        this.memberRepository = memberRepository;
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
    public Member getPaymentsByMember(Integer memberId){
        return   memberRepository.findByMemberId(memberId).orElseThrow(

                () -> new MemberNotFoundException(String.format("Member with id %s not found",memberId)));
    }

    public String addPaymentToMember(PaymentDto dto) {
        Optional<Member> member = memberService.findMemberById(dto.getMemberId());
        if(member.isPresent()){
            Payment payment = new Payment();
            payment.setPaymentId(dto.getPaymentId());
            payment.setAmount(dto.getAmount());
            payment.setPaymentDate(dto.getPaymentDate());
            payment.setReason(dto.getReason());
            member.get().getPayments().add(payment);
            memberService.saveMember(member.get());
            List<Payment> payments = member.get().getPayments();
            payments.forEach(p ->{
                System.out.printf("Payment Id: %d%n" +
                        "Payment Amount: %.2f%n" +
                        "Payment Reason: %s%n Payment Date: %s%n",
                        p.getPaymentId(),
                        p.getAmount(),
                        p.getReason(),
                        p.getPaymentDate());
            });

            return "Payment added successfully";
        }
        return "Payment is not added";
    }
}
