package com.alem.GIA.otp;

import com.alem.GIA.DTO.ConfirmOtpDto;
import com.alem.GIA.DTO.OtpMgtDto;
import com.alem.GIA.DTO.UserDto;
import com.alem.GIA.entity.TempUser;
import com.alem.GIA.model.UserRegistrationResponse;
import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.repository.TempUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {

    private final TempUserRepository tempUserRepo;
    private final UserDomainService userDomainService;
    private final OtpDomainService otpService;
    private final RoleAssignmentService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationResponse register(UserDto dto) {

        userDomainService.ensureUserDoesNotExist(dto.getUserName(), dto.getEmail());

        TempUser tempUser = TempUser.builder()
                .userName(dto.getUserName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .build();

        tempUserRepo.save(tempUser);
        otpService.generateOtp(dto.getUserName(), "REGISTRATION");

        return UserRegistrationResponse.success(tempUser);
    }

    public ConfirmOtpDto confirmRegistration(OtpMgtDto dto) {

        otpService.validateAndConsumeOtp(
                dto.getUserName(),
                dto.getOtpText(),
                "REGISTRATION"
        );

        TempUser tempUser = tempUserRepo.findByUserName(dto.getUserName())
                .orElseThrow(() -> new RuntimeException("Temp user not found"));

        ApplicationUser user = userDomainService.createUserFromTemp(
                tempUser,
                roleService.defaultUserRoles()
        );

        tempUserRepo.delete(tempUser);

        return ConfirmOtpDto.success(user.getUserName());
    }
}

