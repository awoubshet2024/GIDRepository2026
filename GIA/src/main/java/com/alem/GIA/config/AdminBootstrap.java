package com.alem.GIA.config;

import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.Permission;
import com.alem.GIA.permission.Role;
import com.alem.GIA.repository.PermissionRepository;
import com.alem.GIA.repository.RoleRepository;
import com.alem.GIA.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {

        // 1️⃣ If admin already exists → do nothing
        if (userRepository.existsByRoles_RoleName("ADMIN")) {
            return;
        }

        // 2️⃣ Ensure ADMIN:WRITE permission exists
        Permission adminWrite = permissionRepository
                .findPermissionByPermissionName("ADMIN:WRITE")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setPermissionName("ADMIN:WRITE");
                    return permissionRepository.save(p);
                });

        // 3️⃣ Ensure ADMIN:READ permission exists
        Permission adminRead = permissionRepository
                .findPermissionByPermissionName("ADMIN:READ")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setPermissionName("ADMIN:READ");
                    return permissionRepository.save(p);
                });

        // 4️⃣ Ensure ADMIN role exists with BOTH permissions
        Role adminRole = roleRepository
                .findByRoleName("ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleName("ADMIN");
                    r.setPermissions(Set.of(adminWrite, adminRead));
                    return roleRepository.save(r);
                });

        // 5️⃣ Create bootstrap admin user
        ApplicationUser admin = new ApplicationUser();
        admin.setUserName("admin");
        admin.setEmail("admin@system.local");
        admin.setPassword(
                passwordEncoder.encode(
                        System.getenv().getOrDefault(
                                "BOOTSTRAP_ADMIN_PASSWORD",
                                "ChangeMeNow!"
                        )
                )
        );
        admin.setRoles(Set.of(adminRole));
        admin.setStatus(true);

        userRepository.save(admin);

        System.out.println("✔ Bootstrap ADMIN created with READ + WRITE permissions");
    }
}
