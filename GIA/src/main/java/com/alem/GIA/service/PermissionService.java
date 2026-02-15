package com.alem.GIA.service;

import com.alem.GIA.DTO.PermissionDto;
import com.alem.GIA.permission.Permission;
import com.alem.GIA.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    public PermissionService(PermissionRepository permissionRepository){
        this.permissionRepository = permissionRepository;
    }
    public Permission savePermission(Permission permission){
        return permissionRepository.save(permission);

    }
    public Set<PermissionDto> getAllPermission(){
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream().map(p -> new PermissionDto(
                p.getId(),p.getPermissionName())).collect(Collectors.toSet());

    }

}
