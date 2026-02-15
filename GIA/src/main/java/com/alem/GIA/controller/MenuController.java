package com.alem.GIA.controller;


import com.alem.GIA.DTO.MenuItemDto;
import com.alem.GIA.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuItemDto>> getUserMenu(Authentication authentication) {
        String username = authentication.getName();
        List<MenuItemDto> menu = menuService.getMenuForUser(username);
        return ResponseEntity.ok(menu);
    }
}