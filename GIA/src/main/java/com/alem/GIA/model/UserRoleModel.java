package com.alem.GIA.model;

import com.alem.GIA.DTO.UserDto;
import com.alem.GIA.permission.ApplicationUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserRoleModel {
    String message;
    boolean result;
    ApplicationUser user;
}
