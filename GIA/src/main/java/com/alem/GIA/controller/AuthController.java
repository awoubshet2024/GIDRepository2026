package com.alem.GIA.controller;

import com.alem.GIA.DTO.ConfirmOtpDto;
import com.alem.GIA.DTO.OtpMgtDto;
import com.alem.GIA.DTO.UserDto;
import com.alem.GIA.model.UserRegistrationResponse;
import com.alem.GIA.otp.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    public UserRegistrationResponse register(@RequestBody UserDto dto) {
        return registrationService.register(dto);
    }

    @PostMapping("/confirm")
    public ConfirmOtpDto confirm(@RequestBody OtpMgtDto dto) {
        return registrationService.confirmRegistration(dto);
    }
}
