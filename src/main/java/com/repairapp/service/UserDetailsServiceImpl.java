package com.repairapp.service;

import com.repairapp.entity.User;
import com.repairapp.repository.UserRepository;
import com.repairapp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserDetailsService implementation for Spring Security
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Try to find user by mobile number or email
        User user = userRepository.findByMobileOrEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with mobile/email: " + identifier));

        return UserPrincipal.create(user);
    }

    /**
     * Load user by ID for JWT authentication
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return UserPrincipal.create(user);
    }
}
