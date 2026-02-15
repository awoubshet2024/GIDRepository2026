package com.alem.GIA.controller;

import com.alem.GIA.DTO.PermissionDto;
import com.alem.GIA.permission.Permission;
import com.alem.GIA.service.PermissionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("api/permissions")
@AllArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping("/save")
    public ResponseEntity<Permission>savePermission(@RequestBody Permission permission){
        return new ResponseEntity<>(permissionService.savePermission(permission), HttpStatus.CREATED);
    }
    @GetMapping("/allPermissions")
    public ResponseEntity<Set<PermissionDto>>getAllPermissions(){
        return ResponseEntity.ok(permissionService.getAllPermission());
    }

}
