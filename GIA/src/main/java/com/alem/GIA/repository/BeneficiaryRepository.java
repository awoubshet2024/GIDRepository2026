package com.alem.GIA.repository;

import com.alem.GIA.entity.Beneficiary;
import com.alem.GIA.entity.Member;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Integer> {
    @Query("""
            SELECT b FROM Beneficiary b
            LEFT JOIN FETCH b.address
            WHERE b.member = :member
            """)
    List<Beneficiary> findByMember(Member member);
}
