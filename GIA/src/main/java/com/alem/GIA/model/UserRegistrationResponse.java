package com.alem.GIA.model;

import com.alem.GIA.entity.TempUser;
import com.alem.GIA.permission.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class UserRegistrationResponse {
    String message;
    boolean result;
    Integer userId;
    String userName;
    String phone;
    public static UserRegistrationResponse success(TempUser tempUser) {
        return UserRegistrationResponse.builder()
                .result(true)
                .message("User temporarily registered successfully")
                .userName(tempUser.getUserName())
                .userId(tempUser.getTempUserId())
                .phone(tempUser.getPhone())
                .build();
    }

}
