package com.repairapp.security;

import com.repairapp.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * UserPrincipal implementation for Spring Security UserDetails
 */
public class UserPrincipal implements UserDetails {
    
    private Long id;
    private String phone; // Using phone as username
    private String email;
    private String password;
    private String name;
    private User.Role role;
    private Boolean isActive;
    
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String phone, String email, String password, 
                        String name, User.Role role, Boolean isActive,
                        Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.isActive = isActive;
        this.authorities = authorities;
    }

    /**
     * Create UserPrincipal from User entity
     */
    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
            user.getId(),
            user.getPhone(),
            user.getEmail(),
            user.getPassword(),
            user.getName(),
            user.getRole(),
            user.getIsActive(),
            authorities
        );
    }

    // UserDetails implementation
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
        return phone; // Return phone as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive; // Only check if user is active, no email verification required
    }

    // Custom getters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public User.Role getRole() {
        return role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
