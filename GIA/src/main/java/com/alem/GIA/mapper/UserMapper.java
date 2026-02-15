package com.alem.GIA.mapper;


// UserMapper.java
import com.alem.GIA.DTO.PermissionDto;
import com.alem.GIA.DTO.RoleDto;
import com.alem.GIA.DTO.UserDto;
import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.Permission;
import com.alem.GIA.permission.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDto toDTO(ApplicationUser user) {
        Set<RoleDto> roleDTOs = user.getRoles().stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toSet());

        return new UserDto(user.getId(), user.getUserName(), roleDTOs);
    }

    public static RoleDto toDTO(Role role) {
        Set<PermissionDto> permissionDTOs = role.getPermissions().stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toSet());

        return new RoleDto(role.getId(), role.getRoleName(), permissionDTOs);
    }

    public static PermissionDto toDTO(Permission permission) {
        return new PermissionDto(permission.getId(), permission.getPermissionName());
    }
}

