package com.alem.GIA.service;

import com.alem.GIA.permission.ApplicationUser;


import com.alem.GIA.permission.AuthenticatedUser;
import com.alem.GIA.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class MyUserDetailsService implements UserDetailsService {

   private final UserRepository userRepository;


   public MyUserDetailsService(UserRepository accountRepository) {
        this.userRepository = accountRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        ApplicationUser user = userRepository
                .findByUserName(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"));

        Set<SimpleGrantedAuthority> authorities =
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(
                                "ROLE_" + r.getRoleName()))
                        .collect(Collectors.toSet());

        System.out.println("Authorities from DB: " + authorities);

        return new AuthenticatedUser(user,authorities);

    }

}
