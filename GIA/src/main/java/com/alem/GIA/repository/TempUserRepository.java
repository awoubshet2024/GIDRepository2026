package com.alem.GIA.repository;

import com.alem.GIA.entity.TempUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TempUserRepository extends JpaRepository<TempUser,Integer> {
    Optional<TempUser> findByUserName(String userName);
}
