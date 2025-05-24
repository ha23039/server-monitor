package com.monitoring.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

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

    public boolean isAdmin() {
        return hasRole("admin");
    }

    public boolean isUser() {
        return hasRole("user") || isAdmin();
    }

    public String getCurrentUserRoleDisplay() {
        if (isAdmin()) {
            return "Administrador";
        } else if (isUser()) {
            return "Usuario";
        } else {
            return "Sin Rol";
        }
    }

    // Métodos de compatibilidad con su sistema existente
    public boolean canAccessDashboard() {
        return isAuthenticated();
    }

    public boolean canManageDatabases() {
        return isAdmin();
    }

    public boolean canViewDatabases() {
        return isAuthenticated();
    }

    public boolean canConfigureAlerts() {
        return isAdmin();
    }

    public boolean canViewAlertConfig() {
        return isAuthenticated();
    }

    public boolean canManageUsers() {
        return isAdmin();
    }
}