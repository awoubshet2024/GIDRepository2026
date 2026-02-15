package com.alem.GIA.auth;

import com.alem.GIA.permission.ApplicationUser;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public interface ApplicationUserDao {
    Optional<ApplicationUser> loadUsername(String username);
}
