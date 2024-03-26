package com.example.demo.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    @Getter
    private final Long id;
    private final String username;
    @Getter
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String email, String password, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Account is never expired
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Account is never locked
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Credentials are never expired
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Account is always enabled
    @Override
    public boolean isEnabled() {
        return true;
    }
}
