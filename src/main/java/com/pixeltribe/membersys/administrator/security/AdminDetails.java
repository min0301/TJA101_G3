package com.pixeltribe.membersys.administrator.security;

import com.pixeltribe.membersys.administrator.model.Administrator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public class AdminDetails implements UserDetails {

    private final Administrator admin;

    public AdminDetails(Administrator admin) {
        this.admin = admin;
    }


    public Integer getId() {
        return admin.getId();
    }

    public String getRole() {
        return "ROLE_ADMIN";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(getRole()));
    }

    @Override
    public String getPassword() {
        return admin.getAdmPassword();
    }

    @Override
    public String getUsername() {
        return admin.getAdmAccount();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}