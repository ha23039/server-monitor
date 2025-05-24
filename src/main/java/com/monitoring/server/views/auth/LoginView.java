package com.monitoring.server.views.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    public LoginView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();
        getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");

        // Container principal
        Div container = new Div();
        container.getStyle()
            .set("background", "white")
            .set("padding", "3rem")
            .set("border-radius", "15px")
            .set("box-shadow", "0 10px 30px rgba(0,0,0,0.3)")
            .set("text-align", "center")
            .set("max-width", "400px")
            .set("width", "100%");

        // Header
        createHeader(container);
        
        // Botones de autenticación
        createAuthButtons(container);
        
        // Información del sistema
        createSystemInfo(container);

        add(container);
    }

    private void createHeader(Div container) {
        // Título principal
        H1 title = new H1("🖥️ Server Monitor");
        title.getStyle()
            .set("color", "#333")
            .set("margin-bottom", "0.5rem")
            .set("font-size", "2.2rem");

        // Subtítulo
        H3 subtitle = new H3("Sistema de Monitoreo");
        subtitle.getStyle()
            .set("color", "#666")
            .set("margin-bottom", "2rem")
            .set("font-weight", "300")
            .set("font-size", "1.1rem");

        container.add(title, subtitle);
    }

    private void createAuthButtons(Div container) {
        // Botón principal - Continuar con Google (estilo profesional)
        Button googleButton = new Button("Continuar con Google");
        
        // Crear ícono de Google personalizado
        Icon googleIcon = VaadinIcon.GLOBE.create();
        googleIcon.setSize("16px");
        googleButton.setIcon(googleIcon);
        
        googleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        googleButton.getStyle()
            .set("width", "100%")
            .set("margin-bottom", "1rem")
            .set("font-size", "1rem")
            .set("padding", "12px 24px");
        
        googleButton.addClickListener(e -> {
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/auth0");
        });

        // Separador "O"
        Div separatorDiv = new Div();
        separatorDiv.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("margin", "1.5rem 0");

        Hr leftLine = new Hr();
        leftLine.getStyle().set("flex", "1").set("margin", "0 1rem 0 0");

        Paragraph orText = new Paragraph("O");
        orText.getStyle()
            .set("margin", "0")
            .set("color", "#6c757d")
            .set("font-size", "0.9rem");

        Hr rightLine = new Hr();
        rightLine.getStyle().set("flex", "1").set("margin", "0 0 0 1rem");

        separatorDiv.add(leftLine, orText, rightLine);

        // Botón secundario - Iniciar Sesión (email/password)
        Button emailButton = new Button("Iniciar Sesión");
        emailButton.setIcon(VaadinIcon.SIGN_IN.create());
        emailButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        emailButton.getStyle()
            .set("width", "100%")
            .set("margin-bottom", "1.5rem")
            .set("font-size", "1rem")
            .set("padding", "12px 24px");
        
        emailButton.addClickListener(e -> {
            // Mismo endpoint pero Auth0 mostrará opción de email/password
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/auth0");
        });

        container.add(googleButton, separatorDiv, emailButton);
    }

    private void createSystemInfo(Div container) {
        // Información del usuario de prueba (más discreta)
        Div infoCard = new Div();
        infoCard.getStyle()
            .set("background", "#f8f9fa")
            .set("padding", "1rem")
            .set("border-radius", "8px")
            .set("margin-top", "1rem")
            .set("border-left", "3px solid #007bff");

        Paragraph infoTitle = new Paragraph("👤 Usuario de Prueba");
        infoTitle.getStyle()
            .set("font-weight", "600")
            .set("margin-bottom", "0.5rem")
            .set("color", "#495057")
            .set("font-size", "0.9rem");

        Paragraph infoText = new Paragraph("ha23039@ues.edu.sv");
        infoText.getStyle()
            .set("margin", "0")
            .set("color", "#6c757d")
            .set("font-size", "0.85rem");

        infoCard.add(infoTitle, infoText);

        // Estado del sistema (compacto)
        Div statusDiv = new Div();
        statusDiv.getStyle()
            .set("margin-top", "1rem")
            .set("padding", "0.75rem")
            .set("background", "#d4edda")
            .set("border-radius", "6px")
            .set("border-left", "3px solid #28a745");

        Paragraph statusText = new Paragraph("🟢 Sistema configurado");
        statusText.getStyle()
            .set("margin", "0")
            .set("color", "#155724")
            .set("font-size", "0.85rem")
            .set("text-align", "center");

        statusDiv.add(statusText);

        container.add(infoCard, statusDiv);
    }
}