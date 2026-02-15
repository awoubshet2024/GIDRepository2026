package com.alem.GIA.repository;

import com.alem.GIA.entity.Payment;
import com.alem.GIA.permission.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

}
