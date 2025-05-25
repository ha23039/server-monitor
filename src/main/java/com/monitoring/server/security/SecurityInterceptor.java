package com.monitoring.server.security;

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
 * ✅ MEJORADO: Security Interceptor sin logs excesivos
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
        String viewName = targetView.getSimpleName();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                                !"anonymousUser".equals(auth.getName());
        
        // ✅ SKIP: Vistas que permiten acceso anónimo
        if (targetView.isAnnotationPresent(com.vaadin.flow.server.auth.AnonymousAllowed.class)) {
            return;
        }
        
        // ✅ SKIP: Vistas de error y navegación del sistema
        if (viewName.contains("Error") || viewName.contains("NotFound") || 
            viewName.contains("AccessDenied") || viewName.contains("LoginView") ||
            viewName.equals("TestView")) {
            return;
        }
        
        // ✅ VERIFICACIONES DE SEGURIDAD
        
        // 1. Verificar autenticación básica
        if (targetView.isAnnotationPresent(RequiresAuth.class) || 
            targetView.isAnnotationPresent(RequiresViewer.class)) {
            
            if (!isAuthenticated) {
                System.out.println("❌ " + viewName + " - Redirigiendo a login - no autenticado");
                event.rerouteTo("login");
                return;
            }
        }
        
        // 2. Verificar permisos de operador
        if (targetView.isAnnotationPresent(RequiresOperator.class)) {
            if (!isAuthenticated) {
                System.out.println("❌ " + viewName + " - Redirigiendo a login - no autenticado");
                event.rerouteTo("login");
                return;
            }
            if (!hasOperatorPrivileges()) {
                System.out.println("❌ " + viewName + " - Acceso denegado - sin permisos de operator");
                event.rerouteTo("access-denied");
                return;
            }
        }
        
        // 3. Verificar permisos de administrador  
        if (targetView.isAnnotationPresent(RequiresAdmin.class)) {
            if (!isAuthenticated) {
                System.out.println("❌ " + viewName + " - Redirigiendo a login - no autenticado");
                event.rerouteTo("login");
                return;
            }
            if (!hasAdminPrivileges()) {
                System.out.println("❌ " + viewName + " - Acceso denegado - sin permisos de admin");
                event.rerouteTo("access-denied");
                return;
            }
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
    
    private boolean hasAdminPrivileges() {
        return hasRole("ROLE_admin");
    }
    
    private boolean hasOperatorPrivileges() {
        return hasRole("ROLE_admin") || hasRole("ROLE_operator");
    }
}