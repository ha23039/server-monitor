package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.monitoring.server.data.entity.User;
import com.monitoring.server.data.entity.User.UserRole;
import com.monitoring.server.data.repository.UserRepository;

/**
 * Service for handling authentication and user management
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Get the currently authenticated user
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String auth0Subject = jwt.getSubject();
            return userRepository.findByAuth0Subject(auth0Subject);
        }

        return Optional.empty();
    }

    /**
     * Get current user's role
     */
    public UserRole getCurrentUserRole() {
        return getCurrentUser()
            .map(User::getRole)
            .orElse(UserRole.VIEWER);
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(UserRole requiredRole) {
        UserRole currentRole = getCurrentUserRole();
        
        // Hierarchy: SYSADMIN > OPERATOR > VIEWER
        switch (requiredRole) {
            case VIEWER:
                return true; // All roles can view
            case OPERATOR:
                return currentRole == UserRole.SYSADMIN || currentRole == UserRole.OPERATOR;
            case SYSADMIN:
                return currentRole == UserRole.SYSADMIN;
            default:
                return false;
        }
    }

    /**
     * Check if current user is system administrator
     */
    public boolean isSysAdmin() {
        return hasRole(UserRole.SYSADMIN);
    }

    /**
     * Check if current user is operator or higher
     */
    public boolean isOperatorOrHigher() {
        return hasRole(UserRole.OPERATOR);
    }

    /**
     * Create or update user from Auth0 JWT token
     */
    public User createOrUpdateUser(Jwt jwt) {
        String auth0Subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String nickname = jwt.getClaimAsString("nickname");
        String picture = jwt.getClaimAsString("picture");
        
        // Extract roles from Auth0 custom claim
        @SuppressWarnings("unchecked")
        List<String> roles = jwt.getClaimAsStringList("https://servermonitor.app/roles");
        UserRole userRole = extractUserRole(roles);

        Optional<User> existingUser = userRepository.findByAuth0Subject(auth0Subject);
        
        if (existingUser.isPresent()) {
            // Update existing user
            User user = existingUser.get();
            user.setName(name);
            user.setNickname(nickname);
            user.setPicture(picture);
            user.setRole(userRole);
            user.setLastLogin(LocalDateTime.now());
            return userRepository.save(user);
        } else {
            // Create new user
            User newUser = new User(auth0Subject, email, name, userRole);
            newUser.setNickname(nickname);
            newUser.setPicture(picture);
            newUser.setLastLogin(LocalDateTime.now());
            return userRepository.save(newUser);
        }
    }

    /**
     * Extract user role from Auth0 roles claim
     */
    private UserRole extractUserRole(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return UserRole.VIEWER;
        }

        // Check for highest role first
        if (roles.contains("sysadmin")) {
            return UserRole.SYSADMIN;
        }
        if (roles.contains("operator")) {
            return UserRole.OPERATOR;
        }
        if (roles.contains("viewer")) {
            return UserRole.VIEWER;
        }

        return UserRole.VIEWER; // Default
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users by role
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Update user role (only for sysadmin)
     */
    public User updateUserRole(Long userId, UserRole newRole) {
        if (!isSysAdmin()) {
            throw new SecurityException("Only system administrators can update user roles");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(newRole);
            return userRepository.save(user);
        }
        
        throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    /**
     * Deactivate user (only for sysadmin)
     */
    public User deactivateUser(Long userId) {
        if (!isSysAdmin()) {
            throw new SecurityException("Only system administrators can deactivate users");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            return userRepository.save(user);
        }
        
        throw new IllegalArgumentException("User not found with ID: " + userId);
    }
}