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
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

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
    
    // DEBUG: Agregar logs
    System.out.println("🔍 Navegando a: " + targetView.getSimpleName());
    System.out.println("🔍 Anotaciones: " + Arrays.toString(targetView.getAnnotations()));
    
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                            !"anonymousUser".equals(auth.getName());
    
    System.out.println("🔍 Usuario autenticado: " + isAuthenticated);
    
    if (targetView.isAnnotationPresent(RequiresOperator.class)) {
        System.out.println("🔍 Vista requiere Operator");
        if (!isAuthenticated) {
            System.out.println("❌ Redirigiendo a login - no autenticado");
            event.rerouteTo("login");
            return;
        }
        if (!hasRole("ROLE_admin") && !hasRole("ROLE_operator")) {
            System.out.println("❌ Redirigiendo a access-denied - sin permisos");
            event.rerouteTo("access-denied");
            return;
        }
        System.out.println("✅ Acceso permitido");
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