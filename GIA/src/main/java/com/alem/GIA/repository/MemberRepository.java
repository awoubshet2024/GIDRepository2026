package com.alem.GIA.repository;

import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    @Query("""
               SELECT m FROM Member m
               LEFT JOIN FETCH m.user
               WHERE m.memberId = :id
            """)
    Optional<Member> findByIdWithUser(Integer id);


    @Query("select u from ApplicationUser u where u.userName = ?1")
    ApplicationUser loadUserByUsername(String username);

  @EntityGraph(attributePaths = {"address", "dependents", "payments","beneficiaries","beneficiaries.address"})
    Optional<Member> findMemberWithDependentsByMemberId(Integer memberId);
/*  @EntityGraph(attributePaths = {
          "address"
  })
  Optional<Member> findMemberWithDependentsByMemberId(Integer memberId);*/


    Optional<Member> findMemberByEmail(String email);

    boolean existsByEmail(String email);

   //Member findByEmail(String email);
   Optional<Member> findByEmail(String email);

    Optional<Member> findByUser_UserName(String username);

    Optional<Member> findByUserId(Integer userId);

    boolean existsByUser(ApplicationUser targetUser);


    @EntityGraph(attributePaths = {"dependents", "payments"})
    @Query("""
            SELECT m FROM Member m
            WHERE (:lastName IS NULL OR LOWER(m.lastName) LIKE :lastName)
            AND (:phone IS NULL OR m.phone LIKE :phone)
            """)
    Page<Member> searchMembers(
            @Param("lastName") String lastName,
            @Param("phone") String phone,
            Pageable pageable
    );



    @Query("""
       SELECT DISTINCT m FROM Member m
       LEFT JOIN FETCH m.address
       LEFT JOIN FETCH m.dependents
       LEFT JOIN FETCH m.payments
       LEFT JOIN FETCH m.beneficiaries b
       LEFT JOIN FETCH b.address
       WHERE m.user.id = :userId
       """)
    Optional<Member> findByUserIdWithCollections(@Param("userId") Integer userId);

    @EntityGraph(attributePaths = {"address", "dependents", "payments","beneficiaries","beneficiaries.address"})
    Optional<Member> findMemberWithBeneficiariesByMemberId(Integer memberId);
}
