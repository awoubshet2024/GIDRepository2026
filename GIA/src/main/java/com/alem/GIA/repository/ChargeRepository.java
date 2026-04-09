package com.alem.GIA.repository;

import com.alem.GIA.entity.Charge;
import com.alem.GIA.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge,Integer> {
    // Find all charges for a member
    List<Charge> findByMemberMemberId(Integer memberId);
    @Query("""
        SELECT c FROM Charge c
        WHERE c.member = :member
        AND c.totalAmount > c.paidAmount
        ORDER BY c.dueDate ASC
    """)
    List<Charge> findUnpaidCharges(Member member);
}
