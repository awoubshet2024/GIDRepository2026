package com.alem.GIA.model;

import com.alem.GIA.entity.Dependent;
import com.alem.GIA.entity.Payment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberModel {

    private Integer memberId;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private LocalDate dateOfBirth;
    private LocalDate dateOfReg;
    private List<Dependent> dependents = new ArrayList<>();
    private List<Payment> payments = new ArrayList<>();
    private MultipartFile photo;
}
