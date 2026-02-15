package com.alem.GIA.model;

import com.alem.GIA.DTO.RoleDto;
import com.alem.GIA.permission.Role;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;


@NoArgsConstructor
@Getter
@Setter
public class RegisterRequest {
    private String userName;
    private String password;
    private String email;
    private Set<Role> roles = new HashSet<>();


}
