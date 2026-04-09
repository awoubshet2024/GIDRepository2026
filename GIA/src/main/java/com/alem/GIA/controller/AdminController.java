package com.alem.GIA.controller;

import com.alem.GIA.annotation.Auditable;
import com.alem.GIA.entity.Member;
import com.alem.GIA.model.MemberResponse;
import com.alem.GIA.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor // Automatically generates the constructor for final fields
public class AdminController {

    private final AdminService adminService;


    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@RequestBody Member member) {

        return ResponseEntity.ok(adminService.adminMemberRegister(member));
    }

}