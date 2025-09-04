package com.repairapp.service;

import com.repairapp.dto.auth.JwtResponse;
import com.repairapp.dto.auth.LoginRequest;
import com.repairapp.dto.auth.SignupRequest;
import com.repairapp.entity.User;
import com.repairapp.exception.BadRequestException;
import com.repairapp.repository.UserRepository;
import com.repairapp.security.JwtUtils;
import com.repairapp.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service for login, registration, and token management
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Authenticate user and generate JWT tokens
     */
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getMobile(), // Use mobile as username
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        return new JwtResponse(
            jwt,
            refreshToken,
            userPrincipal.getId(),
            userPrincipal.getName(),
            userPrincipal.getEmail(),
            userPrincipal.getPhone(),
            userPrincipal.getRole()
        );
    }

    /**
     * Authenticate user without password verification (for OTP-based login)
     * This should only be called after OTP verification is successful
     */
    public JwtResponse authenticateUserWithoutPassword(User user) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);
        
        return new JwtResponse(
            jwt,
            refreshToken,
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole()
        );
    }

    /**
     * Register new user
     */
    public User registerUser(SignupRequest signUpRequest) {
        // Check if phone already exists
        if (!isMobileAvailable(signUpRequest.getPhone())) {
            throw new BadRequestException("Phone number is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email address is already in use!");
        }

        // Create new user account
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setPhone(signUpRequest.getPhone());
        user.setRole(User.Role.CUSTOMER);
        user.setIsActive(true);
        user.setPhoneVerified(true); // Mark phone as verified since OTP was verified

        User savedUser = userRepository.save(user);
        logger.info("New user registered with phone: {}", savedUser.getPhone());
        
        return savedUser;
    }

    /**
     * Refresh JWT token
     */
    public JwtResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateJwtToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token!");
        }

        String phone = jwtUtils.getUserNameFromJwtToken(refreshToken);
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BadRequestException("User not found!"));

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities()
        );

        String newJwt = jwtUtils.generateJwtToken(authentication);
        String newRefreshToken = jwtUtils.generateRefreshToken(authentication);

        return new JwtResponse(
            newJwt,
            newRefreshToken,
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole()
        );
    }

    /**
     * Change user password
     */
    public boolean changePassword(String phone, String oldPassword, String newPassword) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BadRequestException("User not found!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        logger.info("Password changed for user with phone: {}", phone);
        return true;
    }

    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("No authenticated user found!");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BadRequestException("User not found!"));
    }

    /**
     * Reset password (for forgot password functionality)
     */
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found with email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        logger.info("Password reset for user with phone: {}", user.getPhone());
    }
    
    /**
     * Reset password with email (enhanced version)
     */
    public boolean resetPasswordWithEmail(String email, String newPassword) {
        try {
            resetPassword(email, newPassword);
            return true;
        } catch (Exception e) {
            logger.error("Failed to reset password for email: {}", email, e);
            return false;
        }
    }
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    
    /**
     * Get user by mobile number
     */
    public User getUserByMobile(String mobile) {
        return userRepository.findByPhone(mobile).orElse(null);
    }
    
    /**
     * Get user by mobile or email
     */
    public User getUserByMobileOrEmail(String identifier) {
        return userRepository.findByMobileOrEmail(identifier).orElse(null);
    }
    
    /**
     * Check if email is available
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    
    /**
     * Check if mobile number is available
     */
    public boolean isMobileAvailable(String mobile) {
        return !userRepository.existsByPhone(mobile);
    }
    
    /**
     * Save user entity
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
