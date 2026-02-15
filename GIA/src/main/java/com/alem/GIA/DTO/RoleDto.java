package com.alem.GIA.DTO;

import com.alem.GIA.enumes.RoleName;
import com.alem.GIA.permission.Permission;
import com.alem.GIA.permission.Role;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "roleId")
public class RoleDto {
    Integer roleId;
    String roleName;
   // Set<UserDto>users;
    Set<PermissionDto> permissions;

    public RoleDto(String roleName) {

        this.roleName = roleName;
    }
    /*public RoleDto(Integer roleId,String roleName, Set<PermissionDto> permissions) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.permissions = permissions;
    }*/
    public RoleDto(String roleName, Set<PermissionDto> permissions) {

        this.roleName = roleName;
        this.permissions = permissions;
    }
    public RoleDto(Integer roleId,String roleName) {

        this.roleName = roleName;
        this.permissions = permissions;
    }
    public static Set<RoleDto> from(Set<Role> roles){
        return roles.stream().map(role -> {
            RoleDto roleDto = new RoleDto();
            roleDto.setRoleId(role.getId());
            roleDto.setRoleName(role.getRoleName());
            /*Set<PermissionDto> permissionDtos = role.getPermissions().stream().map(
                    p -> new PermissionDto(roleDto.getRoleName())
            ).collect(Collectors.toSet());*/
            roleDto.setPermissions(role.getPermissions().stream().map(
                    PermissionDto::fromEntity
                  //  p -> new PermissionDto(p.getId(), p.getPermissionName())
            ).collect(Collectors.toSet()));
            return roleDto;

        }).collect(Collectors.toSet());


    }
    public static RoleDto fromEntity(Role role){
        return new RoleDto(
                role.getId(),role.getRoleName(),
                role.getPermissions().stream().map(PermissionDto::fromEntity)
                        .collect(Collectors.toSet()));

    }



}


