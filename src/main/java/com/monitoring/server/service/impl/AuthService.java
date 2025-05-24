package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.monitoring.server.data.entity.User;
import com.monitoring.server.data.entity.User.UserRole;
import com.monitoring.server.data.repository.UserRepository;

/**
 * Service for handling authentication and user management
 * Actualizado para usar roles simplificados: ADMIN, USER
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

        // Para OAuth2 login (OidcUser)
        if (authentication.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            String auth0Subject = oidcUser.getSubject();
            return userRepository.findByAuth0Subject(auth0Subject);
        }
        
        // Para JWT tokens (APIs)
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
            .orElse(UserRole.USER);
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(UserRole requiredRole) {
        UserRole currentRole = getCurrentUserRole();
        
        // Jerarquía simplificada: ADMIN > USER
        switch (requiredRole) {
            case USER:
                return true; // Ambos roles pueden acceder a funciones USER
            case ADMIN:
                return currentRole == UserRole.ADMIN;
            default:
                return false;
        }
    }

    /**
     * Check if current user is administrator
     */
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    /**
     * Check if current user can manage system
     */
    public boolean canManageSystem() {
        return isAdmin();
    }

    /**
     * Check if current user can configure alerts
     */
    public boolean canConfigureAlerts() {
        return isAdmin();
    }

    /**
     * Check if current user can manage databases
     */
    public boolean canManageDatabases() {
        return isAdmin();
    }

    /**
     * Check if current user can manage users
     */
    public boolean canManageUsers() {
        return isAdmin();
    }

    /**
     * Check if current user can view dashboards
     */
    public boolean canAccessDashboard() {
        return getCurrentUser().isPresent(); // Cualquier usuario autenticado
    }

    /**
     * Create or update user from Auth0 OIDC token
     */
    public User createOrUpdateUser(OidcUser oidcUser) {
        String auth0Subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String nickname = oidcUser.getClaimAsString("nickname"); // Usar getClaimAsString
        String picture = oidcUser.getClaimAsString("picture");   // Usar getClaimAsString
        
        // Extract roles from Auth0 custom claim
        UserRole userRole = extractUserRoleFromOidc(oidcUser);

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
     * Create or update user from Auth0 JWT token (for APIs)
     */
    public User createOrUpdateUser(Jwt jwt) {
        String auth0Subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String nickname = jwt.getClaimAsString("nickname");
        String picture = jwt.getClaimAsString("picture");
        
        // Extract roles from Auth0 custom claim
        UserRole userRole = extractUserRoleFromJwt(jwt);

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
     * Extract user role from Auth0 OIDC user
     */
    private UserRole extractUserRoleFromOidc(OidcUser oidcUser) {
        // Buscar en claims de Auth0
        Object rolesObj = oidcUser.getClaims().get("https://servermonitor.api/roles");
        
        if (rolesObj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) rolesObj;
            return extractUserRole(roles);
        }
        
        return UserRole.USER; // Default
    }

    /**
     * Extract user role from Auth0 JWT token
     */
    private UserRole extractUserRoleFromJwt(Jwt jwt) {
        @SuppressWarnings("unchecked")
        List<String> roles = jwt.getClaimAsStringList("https://servermonitor.api/roles");
        return extractUserRole(roles);
    }

    /**
     * Extract user role from Auth0 roles claim - SIMPLIFICADO
     */
    private UserRole extractUserRole(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return UserRole.USER;
        }

        // Mapear roles de Auth0 a nuestros roles
        if (roles.contains("admin")) {
            return UserRole.ADMIN;
        }
        if (roles.contains("user")) {
            return UserRole.USER;
        }

        return UserRole.USER; // Default
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
     * Update user role (only for admin)
     */
    public User updateUserRole(Long userId, UserRole newRole) {
        if (!isAdmin()) {
            throw new SecurityException("Only administrators can update user roles");
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
     * Deactivate user (only for admin)
     */
    public User deactivateUser(Long userId) {
        if (!isAdmin()) {
            throw new SecurityException("Only administrators can deactivate users");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            return userRepository.save(user);
        }
        
        throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    /**
     * Activate user (only for admin)
     */
    public User activateUser(Long userId) {
        if (!isAdmin()) {
            throw new SecurityException("Only administrators can activate users");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(true);
            return userRepository.save(user);
        }
        
        throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    /**
     * Get current user display name
     */
    public String getCurrentUserName() {
        return getCurrentUser()
            .map(user -> user.getName() != null ? user.getName() : user.getEmail())
            .orElse("Usuario");
    }

    /**
     * Get current user role for display
     */
    public String getCurrentUserRoleDisplay() {
        UserRole role = getCurrentUserRole();
        return role.getDisplayName();
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }
}