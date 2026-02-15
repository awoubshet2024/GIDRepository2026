package com.alem.GIA.service;

import com.alem.GIA.DTO.PermissionDto;
import com.alem.GIA.DTO.RoleDto;
import com.alem.GIA.DTO.RoleDtoDetails;
import com.alem.GIA.DTO.UserDto;
import com.alem.GIA.exception.PermissionIsAlreadyAdded;
import com.alem.GIA.exception.PermissionNotFound;
import com.alem.GIA.exception.RoleNotFoundException;
import com.alem.GIA.iModel.RolePermissionProjection;
import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.Permission;
import com.alem.GIA.permission.Role;
import com.alem.GIA.repository.PermissionRepository;
import com.alem.GIA.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository rolRepository, PermissionRepository permissionRepository) {
        this.roleRepository = rolRepository;
        this.permissionRepository = permissionRepository;
    }

    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }


    public RoleDto assignPermissionToRole(Integer roleId, Integer permissionId) {
        Role role = roleRepository.findById(roleId).orElseThrow(()-> new RoleNotFoundException(String.format("Role with %s is not found",roleId)));
        Permission permission = permissionRepository.
                findById(permissionId).orElseThrow(
                        () -> new PermissionNotFound(String.format("Permission with id %s not found", permissionId)));



        if (!role.getPermissions().contains(permission)) {
            role.getPermissions().add(permission);
            role = roleRepository.save(role);
        }

        return mapToRoleDto(role);
    }
    private RoleDto mapToRoleDto(Role role) {
        return new RoleDto(
                role.getId(),
                role.getRoleName(),
                role.getPermissions().stream()
                        .map(this::mapToPermissionDto)
                        .collect(Collectors.toSet())
        );
    }
    private PermissionDto mapToPermissionDto(Permission permission) {
        return new PermissionDto(
                permission.getId(),
                permission.getPermissionName()
        );
    }
    /**
     * Remove permission from role
     */
    public RoleDto removePermissionFromRole(Integer roleId, Integer permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RoleNotFoundException("Permission not found with id: " + permissionId));

        role.getPermissions().remove(permission);
        role = roleRepository.save(role);

        return mapToRoleDto(role);
    }

    /**
     * Update role permissions (replace all)
     */
    public RoleDto updateRolePermissions(Integer roleId, Set<Integer> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + roleId));

        Set<Permission> permissions = getValidPermissions(permissionIds);

        // Verify all requested permissions exist
      //  if (permissions.size() != permissionIds.size()) {
           // throw new RoleNotFoundException("One or more permissions not found");
        //}


        role.setPermissions(permissions);
        role = roleRepository.save(role);

        return mapToRoleDto(role);
    }
    private Set<Permission> getValidPermissions(Set<Integer> permissionIds) {
        Set<Permission> permissions = new HashSet<>(
                permissionRepository.findAllById(permissionIds)
        );

        validatePermissionsExist(permissionIds, permissions);
        return permissions;
    }
    private void validatePermissionsExist(Set<Integer> requestedIds, Set<Permission> foundPermissions) {
        if (foundPermissions.size() != requestedIds.size()) {
            Set<Integer> foundIds = foundPermissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());

            Set<Integer> missingIds = requestedIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());

            throw new RoleNotFoundException("Missing permissions: " + missingIds);
        }
    }
    private Role getRoleById(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
    }

    public List<Role> getRolesByExactName()
    {

            return roleRepository.findAll();
       // return roleRepository.filterByRoleName(roleName);
    }
    @Transactional
    public Set<RoleDto> findAllRoles(){
        List<Role> roles =  roleRepository.findAll();
      return  roles.stream().map(role -> new RoleDto(
            role.getId(),
              role.getRoleName(),
              role.getPermissions().stream().map(p -> new PermissionDto(
                      p.getId(), p.getPermissionName()
              )).collect(Collectors.toSet())
        )).collect(Collectors.toSet());

    }
    public void updateRolePermissions(Integer roleId,List<Integer>permissionIds){
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
       List<Permission> permissions = permissionRepository.findAllById(permissionIds);
       role.setPermissions(new HashSet<>(permissions));
       roleRepository.save(role);

    }

/*    public List<RoleDto> getAllRoles(boolean withUsers) {
        List<Role> roles;

        if (withUsers) {
            roles = roleRepository.findAllWithUsersAndPermissions();
        } else {
            roles = roleRepository.findAllWithPermissions();
        }

        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }*/

    /*private RoleDto convertToDTO(Role role) {
        RoleDto dto = new RoleDto();

        dto.setRoleId(role.getId());
        dto.setRoleName(role.getRoleName());

        dto.setPermissions(role.getPermissions().stream()
                .map(this::convertPermissionToDTO)
                .collect(Collectors.toSet()));

        if (!role.getUsers().isEmpty()) {
            dto.setUsers(role.getUsers().stream()
                    .map(this::convertUserToDTO)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }*/

    /*private PermissionDto convertPermissionToDTO(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setPermissionId(permission.getId());
        dto.setPermissionName(permission.getPermissionName());

        return dto;
    }*/

   /* private UserDto convertUserToDTO(ApplicationUser user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setPassword(user.getPassword());
        dto.setPhone(user.getPhone());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setRoles(Collections.emptySet());
        return dto;
    }*/

}

