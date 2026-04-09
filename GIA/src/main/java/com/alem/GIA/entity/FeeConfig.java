package com.alem.GIA.entity;

import com.alem.GIA.enumes.MaritalStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "fee_config")
@Data
public class FeeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    private BigDecimal monthlyFee;

}
