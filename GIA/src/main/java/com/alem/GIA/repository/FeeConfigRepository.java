package com.alem.GIA.repository;

import com.alem.GIA.entity.FeeConfig;
import com.alem.GIA.enumes.MaritalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeeConfigRepository extends JpaRepository<FeeConfig, Integer> {

    Optional<FeeConfig> findByMaritalStatus(MaritalStatus maritalStatus);

}