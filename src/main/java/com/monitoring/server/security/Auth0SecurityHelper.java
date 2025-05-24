package com.monitoring.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

/**
 * Auth0 Security Helper for Server Monitoring System
 * Unified role system: admin, operator, viewer
 */
@Component
public class Auth0SecurityHelper {

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && 
               !(auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()));
    }

    public String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) auth.getPrincipal();
            String name = oidcUser.getFullName();
            return name != null ? name : oidcUser.getEmail();
        }
        return "Usuario Desconocido";
    }

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) auth.getPrincipal();
            return oidcUser.getEmail();
        }
        return null;
    }

    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    // ===== ROLES ESPECÍFICOS =====
    
    public boolean isAdmin() {
        return hasRole("admin");
    }

    public boolean isOperator() {
        return hasRole("operator");
    }

    public boolean isViewer() {
        return hasRole("viewer") || hasRole("user"); // "user" mapea a viewer
    }

    // ===== ROLES JERÁRQUICOS =====
    
    public boolean hasAdminPrivileges() {
        return isAdmin();
    }

    public boolean hasOperatorPrivileges() {
        return isAdmin() || isOperator();
    }

    public boolean hasViewerPrivileges() {
        return isAdmin() || isOperator() || isViewer();
    }

    public String getCurrentUserRoleDisplay() {
        if (isAdmin()) {
            return "Administrador del Sistema";
        } else if (isOperator()) {
            return "Operador de Sistemas";
        } else if (isViewer()) {
            return "Visualizador";
        } else {
            return "Sin Rol Asignado";
        }
    }

    // ===== PERMISOS ESPECÍFICOS DEL SISTEMA DE MONITOREO =====

    // Dashboard y Métricas
    public boolean canAccessDashboard() {
        return hasViewerPrivileges();
    }

    public boolean canViewMetrics() {
        return hasViewerPrivileges();
    }

    public boolean canExportMetrics() {
        return hasOperatorPrivileges();
    }

    // Bases de Datos
    public boolean canViewDatabases() {
        return hasViewerPrivileges();
    }

    public boolean canManageDatabases() {
        return hasOperatorPrivileges();
    }

    public boolean canTestDatabaseConnections() {
        return hasOperatorPrivileges();
    }

    // Alertas y Configuración
    public boolean canViewAlertConfig() {
        return hasViewerPrivileges();
    }

    public boolean canConfigureAlerts() {
        return hasOperatorPrivileges();
    }

    public boolean canAcknowledgeAlerts() {
        return hasOperatorPrivileges();
    }

    // Administración del Sistema
    public boolean canManageUsers() {
        return hasAdminPrivileges();
    }

    public boolean canChangeSystemSettings() {
        return hasAdminPrivileges();
    }

    public boolean canViewSystemLogs() {
        return hasOperatorPrivileges();
    }

    public boolean canManageSystemServices() {
        return hasAdminPrivileges();
    }

    // Monitoreo Avanzado
    public boolean canCreateCustomDashboards() {
        return hasOperatorPrivileges();
    }

    public boolean canScheduleReports() {
        return hasOperatorPrivileges();
    }

    public boolean canManageMonitoringAgents() {
        return hasAdminPrivileges();
    }

    public String getLogoutUrl() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLogoutUrl'");
    }
}