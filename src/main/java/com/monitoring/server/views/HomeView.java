package com.monitoring.server.views;

import com.monitoring.server.security.SecurityAnnotations.RequiresAuth;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Inicio - Server Monitor")
@Route(value = "")  // ✅ RUTA RAÍZ
@RouteAlias(value = "home")  // ✅ ALIAS PARA /home
@RequiresAuth
public class HomeView extends VerticalLayout {
    
    public HomeView() {
        add(new H1("🏠 ¡BIENVENIDO AL SERVER MONITOR!"));
        add(new Paragraph("Sistema funcionando correctamente."));
        add(new Paragraph("Autenticación exitosa."));
    }
}