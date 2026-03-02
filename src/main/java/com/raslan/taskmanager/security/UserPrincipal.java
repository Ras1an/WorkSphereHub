package com.raslan.taskmanager.security;

import com.raslan.taskmanager.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final boolean isVerified;
    private final Collection<? extends GrantedAuthority> authorities;


    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.isVerified = user.isVerified();
        this.authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isEnabled() {
        return isVerified;
    }
}
