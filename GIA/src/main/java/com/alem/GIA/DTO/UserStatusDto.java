package com.alem.GIA.DTO;

import com.alem.GIA.permission.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserStatusDto {
    private Integer userId;
    private String fullName;
    private String email;
    private boolean status;
    public UserStatusDto(ApplicationUser user){
        this.userId = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.status = user.getStatus();

    }
}
