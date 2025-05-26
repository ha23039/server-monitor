package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monitoring.server.data.entity.User;
import com.monitoring.server.data.entity.User.UserRole;
import com.monitoring.server.data.repository.UserRepository;

/**
 * Service for handling authentication and user management
 * ✅ DEFINITIVO: Roles 100% desde Auth0, sin SYSADMIN, sin hardcodeos
 */
@Service
@Transactional
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
            .orElse(UserRole.VIEWER); // Default role
    }

    /**
     * Check if current user has specific role (jerárquico)
     */
    public boolean hasRole(UserRole requiredRole) {
        UserRole currentRole = getCurrentUserRole();
        
        // Sistema jerárquico: ADMIN > OPERATOR > VIEWER > USER
        switch (requiredRole) {
            case USER:
            case VIEWER:
                return true; // Todos los roles pueden acceder a funciones básicas
            case OPERATOR:
                return currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR;
            case ADMIN:
                return currentRole == UserRole.ADMIN;
            default:
                return false;
        }
    }

    /**
     * ✅ DEFINITIVO: Check if current user is administrator (Solo ADMIN real)
     */
    public boolean isAdmin() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.ADMIN; // ✅ Solo ADMIN, sin OPERATOR temporal
    }

    /**
     * Check if current user is operator or admin
     */
    public boolean isOperator() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.ADMIN || role == UserRole.OPERATOR;
    }

    /**
     * Check if current user is viewer or higher
     */
    public boolean isViewer() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.ADMIN || role == UserRole.OPERATOR || 
               role == UserRole.VIEWER || role == UserRole.USER;
    }

    /**
     * Check if current user can manage system
     */
    public boolean canManageSystem() {
        return isAdmin(); // Solo ADMIN
    }

    /**
     * Check if current user can configure alerts
     */
    public boolean canConfigureAlerts() {
        return isOperator(); // Operator o Admin
    }

    /**
     * Check if current user can manage databases
     */
    public boolean canManageDatabases() {
        return isOperator(); // Operator o Admin
    }

    /**
     * ✅ DEFINITIVO: Check if current user can manage users (Solo ADMIN real)
     */
    public boolean canManageUsers() {
        return isAdmin(); // ✅ Solo ADMIN, sin OPERATOR temporal
    }

    /**
     * Check if current user can view dashboards
     */
    public boolean canAccessDashboard() {
        return getCurrentUser().isPresent(); // Cualquier usuario autenticado
    }

    /**
     * Check if current user can view metrics
     */
    public boolean canViewMetrics() {
        return isViewer(); // Viewer o superior
    }

    /**
     * Check if current user can export data
     */
    public boolean canExportData() {
        return isOperator(); // Operator o Admin
    }

    /**
     * ✅ DEFINITIVO: Create or update user from Auth0 OIDC token
     * Respeta 100% Auth0, sin hardcodeos, sin SYSADMIN
     */
    public User createOrUpdateUser(OidcUser oidcUser) {
        String auth0Subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String nickname = oidcUser.getClaimAsString("nickname");
        String picture = oidcUser.getClaimAsString("picture");
        
        logger.info("🔄 Sincronizando usuario OIDC: {} (subject: {})", email, auth0Subject);
        
        // ✅ DEFINITIVO: Extraer rol DIRECTAMENTE de Auth0
        UserRole roleFromAuth0 = extractUserRoleFromOidc(oidcUser);
        logger.info("🏷️ Rol extraído de Auth0 para {}: {}", email, roleFromAuth0);

        Optional<User> existingUser = userRepository.findByAuth0Subject(auth0Subject);
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            logger.info("👤 Actualizando usuario existente: {}", email);
            
            // Verificar si el rol cambió en Auth0
            if (!user.getRole().equals(roleFromAuth0)) {
                logger.info("🔄 ROL ACTUALIZADO para {}: {} → {}", 
                        email, user.getRole(), roleFromAuth0);
            }
        } else {
            user = new User(auth0Subject, email, name, roleFromAuth0);
            logger.info("🆕 Creando nuevo usuario: {}", email);
        }
        
        try {
            // ✅ Actualizar datos del usuario con rol de Auth0
            user.setName(name);
            user.setNickname(nickname);
            user.setPicture(picture);
            user.setRole(roleFromAuth0); // ✅ Rol 100% desde Auth0
            user.setLastLogin(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            logger.info("✅ Usuario guardado con ID: {} y rol: {}", savedUser.getId(), savedUser.getRole());
            
            return savedUser;
        } catch (Exception e) {
            logger.error("❌ Error sincronizando usuario: {}", e.getMessage());
            throw e;
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
        
        logger.info("🔄 Sincronizando usuario JWT: {} (subject: {})", email, auth0Subject);
        
        // ✅ Rol extraído directamente de Auth0
        UserRole roleFromAuth0 = extractUserRoleFromJwt(jwt);
        logger.info("🏷️ Rol extraído para {}: {}", email, roleFromAuth0);

        Optional<User> existingUser = userRepository.findByAuth0Subject(auth0Subject);
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = new User(auth0Subject, email, name, roleFromAuth0);
        }
        
        user.setName(name);
        user.setNickname(nickname);
        user.setPicture(picture);
        user.setRole(roleFromAuth0); // ✅ Rol 100% desde Auth0
        user.setLastLogin(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * ✅ Extract user role from Auth0 OIDC user - SIN HARDCODEOS
     */
    private UserRole extractUserRoleFromOidc(OidcUser oidcUser) {
        Map<String, Object> claims = oidcUser.getClaims();
        String email = oidcUser.getEmail();
        
        logger.debug("🔍 TODOS LOS CLAIMS para {}: {}", email, claims);
        
        // Buscar en el namespace personalizado de Auth0
        String rolesKey = "https://servermonitor.api/roles";
        Object rolesObj = claims.get(rolesKey);
        
        if (rolesObj instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) rolesObj;
            logger.info("✅ Roles extraídos del namespace custom para {}: {}", email, roles);
            return extractUserRole(roles);
        } else if (rolesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) rolesObj;
            logger.info("✅ Roles extraídos del namespace custom para {}: {}", email, roles);
            return extractUserRole(roles);
        }
        
        // Fallback: buscar en otros claims
        Object authoritiesObj = claims.get("authorities");
        if (authoritiesObj instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> authorities = (Collection<String>) authoritiesObj;
            logger.info("✅ Roles extraídos de authorities para {}: {}", email, authorities);
            return extractUserRole(authorities);
        }
        
        // Fallback final: buscar roles directamente
        Object directRoles = claims.get("roles");
        if (directRoles instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) directRoles;
            logger.info("✅ Roles extraídos directamente para {}: {}", email, roles);
            return extractUserRole(roles);
        }
        
        logger.warn("⚠️ No se encontraron roles para usuario {}, asignando VIEWER", email);
        return UserRole.VIEWER; // Default
    }

    /**
     * Extract user role from Auth0 JWT token
     */
    private UserRole extractUserRoleFromJwt(Jwt jwt) {
        // Buscar en el namespace personalizado
        List<String> roles = jwt.getClaimAsStringList("https://servermonitor.api/roles");
        if (roles != null && !roles.isEmpty()) {
            return extractUserRole(roles);
        }
        
        // Fallback
        List<String> authorities = jwt.getClaimAsStringList("authorities");
        if (authorities != null && !authorities.isEmpty()) {
            return extractUserRole(authorities);
        }
        
        return UserRole.VIEWER; // Default
    }

    /**
     * ✅ Extract user role from Auth0 roles claim - DEFINITIVO
     * Solo admin, operator, viewer, user (minúsculas)
     */
    private UserRole extractUserRole(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return UserRole.VIEWER;
        }

        logger.info("🔍 ROLES EXTRAÍDOS DE AUTH0: {}", roles);

        // ✅ Prioridad jerárquica: admin > operator > viewer > user
        // ✅ DEFINITIVO: Solo minúsculas, sin SYSADMIN
        if (roles.contains("admin")) {
            return UserRole.ADMIN;  // ✅ Solo 'admin', nunca 'sysadmin'
        }
        if (roles.contains("operator")) {
            return UserRole.OPERATOR;
        }
        if (roles.contains("viewer")) {
            return UserRole.VIEWER;
        }
        if (roles.contains("user")) {
            return UserRole.USER;
        }

        // ⚠️ MIGRACIÓN: Si hay SYSADMIN por migración, convertir a ADMIN
        if (roles.contains("sysadmin") || roles.contains("SYSADMIN")) {
            logger.warn("🔄 MIGRACIÓN: Convirtiendo SYSADMIN a ADMIN para roles: {}", roles);
            return UserRole.ADMIN;
        }

        logger.warn("⚠️ Roles no reconocidos: {}, asignando VIEWER", roles);
        return UserRole.VIEWER; // Default seguro
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
     * ✅ Update user role (only for admin) - SIN RESTRICCIONES HARDCODEADAS
     */
    public User updateUserRole(Long userId, UserRole newRole) {
        if (!isAdmin()) {
            throw new SecurityException("Only administrators can update user roles");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserRole oldRole = user.getRole();
            
            user.setRole(newRole);
            user.setUpdatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            logger.info("🔄 Rol actualizado para usuario {}: {} -> {}", 
                       user.getEmail(), oldRole, newRole);
            
            return savedUser;
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
            user.setUpdatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            logger.info("🚫 Usuario desactivado: {}", user.getEmail());
            
            return savedUser;
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
            user.setUpdatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            logger.info("✅ Usuario activado: {}", user.getEmail());
            
            return savedUser;
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

    /**
     * Debug current user - Para troubleshooting
     */
    public void debugCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.info("🐛 =====DEBUG USUARIO ACTUAL=====");
            logger.info("🐛 Name: {}", auth.getName());
            logger.info("🐛 Principal: {}", auth.getPrincipal().getClass().getSimpleName());
            logger.info("🐛 Authenticated: {}", auth.isAuthenticated());
            logger.info("🐛 Authorities: {}", auth.getAuthorities());
            
            Optional<User> currentUser = getCurrentUser();
            if (currentUser.isPresent()) {
                User user = currentUser.get();
                logger.info("🐛 DB User ID: {}", user.getId());
                logger.info("🐛 DB User Email: {}", user.getEmail());
                logger.info("🐛 DB User Role: {}", user.getRole());
                logger.info("🐛 isAdmin: {}", isAdmin());
                logger.info("🐛 isOperator: {}", isOperator());
                logger.info("🐛 isViewer: {}", isViewer());
                logger.info("🐛 canManageUsers: {}", canManageUsers());
            } else {
                logger.info("🐛 NO HAY USUARIO EN LA DB");
            }
            logger.info("🐛 ================================");
        } else {
            logger.info("🐛 NO HAY AUTHENTICATION");
        }
    }
}