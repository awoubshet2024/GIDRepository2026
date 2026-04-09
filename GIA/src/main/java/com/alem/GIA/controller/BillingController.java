package com.alem.GIA.controller;


import com.alem.GIA.entity.BillingSummary;
import com.alem.GIA.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/summary/{memberId}")
    public BillingSummary getSummary(@PathVariable Integer memberId) {
        return billingService.getBillingSummary(memberId);
    }
}