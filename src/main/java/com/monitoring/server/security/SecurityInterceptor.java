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
        
        // Verificar si el usuario está autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                                !"anonymousUser".equals(auth.getName());
        
        // Verificar anotaciones de seguridad
        if (targetView.isAnnotationPresent(RequiresAuth.class) || 
            targetView.isAnnotationPresent(RequiresViewer.class)) {
            if (!isAuthenticated) {
                event.rerouteTo("login");
                return;
            }
        }
        
        if (targetView.isAnnotationPresent(RequiresOperator.class)) {
            if (!isAuthenticated) {
                event.rerouteTo("login");
                return;
            }
            if (!hasRole("ROLE_admin") && !hasRole("ROLE_operator")) {
                event.rerouteTo("access-denied");
                return;
            }
        }
        
        if (targetView.isAnnotationPresent(RequiresAdmin.class)) {
            if (!isAuthenticated) {
                event.rerouteTo("login");
                return;
            }
            if (!hasRole("ROLE_admin")) {
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
}