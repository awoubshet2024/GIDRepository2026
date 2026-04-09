package com.alem.GIA.controller;


import com.alem.GIA.entity.FeeConfig;
import com.alem.GIA.enumes.MaritalStatus;
import com.alem.GIA.repository.FeeConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeConfigController {

    private final FeeConfigRepository repository;

    @PutMapping("/{status}")
    public FeeConfig updateFee(
            @PathVariable MaritalStatus status,
            @RequestParam BigDecimal fee) {

        FeeConfig config = repository
                .findByMaritalStatus(status)
                .orElseThrow();

        config.setMonthlyFee(fee);

        return repository.save(config);
    }
}