package com.monitoring.server.views.error;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Vista que se muestra cuando un usuario no tiene permisos para acceder a una página
 */
@Route("access-denied")
@PageTitle("Acceso Denegado - Server Monitor")
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout {

    public AccessDeniedView() {
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
        
        // Icono de acceso denegado
        Div iconDiv = new Div();
        iconDiv.getElement().setProperty("innerHTML", 
            VaadinIcon.LOCK.create().getElement().getOuterHTML());
        iconDiv.addClassName("text-error");
        iconDiv.getStyle().set("font-size", "4rem");
        iconDiv.getStyle().set("color", "var(--lumo-error-color)");
        
        // Título
        H1 title = new H1("Acceso Denegado");
        title.getStyle().set("color", "var(--lumo-error-text-color)");
        title.getStyle().set("margin-top", "1rem");
        
        // Subtítulo
        H3 subtitle = new H3("No tienes permisos para acceder a esta página");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("font-weight", "normal");
        
        // Mensaje explicativo
        Paragraph message = new Paragraph(
            "Tu cuenta no tiene los permisos necesarios para ver este contenido. " +
            "Si necesitas acceso, contacta con tu administrador del sistema."
        );
        message.getStyle().set("text-align", "center");
        message.getStyle().set("max-width", "500px");
        message.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        // Botón para volver al inicio
        Button homeButton = new Button("Volver al Inicio", VaadinIcon.HOME.create());
        homeButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        homeButton.addClassName("primary");
        homeButton.getStyle().set("margin-top", "2rem");
        
        // Contenedor principal
        VerticalLayout content = new VerticalLayout(
            iconDiv, title, subtitle, message, homeButton
        );
        content.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        content.setSpacing(true);
        content.setPadding(true);
        content.setMaxWidth("600px");
        content.getStyle().set("background", "var(--lumo-base-color)");
        content.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        content.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
        
        add(content);
    }
}