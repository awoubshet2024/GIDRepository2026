package com.alem.GIA.repository;

import com.alem.GIA.entity.Payment;
import com.alem.GIA.permission.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    public Optional<Payment> findById(Integer paymentId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.member WHERE p.paymentId = :id")
    Optional<Payment> findByIdWithMember(@Param("id") Integer id);

    List<Payment> findByMemberMemberId(Integer memberId);

    @Query("""
            SELECT m.maritalStatus, SUM(p.amount)
            FROM Payment p
            JOIN p.member m
            GROUP BY m.maritalStatus
            """)
    List<Object[]> sumPaymentsByMaritalStatus();

    @Query("""
SELECT 
m.maritalStatus,
COUNT(DISTINCT m.memberId),
COALESCE(SUM(p.amount),0),
COALESCE(AVG(p.amount),0)
FROM Member m
LEFT JOIN m.payments p
GROUP BY m.maritalStatus
""")
    List<Object[]> getPaymentAnalytics();

}
