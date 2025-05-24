package com.monitoring.server.views.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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
            .set("max-width", "450px")
            .set("width", "100%");

        // Título principal
        H1 title = new H1("🖥️ Server Monitor");
        title.getStyle()
            .set("color", "#333")
            .set("margin-bottom", "0.5rem")
            .set("font-size", "2.5rem");

        // Subtítulo
        H3 subtitle = new H3("Parcial 2 - Sistema de Monitoreo");
        subtitle.getStyle()
            .set("color", "#666")
            .set("margin-bottom", "2rem")
            .set("font-weight", "300");

        // Descripción
        Paragraph description = new Paragraph("Accede al sistema de monitoreo con autenticación Auth0");
        description.getStyle()
            .set("color", "#888")
            .set("margin-bottom", "2rem");

        // Botón de login principal (ÚNICO BOTÓN)
        Button loginButton = new Button("🔐 Iniciar Sesión con Auth0");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.setIcon(VaadinIcon.SIGN_IN.create());
        loginButton.getStyle()
            .set("width", "100%")
            .set("margin-bottom", "2rem") // Aumentado de 1rem a 2rem
            .set("font-size", "1.1rem");
        loginButton.addClickListener(e -> {
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/auth0");
        });

        // Información de credenciales
        Div credentialsInfo = new Div();
        credentialsInfo.getStyle()
            .set("background", "#f8f9fa")
            .set("padding", "1rem")
            .set("border-radius", "8px")
            .set("border-left", "4px solid #007bff");

        Paragraph credTitle = new Paragraph("📋 Credenciales de Prueba:");
        credTitle.getStyle()
            .set("font-weight", "bold")
            .set("margin-bottom", "0.5rem")
            .set("color", "#495057");

        Paragraph credUser = new Paragraph("👤 Usuario: ha23039@ues.edu.sv");
        Paragraph credRole = new Paragraph("🔑 Rol: Administrador");
        Paragraph credNote = new Paragraph("💡 Configurar usuario en Auth0 Dashboard");
        
        credUser.getStyle().set("margin", "0.25rem 0").set("color", "#6c757d");
        credRole.getStyle().set("margin", "0.25rem 0").set("color", "#6c757d");
        credNote.getStyle().set("margin", "0.25rem 0").set("color", "#dc3545").set("font-style", "italic");

        credentialsInfo.add(credTitle, credUser, credRole, credNote);

        // Features implementadas
        Div featuresDiv = new Div();
        featuresDiv.getStyle()
            .set("margin-top", "1.5rem")
            .set("text-align", "left");

        Paragraph featuresTitle = new Paragraph("✨ Características:");
        featuresTitle.getStyle()
            .set("font-weight", "bold")
            .set("margin-bottom", "0.5rem")
            .set("text-align", "center");

        Div featuresList = new Div();
        featuresList.add(
            createFeature("🔐", "Autenticación OAuth2 con Auth0"),
            createFeature("👥", "Control de roles (Admin/User)"),
            createFeature("📊", "Monitoreo en tiempo real"),
            createFeature("🚀", "Desplegado en Render"),
            createFeature("🗄️", "Base de datos PostgreSQL en Neon")
        );

        featuresDiv.add(featuresTitle, featuresList);

        // Estado del sistema
        Div statusDiv = new Div();
        statusDiv.getStyle()
            .set("margin-top", "1rem")
            .set("padding", "0.75rem")
            .set("background", "#d4edda")
            .set("border-radius", "8px")
            .set("border-left", "4px solid #28a745");

        Paragraph statusText = new Paragraph("🟢 Sistema configurado y listo para autenticación");
        statusText.getStyle()
            .set("margin", "0")
            .set("color", "#155724")
            .set("font-weight", "500")
            .set("text-align", "center")
            .set("font-size", "0.9rem");

        statusDiv.add(statusText);

        // Agregar todos los componentes al container (SIN el botón de Google)
        container.add(title, subtitle, description, loginButton, credentialsInfo, featuresDiv, statusDiv);
        add(container);
    }

    private Div createFeature(String icon, String text) {
        Div feature = new Div();
        feature.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("margin-bottom", "0.5rem")
            .set("color", "#6c757d");

        Div iconDiv = new Div();
        iconDiv.setText(icon);
        iconDiv.getStyle()
            .set("margin-right", "0.5rem")
            .set("font-size", "1.2rem");

        Div textDiv = new Div();
        textDiv.setText(text);

        feature.add(iconDiv, textDiv);
        return feature;
    }
}