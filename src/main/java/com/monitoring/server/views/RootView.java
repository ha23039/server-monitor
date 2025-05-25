package com.monitoring.server.views;

import com.monitoring.server.security.SecurityAnnotations.RequiresAuth;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Inicio - Server Monitor")
@Route(value = "")  // ✅ Ruta raíz SIN MainLayout
@RequiresAuth
public class RootView extends VerticalLayout {
    
    public RootView() {
        add(new H1("🚀 SERVER MONITOR - INICIO"));
        add(new Paragraph("¡Autenticación exitosa!"));
        
        // Enlaces de navegación manual 
        add(new Anchor("/home", "Ir a Home"));
        add(new Anchor("/test", "Ir a Test"));
    }
} 
    
