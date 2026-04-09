package com.alem.GIA.controller;

import com.alem.GIA.DTO.PaymentAnalyticsDto;
import com.alem.GIA.DTO.PaymentDto;
import com.alem.GIA.DTO.PaymentSummaryDto;
import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.Payment;
import com.alem.GIA.model.PaymentResponse;
import com.alem.GIA.report.ReportService;
import com.alem.GIA.service.InvoiceService;
import com.alem.GIA.service.MemberService;
import com.alem.GIA.service.PaymentService;
import com.stripe.exception.StripeException;
import net.sf.jasperreports.engine.JRException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.*;

@RestController
@RequestMapping("api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final ReportService reportService;
    private final InvoiceService invoiceService;


    public PaymentController(PaymentService paymentService,
                             ReportService reportService, InvoiceService invoiceService
    ) {
        this.paymentService = paymentService;
        this.reportService = reportService;

        this.invoiceService = invoiceService;
    }
    @GetMapping
    public ResponseEntity<List<Payment>> getPaymentsByMember(@RequestParam("memberId") Integer memberId){

        return ResponseEntity.ok(paymentService.getPaymentsByMember(memberId));
    }

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


   @PostMapping("/create-intent")
   public ResponseEntity<Map<String, String>> createIntent(@RequestBody PaymentDto dto) {
       try {
           String clientSecret = paymentService.createPaymentIntent(dto);
           Map<String, String> response = new HashMap<>();
           response.put("clientSecret", clientSecret);
           return ResponseEntity.ok(response);
       } catch (Exception e) {
           e.printStackTrace();
           return ResponseEntity.status(500).build();
       }
   }
    @PostMapping("/save")
    public ResponseEntity<Map<String,String>> savePayment(@RequestParam Integer memberId, @RequestBody PaymentDto dto) throws StripeException {
        String result = paymentService.addPaymentToMember(memberId, dto);
        Map<String,String>response = new HashMap<>();
        response.put("message",result);
        return ResponseEntity.ok(response);
    }

  @GetMapping("/invoice/{paymentId}")
  public ResponseEntity<byte[]> downloadInvoice(@PathVariable Integer paymentId) throws Exception {

      ByteArrayOutputStream invoice =
              invoiceService.generateInvoice(paymentId);

      return ResponseEntity.ok()
              .contentType(MediaType.APPLICATION_PDF)
              .body(invoice.toByteArray());
  }
    @GetMapping("/summary-by-marital-status")
    public List<PaymentSummaryDto> getPaymentsByMaritalStatus() {
        return paymentService.getPaymentsGroupedByMaritalStatus();
    }
    @GetMapping("/analytics")
    public List<PaymentAnalyticsDto> getAnalytics() {
        return paymentService.getPaymentAnalytics();
    }
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getMemberBalance(@RequestParam Integer memberId) {

        Map<String, Object> response = new HashMap<>();

        response.put("memberId", memberId);
        response.put("balance", paymentService.getMemberBalance(memberId));

        return ResponseEntity.ok(response);
    }
}
