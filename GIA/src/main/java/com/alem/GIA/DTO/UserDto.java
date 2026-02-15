package com.alem.GIA.DTO;

import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "userId") // Ensure distinct by ID
public class UserDto {
    Integer userId;
    String fullName;
    String userName;
    String password;
    String email;
    String phone;
    boolean status;
    Set<RoleDto>roles = new HashSet<>();
    public UserDto(Integer userId,String userName){
        this.userId = userId;
        this.userName = userName;
    }
    public UserDto(Integer userId,String userName,String email){
        this.userId = userId;
        this.userName = userName;
        this.email = email;
    }
    public UserDto(Integer userId,String userName,Set<RoleDto>roles){
        this.userId = userId;
        this.userName = userName;
        this.roles = roles;
    }
    public UserDto(Integer userId,String userName,String password,String fullName,String email,String phone, Set<RoleDto>roles){
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.roles = roles;

    }
    public static UserDto fromEntity(ApplicationUser user){
        return new UserDto(
                user.getId(),user.getUserName(),user.getPassword(),user.getFullName(),user.getEmail(),
                user.getPhone(),
                user.getRoles().stream().map(RoleDto::fromEntity)
                        .collect(Collectors.toSet()));

    }




}
