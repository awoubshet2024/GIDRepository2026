package com.alem.GIA.controller;

import com.alem.GIA.DTO.RoleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/managements")
public class ManagementController {
    @PostMapping("/addRoleToUser")
    public ResponseEntity<RoleDto>addRoleToUser(){
        return null;

    }
}
