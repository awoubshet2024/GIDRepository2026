package com.alem.GIA.service;

import com.alem.GIA.DTO.MenuItemDto;
import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.Permission;
import com.alem.GIA.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Entry point called by controller
     */
    public List<MenuItemDto> getMenuForUser(String username) {

        ApplicationUser user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 🔐 Collect ALL permissions from ALL roles
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());

        return buildMenu(permissions);
    }

    /**
     * Build menu strictly based on permissions
     */
    private List<MenuItemDto> buildMenu(Set<String> permissions) {

        List<MenuItemDto> menu = new ArrayList<>();

        // =======================
        // DASHBOARD (All logged-in users)
        // =======================
        if (permissions.contains("USER:READ") || permissions.contains("ADMIN:READ")) {
            menu.add(new MenuItemDto(
                    "Dashboard",
                    "/dashboard",
                    "house-door",
                    "USER:READ"
            ));
        }

        // =======================
        // USER SECTION
        // =======================
        if (permissions.contains("USER:READ")) {
            menu.add(new MenuItemDto(
                    "My Profile",
                    "/memberdetails",
                    "person",
                    "USER:READ"
            ));

            menu.add(new MenuItemDto(
                    "Settings",
                    "/mgt/settings",
                    "sliders",
                    "USER:READ"
            ));
        }

        // =======================
        // ADMIN SECTION
        // =======================
        if (permissions.contains("ADMIN:WRITE")) {

            MenuItemDto adminMenu = new MenuItemDto(
                    "Admin",
                    "/admin",
                    "shield-lock",
                    "ADMIN:WRITE"
            );

            adminMenu.getChildren().add(new MenuItemDto(
                    "Users",
                    "/admin/users",
                    "person-badge",
                    "ADMIN:WRITE"
            ));

            adminMenu.getChildren().add(new MenuItemDto(
                    "Members",
                    "/admin/members",
                    "people",
                    "ADMIN:WRITE"
            ));

            adminMenu.getChildren().add(new MenuItemDto(
                    "Add Member",
                    "/admin/addMember",
                    "person-plus",
                    "ADMIN:WRITE"
            ));

            // Audit should be ADMIN:READ (not write)
            if (permissions.contains("ADMIN:READ")) {
                adminMenu.getChildren().add(new MenuItemDto(
                        "Audit",
                        "/admin/audit",
                        "clipboard-data",
                        "ADMIN:READ"
                ));
            }

            menu.add(adminMenu);
        }

        return menu;
    }

}
