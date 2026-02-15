package com.alem.GIA.repository;

import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.entity.Member;
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
   WHERE m.id = :id
""")
    Optional<Member> findByIdWithUser(Long id);


    @Query("select u from ApplicationUser u where u.userName = ?1")
   ApplicationUser loadUserByUsername(String username);
    Optional<Member> findByMemberId(Integer memberId);



    Optional<Member> findMemberByEmail(String email);

    boolean existsByEmail(String email);

    Member findByEmail(String email);
    Optional<Member> findByUser_UserName(String username);

    Optional<Member> findByUserId(Integer userId);

    boolean existsByUser(ApplicationUser targetUser);
}
