package com.alem.GIA.otp;

import com.alem.GIA.entity.OtpManager;
import com.alem.GIA.repository.OtpMgtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;



@Service
@RequiredArgsConstructor
@Slf4j
public class OtpDomainService {

    private final OtpMgtRepository otpRepo;

    public void generateOtp(String userName, String purpose) {

        String otp = String.valueOf(
                100000 + new SecureRandom().nextInt(900000)
        );

        OtpManager manager = new OtpManager();
        manager.setUserName(userName);
        manager.setOtpFor(purpose);
        manager.setOtpText(otp);
        manager.setRegAt(new Date());
        manager.setExpireAt(
                new Date(System.currentTimeMillis() + 10 * 60 * 1000)
        );

        otpRepo.save(manager);

        log.info("OTP for {} : {}", userName, otp);
    }

    public void validateAndConsumeOtp(
            String userName,
            String otp,
            String purpose
    ) {

        OtpManager manager = otpRepo
                .findTopByUserNameAndOtpForOrderByRegAtDesc(userName, purpose)
                .orElseThrow(() ->
                        new RuntimeException("OTP not found")
                );

        if (manager.getExpireAt().before(new Date())) {
            throw new RuntimeException("OTP expired");
        }

        if (!otp.equals(manager.getOtpText())) {
            throw new RuntimeException("Invalid OTP");
        }

        otpRepo.delete(manager);
    }
}


