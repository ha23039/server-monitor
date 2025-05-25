package com.monitoring.server.views.home;

import com.monitoring.server.security.SecurityAnnotations.RequiresAuth;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
@PageTitle("Inicio - Server Monitor")
@Route(value = "home")  // ✅ SIN layout = MainLayout.class
@RequiresAuth
public class HomeView extends VerticalLayout {
    
    public HomeView() {
        add(new H1("🏠 ¡BIENVENIDO AL SERVER MONITOR!"));
        add(new Paragraph("Sistema funcionando correctamente."));
        add(new Paragraph("Autenticación exitosa con rol OPERATOR."));
        
        // Agregar un enlace de prueba
        Anchor testLink = new Anchor("/test", "Ir a Test");
        add(testLink);
    }
}