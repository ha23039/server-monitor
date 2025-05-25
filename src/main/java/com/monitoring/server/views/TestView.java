package com.monitoring.server.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("test")
@AnonymousAllowed
public class TestView extends VerticalLayout {
    
    public TestView() {
        add(new H1("🎉 ¡APLICACIÓN FUNCIONANDO!"));
        add(new Paragraph("Si ves esto, el deploy está correcto."));
        add(new Paragraph("Ahora solo necesitamos arreglar las rutas."));
    }
}