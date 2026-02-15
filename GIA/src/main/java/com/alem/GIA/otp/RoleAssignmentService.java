package com.alem.GIA.otp;

import com.alem.GIA.permission.Role;
import com.alem.GIA.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleAssignmentService {

    private final RoleRepository roleRepository;

    public Set<Role> defaultUserRoles() {
        return Set.of(
                roleRepository.findRoleByRoleName("USER")
                        .orElseThrow(() -> new IllegalStateException("USER role missing"))
        );
    }
}
