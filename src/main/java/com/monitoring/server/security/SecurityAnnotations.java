package com.monitoring.server.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Custom security annotations for role-based access control
 */
public class SecurityAnnotations {

    /**
     * Requires SYSADMIN role
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ROLE_SYSADMIN')")
    public @interface RequiresSysAdmin {
    }

    /**
     * Requires OPERATOR role or higher (OPERATOR or SYSADMIN)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ROLE_SYSADMIN') or hasRole('ROLE_OPERATOR')")
    public @interface RequiresOperator {
    }

    /**
     * Requires any authenticated user (VIEWER, OPERATOR, or SYSADMIN)
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ROLE_SYSADMIN') or hasRole('ROLE_OPERATOR') or hasRole('ROLE_VIEWER')")
    public @interface RequiresAuth {
    }
}