package com.alem.GIA.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Payment {
    @Id
   private int paymentId;
   private   double amount;
   private String reason;
   private Date paymentDate;
    @JsonBackReference
    @ManyToOne
   private Member member;



}
