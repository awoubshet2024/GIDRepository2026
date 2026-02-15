package com.alem.GIA.otp;

import com.alem.GIA.entity.TempUser;
import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.permission.Role;
import com.alem.GIA.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;

    public void ensureUserDoesNotExist(String userName, String email) {
        if (userRepository.findByUserName(userName).isPresent()) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already taken");
        }
    }

    public ApplicationUser createUserFromTemp(
            TempUser tempUser,
            Set<Role> roles
    ) {
        ApplicationUser user = new ApplicationUser();
        user.setUserName(tempUser.getUserName());
        user.setPassword(tempUser.getPassword());
        user.setEmail(tempUser.getEmail());
        user.setFullName(tempUser.getFullName());
        user.setPhone(tempUser.getPhone());
        user.setRoles(roles);

        return userRepository.save(user);
    }
}

