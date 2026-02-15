package com.alem.GIA.entity;

import com.alem.GIA.DTO.RoleDto;
import com.alem.GIA.permission.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TempUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer tempUserId;
    private String fullName;
    private String userName;
    private String password;
    private String email;
    private String phone;


}
