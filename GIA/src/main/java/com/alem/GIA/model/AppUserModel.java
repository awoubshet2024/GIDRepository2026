package com.alem.GIA.model;

import com.alem.GIA.permission.Role;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Set;

public class AppUserModel {
    private String userName;
    private String password;
    private String email;
    private Set<Role> roles;
    private Set<GrantedAuthority> authorities = new HashSet<>();
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;
}
