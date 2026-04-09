package com.alem.GIA.repository;

import com.alem.GIA.entity.Member;
import com.alem.GIA.entity.MembershipCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MembershipChargeRepository extends JpaRepository<MembershipCharge, Integer> {


    @Query("""
                SELECT c FROM MembershipCharge c
                WHERE c.member.memberId = :memberId
                AND c.status = 'UNPAID'
                ORDER BY c.billingMonth ASC
            """)
    List<MembershipCharge> findUnpaidByMember(Integer memberId);
    boolean existsByMemberAndBillingMonth(Member member, LocalDate billingMonth);
}

