package com.alem.GIA.config;

import com.alem.GIA.permission.Permission;
import com.alem.GIA.permission.Role;
import com.alem.GIA.repository.PermissionRepository;
import com.alem.GIA.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {

        // ✅ Create permissions
        Permission userRead = createPermission("USER:READ");
        Permission userWrite = createPermission("USER:WRITE");

        Permission adminRead = createPermission("ADMIN:READ");
        Permission adminWrite = createPermission("ADMIN:WRITE");

        // ✅ USER role
        roleRepository.findRoleByRoleName("USER")
                .ifPresentOrElse(role -> {
                    role.setPermissions(Set.of(userRead, userWrite));
                    roleRepository.save(role);
                }, () -> {
                    Role role = new Role();
                    role.setRoleName("USER");
                    role.setPermissions(Set.of(userRead, userWrite));
                    roleRepository.save(role);
                });

        // ✅ ADMIN role
        roleRepository.findRoleByRoleName("ADMIN")
                .ifPresentOrElse(role -> {
                    role.setPermissions(Set.of(adminRead, adminWrite));
                    roleRepository.save(role);
                }, () -> {
                    Role role = new Role();
                    role.setRoleName("ADMIN");
                    role.setPermissions(Set.of(adminRead, adminWrite));
                    roleRepository.save(role);
                });
    }

    private Permission createPermission(String name) {
        return permissionRepository
                .findPermissionByPermissionName(name)
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setPermissionName(name);
                    return permissionRepository.save(p);
                });
    }
}
