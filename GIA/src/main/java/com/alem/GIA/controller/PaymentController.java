package com.alem.GIA.controller;

import com.alem.GIA.DTO.PaymentDto;
import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.Payment;
import com.alem.GIA.model.PaymentResponse;
import com.alem.GIA.report.ReportService;
import com.alem.GIA.service.MemberService;
import com.alem.GIA.service.PaymentService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final ReportService reportService;




    public PaymentController(PaymentService paymentService,
                             ReportService reportService
                             ) {
        this.paymentService = paymentService;
        this.reportService = reportService;

    }
    @GetMapping
    public ResponseEntity<List<Payment>> getPaymentsByMember(@RequestParam("memberId") Integer memberId){
        Member member =  paymentService.getPaymentsByMember(memberId);
        List<Payment> payments = member.getPayments();
        return ResponseEntity.ok(payments);    }
   @GetMapping("/all")
    public List<Payment> findAll() {
        return paymentService.findAll();
    }
    @GetMapping("/report/{format}")
    public String generateReport(@PathVariable String format) throws FileNotFoundException, JRException {
        return reportService.exportReport(format);
    }
    @PostMapping("/addPayment")
    public PaymentResponse addPayment(@RequestBody Payment payment) {
       return paymentService.save(payment);
    }

    @PostMapping("/addPaymentToMember")
    public String addPaymentToMember(@RequestBody PaymentDto dto){


        return  paymentService.addPaymentToMember(dto);
    }
}
