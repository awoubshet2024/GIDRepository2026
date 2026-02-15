package com.alem.GIA.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ConfirmOtpDto {

    String userName;
    String otpText;
    String result;
    public static ConfirmOtpDto success(String userName) {
        return ConfirmOtpDto.builder()
                .userName(userName)
                .result("pass")
                .build();
    }

    public static ConfirmOtpDto failure(String message) {
        return ConfirmOtpDto.builder()
                .result(message)
                .build();
    }
}