package com.alem.GIA.controller;


import com.alem.GIA.DTO.RoleDto;
import com.alem.GIA.permission.Role;
import com.alem.GIA.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService){
        this.roleService = roleService;
    }
    @PostMapping("/addRole")
    public Role addRole(@RequestBody Role role) {

        return roleService.saveRole(role);
    }


    @PostMapping("/addPermissionToRole")
    public ResponseEntity<RoleDto> assignPermissionToRole(
            @RequestParam("roleId") Integer roleId,
            @RequestParam("permissionId") Integer permissionId) {
        RoleDto roleToUpdate = roleService.assignPermissionToRole(roleId,permissionId);

       return ResponseEntity.ok(roleToUpdate);
    }

   /* @GetMapping("/all")
    public ResponseEntity<List<RoleDto>> getAllRoles(
            @RequestParam(required = false) String expand) {

        boolean withUsers = expand != null && expand.contains("users");
        List<RoleDto> roles = roleService.getAllRoles(withUsers);

        return ResponseEntity.ok(roles);
    }*/
    @GetMapping("/allRoles")
    public Set<RoleDto> allRoles() {


        return roleService.findAllRoles();


    }
}
