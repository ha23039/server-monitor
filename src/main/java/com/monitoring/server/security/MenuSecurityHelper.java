package com.monitoring.server.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.monitoring.server.data.entity.User;
import com.monitoring.server.data.entity.User.UserRole;
import com.monitoring.server.service.impl.AuthService;

/**
 * Helper class for menu and UI security decisions
 * ✅ CORREGIDO: Importaciones y manejo de Optional
 */
@Component
public class MenuSecurityHelper {

    @Autowired
    private AuthService authService;

    /**
     * Check if current user can access dashboard
     */
    public boolean canAccessDashboard() {
        return authService.canAccessDashboard();
    }

    /**
     * Check if current user can manage databases
     */
    public boolean canManageDatabases() {
        return authService.canManageDatabases();
    }

    /**
     * Check if current user can view databases
     */
    public boolean canViewDatabases() {
        return authService.isAuthenticated();
    }

    /**
     * Check if current user can configure alerts
     */
    public boolean canConfigureAlerts() {
        return authService.canConfigureAlerts();
    }

    /**
     * Check if current user can view alert configuration
     */
    public boolean canViewAlertConfig() {
        return authService.isAuthenticated();
    }

    /**
     * Check if current user can acknowledge alerts
     */
    public boolean canAcknowledgeAlerts() {
        return authService.isAuthenticated();
    }

    /**
     * Check if current user can manage users
     */
    public boolean canManageUsers() {
        return authService.canManageUsers();
    }

    /**
     * Get current user role for display purposes
     * ✅ CORREGIDO: Obtiene el rol real de la base de datos
     */
    public String getCurrentUserRoleDisplay() {
        try {
            // ✅ CAMBIO CRÍTICO: Obtener rol de la BD, no de Spring Security
            Optional<User> currentUserOpt = authService.getCurrentUser();
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                UserRole role = currentUser.getRole();
                String displayName = role.getDisplayName();
                
                // Debug temporal
                System.out.println("🏷️ Display rol para " + currentUser.getEmail() + ": " + displayName);
                
                return displayName;
            }
            
            // Fallback: usar roles de Spring Security
            return authService.getCurrentUserRoleDisplay();
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo role display: " + e.getMessage());
            return "Usuario";
        }
    }

    /**
     * Get current user name for display
     */
    public String getCurrentUserName() {
        try {
            Optional<User> currentUserOpt = authService.getCurrentUser();
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                String name = currentUser.getName();
                return name != null ? name : currentUser.getEmail();
            }
            
            return authService.getCurrentUserName();
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo user name: " + e.getMessage());
            return "Usuario";
        }
    }

    /**
     * Check if current user is authenticated
     */
    public boolean isAuthenticated() {
        return authService.isAuthenticated();
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        try {
            Optional<User> currentUserOpt = authService.getCurrentUser();
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                return currentUser.getRole() == UserRole.ADMIN;
            }
            
            // Fallback
            return authService.isAdmin();
        } catch (Exception e) {
            System.err.println("❌ Error verificando admin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if current user is regular user (not admin)
     */
    public boolean isUser() {
        return isAuthenticated() && !isAdmin();
    }
}