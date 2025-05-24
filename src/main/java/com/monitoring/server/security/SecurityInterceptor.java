package com.monitoring.server.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.monitoring.server.security.SecurityAnnotations.RequiresAdmin;
import com.monitoring.server.security.SecurityAnnotations.RequiresAuth;
import com.monitoring.server.security.SecurityAnnotations.RequiresOperator;
import com.monitoring.server.security.SecurityAnnotations.RequiresViewer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

/**
 * Security Interceptor for Vaadin Views
 * Handles route-level security based on custom annotations
 * Works in conjunction with Spring Security method-level security
 */
@Component
public class SecurityInterceptor implements VaadinServiceInitListener {

    @Autowired
    private Auth0SecurityHelper securityHelper;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().addBeforeEnterListener(this::checkSecurity);
        });
    }

    private void checkSecurity(BeforeEnterEvent event) {
        Class<?> targetView = event.getNavigationTarget();
        
        // Debug logging
        System.out.println("🔍 Navegando a: " + targetView.getSimpleName());
        System.out.println("🔍 Anotaciones: " + Arrays.toString(targetView.getAnnotations()));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                                !"anonymousUser".equals(auth.getName());
        
        System.out.println("🔍 Usuario autenticado: " + isAuthenticated);
        
        // Debug de roles
        if (auth != null && auth.getAuthorities() != null) {
            System.out.println("🔍 Roles del usuario:");
            auth.getAuthorities().forEach(authority -> 
                System.out.println("   - " + authority.getAuthority())
            );
            System.out.println("🔍 Tiene ROLE_admin: " + hasRole("ROLE_admin"));
            System.out.println("🔍 Tiene ROLE_operator: " + hasRole("ROLE_operator"));
            System.out.println("🔍 Tiene ROLE_viewer: " + hasRole("ROLE_viewer"));
        }
        
        // Verificar anotaciones de seguridad
        if (targetView.isAnnotationPresent(RequiresAuth.class) || 
            targetView.isAnnotationPresent(RequiresViewer.class)) {
            System.out.println("🔍 Vista requiere autenticación");
            if (!isAuthenticated) {
                System.out.println("❌ Redirigiendo a login - no autenticado");
                event.rerouteTo("login");
                return;
            }
            System.out.println("✅ Usuario autenticado - acceso permitido");
        }
        
        if (targetView.isAnnotationPresent(RequiresOperator.class)) {
            System.out.println("🔍 Vista requiere Operator o Admin");
            if (!isAuthenticated) {
                System.out.println("❌ Redirigiendo a login - no autenticado");
                event.rerouteTo("login");
                return;
            }
            if (!hasRole("ROLE_admin") && !hasRole("ROLE_operator")) {
                System.out.println("❌ Redirigiendo a access-denied - sin permisos de operator");
                event.rerouteTo("access-denied");
                return;
            }
            System.out.println("✅ Usuario tiene permisos de operator - acceso permitido");
        }
        
        if (targetView.isAnnotationPresent(RequiresAdmin.class)) {
            System.out.println("🔍 Vista requiere Admin");
            if (!isAuthenticated) {
                System.out.println("❌ Redirigiendo a login - no autenticado");
                event.rerouteTo("login");
                return;
            }
            if (!hasRole("ROLE_admin")) {
                System.out.println("❌ Redirigiendo a access-denied - sin permisos de admin");
                event.rerouteTo("access-denied");
                return;
            }
            System.out.println("✅ Usuario tiene permisos de admin - acceso permitido");
        }
    }
    
    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
        }
        return false;
    }
}