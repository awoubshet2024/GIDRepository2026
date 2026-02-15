package com.alem.GIA.model;



import com.alem.GIA.DTO.RoleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TokenResponse {
    private Integer userId;
    private String email;
    private String userName;
    private String message;
    private boolean status;
    private Token token;
    private Set<RoleDto> roles;

    // Add permissions and authorities to send to frontend
    private Set<String> permissions;
    private Set<String> authorities;
}

