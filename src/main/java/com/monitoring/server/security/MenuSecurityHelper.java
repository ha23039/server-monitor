package com.monitoring.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.monitoring.server.data.entity.User.UserRole;
import com.monitoring.server.service.impl.AuthService;

/**
 * Helper class for menu and UI security decisions
 */
@Component
public class MenuSecurityHelper {

    @Autowired
    private AuthService authService;

    /**
     * Check if current user can access dashboard
     */
    public boolean canAccessDashboard() {
        return authService.getCurrentUser().isPresent();
    }

    /**
     * Check if current user can manage databases
     */
    public boolean canManageDatabases() {
        return authService.hasRole(UserRole.SYSADMIN);
    }

    /**
     * Check if current user can view databases
     */
    public boolean canViewDatabases() {
        return authService.hasRole(UserRole.OPERATOR);
    }

    /**
     * Check if current user can configure alerts
     */
    public boolean canConfigureAlerts() {
        return authService.hasRole(UserRole.SYSADMIN);
    }

    /**
     * Check if current user can view alert configuration
     */
    public boolean canViewAlertConfig() {
        return authService.hasRole(UserRole.OPERATOR);
    }

    /**
     * Check if current user can acknowledge alerts
     */
    public boolean canAcknowledgeAlerts() {
        return authService.hasRole(UserRole.OPERATOR);
    }

    /**
     * Check if current user can manage users
     */
    public boolean canManageUsers() {
        return authService.hasRole(UserRole.SYSADMIN);
    }

    /**
     * Get current user role for display purposes
     */
    public String getCurrentUserRoleDisplay() {
        UserRole role = authService.getCurrentUserRole();
        switch (role) {
            case SYSADMIN:
                return "Administrador del Sistema";
            case OPERATOR:
                return "Operador";
            case VIEWER:
                return "Visualizador";
            default:
                return "Usuario";
        }
    }

    /**
     * Get current user name for display
     */
    public String getCurrentUserName() {
        return authService.getCurrentUser()
            .map(user -> user.getName() != null ? user.getName() : user.getEmail())
            .orElse("Usuario");
    }

    /**
     * Check if current user is authenticated
     */
    public boolean isAuthenticated() {
        return authService.getCurrentUser().isPresent();
    }
}