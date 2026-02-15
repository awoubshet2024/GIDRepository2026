package com.alem.GIA.auth;

import com.alem.GIA.permission.ApplicationUser;
import com.alem.GIA.repository.UserRepository;

import java.util.Optional;

public class ApplicationUserDaoService implements ApplicationUserDao{
    private final UserRepository userRepository;

    public ApplicationUserDaoService( UserRepository userRepository) {
        this.userRepository = userRepository;

    }

    @Override
    public Optional<ApplicationUser> loadUsername(String username) {
        return userRepository.findByUserName(username);
    }
}
