package com.monitoring.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.monitoring.server.service.impl.AuthService;

/**
 * Helper class for menu and UI security decisions
 * Actualizado para roles simplificados: ADMIN, USER
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
     * Check if current user can view databases (simplified - same as manage for now)
     */
    public boolean canViewDatabases() {
        return authService.isAuthenticated(); // Cualquier usuario autenticado puede ver
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
        return authService.isAuthenticated(); // Cualquier usuario autenticado puede ver
    }

    /**
     * Check if current user can acknowledge alerts
     */
    public boolean canAcknowledgeAlerts() {
        return authService.isAuthenticated(); // Cualquier usuario autenticado puede reconocer alertas
    }

    /**
     * Check if current user can manage users
     */
    public boolean canManageUsers() {
        return authService.canManageUsers();
    }

    /**
     * Get current user role for display purposes
     */
    public String getCurrentUserRoleDisplay() {
        return authService.getCurrentUserRoleDisplay();
    }

    /**
     * Get current user name for display
     */
    public String getCurrentUserName() {
        return authService.getCurrentUserName();
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
        return authService.isAdmin();
    }

    /**
     * Check if current user is regular user
     */
    public boolean isUser() {
        return authService.isAuthenticated() && !authService.isAdmin();
    }
}