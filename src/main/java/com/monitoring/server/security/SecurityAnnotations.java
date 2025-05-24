package com.monitoring.server.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Custom security annotations for Server Monitoring System
 * Unified with Auth0 roles: admin, operator, viewer (WITH ROLE_ prefix for Spring Security)
 * 
 * Role Hierarchy:
 * - ADMIN: Full system access (users, settings, all monitoring)
 * - OPERATOR: Monitoring operations (databases, alerts, configurations)  
 * - VIEWER: Read-only access (dashboards, metrics)
 */
public class SecurityAnnotations {

    /**
     * Requires ADMIN role - Full system access
     * Can manage users, system settings, and all monitoring functions
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ROLE_admin')")
    public @interface RequiresAdmin {
    }

    /**
     * Requires OPERATOR role or higher (OPERATOR or ADMIN)
     * Can configure monitoring, manage databases, acknowledge alerts
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_operator')")
    public @interface RequiresOperator {
    }

    /**
     * Requires any authenticated user (VIEWER, OPERATOR, or ADMIN)
     * Can view dashboards, metrics, and basic monitoring data
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_operator') or hasRole('ROLE_viewer')")
    public @interface RequiresAuth {
    }

    /**
     * Requires VIEWER role or higher (any authenticated user)
     * Alias for RequiresAuth for clarity in code
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_operator') or hasRole('ROLE_viewer')")
    public @interface RequiresViewer {
    }
}