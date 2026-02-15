package com.alem.GIA.repository;

import com.alem.GIA.entity.OtpManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpMgtRepository extends JpaRepository<OtpManager,Integer> {
  Optional<OtpManager> findByUserName(String userName);
  Optional<OtpManager> findTopByUserNameAndOtpForOrderByRegAtDesc(
          String userName,
          String otpFor
  );

}
