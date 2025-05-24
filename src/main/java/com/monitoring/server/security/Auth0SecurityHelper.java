package com.monitoring.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

/**
 * Auth0 Security Helper for Server Monitoring System
 * Unified role system: admin, operator, viewer, user
 * IMPORTANTE: Roles con prefijo ROLE_ para Spring Security
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
        if (auth == null) {
            return false;
        }
        
        // Asegurar que el rol tenga el prefijo ROLE_
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        
        boolean hasRole = auth.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
        
        // Debug logging
        System.out.println("🔍 Verificando rol: " + roleWithPrefix + " = " + hasRole);
        System.out.println("🔍 Authorities disponibles: " + auth.getAuthorities());
        
        return hasRole;
    }

    // ===== ROLES ESPECÍFICOS =====
    
    public boolean isAdmin() {
        return hasRole("admin"); // Se convertirá a ROLE_admin internamente
    }

    public boolean isOperator() {
        return hasRole("operator"); // Se convertirá a ROLE_operator internamente
    }

    public boolean isViewer() {
        return hasRole("viewer") || hasRole("user"); // viewer o user mapean a visualizador
    }

    public boolean isUser() {
        return hasRole("user"); // Se convertirá a ROLE_user internamente
    }

    // ===== ROLES JERÁRQUICOS =====
    
    public boolean hasAdminPrivileges() {
        return isAdmin();
    }

    public boolean hasOperatorPrivileges() {
        return isAdmin() || isOperator();
    }

    public boolean hasViewerPrivileges() {
        return isAdmin() || isOperator() || isViewer() || isUser();
    }

    public String getCurrentUserRoleDisplay() {
        if (isAdmin()) {
            return "Administrador del Sistema";
        } else if (isOperator()) {
            return "Operador de Sistemas";
        } else if (isViewer()) {
            return "Visualizador";
        } else if (isUser()) {
            return "Usuario";
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

    // ===== MÉTODOS DE DEBUG =====
    
    public void debugCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("🐛 =====DEBUG USUARIO ACTUAL=====");
            System.out.println("🐛 Name: " + auth.getName());
            System.out.println("🐛 Principal: " + auth.getPrincipal());
            System.out.println("🐛 Authenticated: " + auth.isAuthenticated());
            System.out.println("🐛 Authorities: " + auth.getAuthorities());
            System.out.println("🐛 isAdmin: " + isAdmin());
            System.out.println("🐛 isOperator: " + isOperator());
            System.out.println("🐛 isViewer: " + isViewer());
            System.out.println("🐛 isUser: " + isUser());
            System.out.println("🐛 ================================");
        } else {
            System.out.println("🐛 NO HAY AUTHENTICATION");
        }
    }

    public String getLogoutUrl() {
        // TODO: Implementar logout de Auth0
        return "/logout";
    }
}