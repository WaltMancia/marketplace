package com.marketplace.orderservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MarketplaceUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String role;
    private final List<GrantedAuthority> authorities;

    public MarketplaceUserDetails(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = normalizeRole(role);
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public boolean hasRole(String expectedRole) {
        return role.equalsIgnoreCase(normalizeRole(expectedRole));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "CUSTOMER";
        }

        String cleanedRole = role.trim().toUpperCase();
        return cleanedRole.startsWith("ROLE_") ? cleanedRole.substring("ROLE_".length()) : cleanedRole;
    }
}