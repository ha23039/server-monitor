package com.monitoring.server.views.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
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

        Div container = new Div();
        container.getStyle()
            .set("background", "white")
            .set("padding", "3rem")
            .set("border-radius", "15px")
            .set("box-shadow", "0 10px 30px rgba(0,0,0,0.3)")
            .set("text-align", "center")
            .set("max-width", "450px")
            .set("width", "100%");

        // Header
        H1 title = new H1("🖥️ Server Monitor");
        title.getStyle()
            .set("color", "#333")
            .set("margin-bottom", "0.5rem")
            .set("font-size", "2.2rem");

        H3 subtitle = new H3("Sistema de Monitoreo");
        subtitle.getStyle()
            .set("color", "#666")
            .set("margin-bottom", "1.5rem")
            .set("font-weight", "300");

        // Instrucciones claras
        Paragraph instructions = new Paragraph("Inicia sesión o crea una cuenta nueva:");
        instructions.getStyle()
            .set("color", "#666")
            .set("margin-bottom", "1.5rem")
            .set("font-size", "1rem");

        // Botón principal
        Button loginButton = new Button("Iniciar Sesión / Registrarse");
        loginButton.setIcon(VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.getStyle()
            .set("width", "100%")
            .set("margin-bottom", "1rem")
            .set("font-size", "1rem")
            .set("padding", "14px 24px");
        
        loginButton.addClickListener(e -> {
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/auth0");
        });

        // Explicación de opciones
        Div optionsDiv = new Div();
        optionsDiv.getStyle()
            .set("background", "#f8f9fa")
            .set("padding", "1rem")
            .set("border-radius", "8px")
            .set("margin", "1rem 0")
            .set("text-align", "left");

        Paragraph optionsTitle = new Paragraph("🔹 Opciones disponibles:");
        optionsTitle.getStyle()
            .set("font-weight", "600")
            .set("margin-bottom", "0.5rem")
            .set("color", "#495057");

        Paragraph optionsList = new Paragraph(
            "• Crear cuenta nueva con email y contraseña\n" +
            "• Iniciar sesión con cuenta existente\n" +
            "• Autenticarse con Google"
        );
        optionsList.getStyle()
            .set("margin", "0")
            .set("color", "#6c757d")
            .set("font-size", "0.9rem")
            .set("white-space", "pre-line");

        optionsDiv.add(optionsTitle, optionsList);

        // Separador
        Hr separator = new Hr();
        separator.getStyle().set("margin", "2rem 0 1rem 0");

        // Usuario de prueba
        Div testUserDiv = new Div();
        testUserDiv.getStyle()
            .set("background", "#e7f3ff")
            .set("padding", "1rem")
            .set("border-radius", "8px")
            .set("border-left", "3px solid #0066cc");

        Paragraph testUserTitle = new Paragraph("👤 Usuario de Prueba Configurado");
        testUserTitle.getStyle()
            .set("font-weight", "600")
            .set("margin-bottom", "0.5rem")
            .set("color", "#0066cc");

        Paragraph testUserInfo = new Paragraph("📧 ha23039@ues.edu.sv\n🔑 Rol: Administrador");
        testUserInfo.getStyle()
            .set("margin", "0")
            .set("color", "#0066cc")
            .set("font-size", "0.85rem")
            .set("white-space", "pre-line");

        testUserDiv.add(testUserTitle, testUserInfo);

        // Instrucciones de registro
        Div registrationDiv = new Div();
        registrationDiv.getStyle()
            .set("background", "#fff3cd")
            .set("padding", "1rem")
            .set("border-radius", "8px")
            .set("border-left", "3px solid #ffc107")
            .set("margin-top", "1rem");

        Paragraph registrationTitle = new Paragraph("📝 Para crear cuenta nueva:");
        registrationTitle.getStyle()
            .set("font-weight", "600")
            .set("margin-bottom", "0.5rem")
            .set("color", "#856404");

        Paragraph registrationSteps = new Paragraph(
            "1. Haz clic en 'Iniciar Sesión / Registrarse'\n" +
            "2. En la pantalla de Auth0, busca el link 'Sign up'\n" +
            "3. Llena el formulario con tu email y contraseña\n" +
            "4. ¡Tu cuenta será creada automáticamente!"
        );
        registrationSteps.getStyle()
            .set("margin", "0")
            .set("color", "#856404")
            .set("font-size", "0.85rem")
            .set("white-space", "pre-line");

        registrationDiv.add(registrationTitle, registrationSteps);

        container.add(title, subtitle, instructions, loginButton, optionsDiv, 
                     separator, testUserDiv, registrationDiv);
        add(container);
    }
}