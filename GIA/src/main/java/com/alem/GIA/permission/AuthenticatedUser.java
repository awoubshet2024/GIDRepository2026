package com.alem.GIA.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AuthenticatedUser implements UserDetails {

    private ApplicationUser user;
    private Set<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Update if you implement expiry
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // You can add lock functionality later
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Change if you expire credentials
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getStatus()); // ❗️ Check status here
    }





}
