package com.monitoring.server.views.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
        setPadding(false);
        setSpacing(false);
        
        // Fondo con gradiente moderno
        getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("position", "relative");

        // Partículas decorativas de fondo
        createBackgroundElements();

        // Container principal
        Div mainContainer = createMainContainer();
        add(mainContainer);
    }

    private void createBackgroundElements() {
        // Círculos decorativos de fondo
        for (int i = 0; i < 6; i++) {
            Div circle = new Div();
            circle.getStyle()
                .set("position", "absolute")
                .set("background", "rgba(255,255,255,0.1)")
                .set("border-radius", "50%")
                .set("pointer-events", "none");
            
            if (i % 2 == 0) {
                circle.getStyle()
                    .set("width", "200px")
                    .set("height", "200px")
                    .set("top", (i * 150) + "px")
                    .set("left", "-100px");
            } else {
                circle.getStyle()
                    .set("width", "150px")
                    .set("height", "150px")
                    .set("top", (i * 120) + "px")
                    .set("right", "-75px");
            }
            add(circle);
        }
    }

    private Div createMainContainer() {
        Div container = new Div();
        container.getStyle()
            .set("background", "rgba(255, 255, 255, 0.95)")
            .set("backdrop-filter", "blur(20px)")
            .set("padding", "2.5rem")
            .set("border-radius", "24px")
            .set("box-shadow", "0 25px 50px rgba(0,0,0,0.25)")
            .set("max-width", "420px")
            .set("width", "90%")
            .set("position", "relative")
            .set("z-index", "10")
            .set("border", "1px solid rgba(255,255,255,0.2)");

        // Header con logo y título
        VerticalLayout header = createHeader();
        
        // Sección principal de autenticación
        VerticalLayout authSection = createAuthSection();

        container.add(header, authSection);
        return container;
    }

    private VerticalLayout createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setSpacing(false);
        header.setPadding(false);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("margin-bottom", "2rem");

        // Logo y título
        HorizontalLayout logoSection = new HorizontalLayout();
        logoSection.setAlignItems(Alignment.CENTER);
        logoSection.setSpacing(true);

        Icon serverIcon = new Icon(VaadinIcon.SERVER);
        serverIcon.setSize("2.5rem");
        serverIcon.getStyle().set("color", "#667eea");

        H1 title = new H1("Server Monitor");
        title.getStyle()
            .set("margin", "0")
            .set("color", "#1e293b")
            .set("font-weight", "700")
            .set("font-size", "2rem");

        logoSection.add(serverIcon, title);

        H3 subtitle = new H3("Bienvenido de vuelta");
        subtitle.getStyle()
            .set("margin", "0.5rem 0 0 0")
            .set("color", "#64748b")
            .set("font-weight", "400")
            .set("font-size", "1.1rem")
            .set("text-align", "center");

        header.add(logoSection, subtitle);
        return header;
    }

    private VerticalLayout createAuthSection() {
        VerticalLayout authSection = new VerticalLayout();
        authSection.setSpacing(true);
        authSection.setPadding(false);
        authSection.setAlignItems(Alignment.CENTER);

        // Botón principal de Auth0
        Button loginButton = createMainLoginButton();

        // Separador mejorado
        Div separator = createSeparator();

        // Botón de Google mejorado
        Button googleButton = createGoogleButton();

        authSection.add(loginButton, separator, googleButton);
        return authSection;
    }

    private Button createMainLoginButton() {
        Button loginButton = new Button("Iniciar Sesión");
        loginButton.setIcon(new Icon(VaadinIcon.ENVELOPE));
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.getStyle()
            .set("width", "100%")
            .set("height", "3rem")
            .set("font-size", "1rem")
            .set("font-weight", "600")
            .set("border-radius", "16px")
            .set("background", "linear-gradient(135deg, #667eea, #764ba2)")
            .set("border", "none")
            .set("box-shadow", "0 8px 25px rgba(102, 126, 234, 0.4)")
            .set("transition", "all 0.3s ease")
            .set("cursor", "pointer");

        // Efectos hover
        loginButton.getElement().addEventListener("mouseenter", e -> {
            loginButton.getStyle()
                .set("transform", "translateY(-2px)")
                .set("box-shadow", "0 12px 35px rgba(102, 126, 234, 0.5)");
        });

        loginButton.getElement().addEventListener("mouseleave", e -> {
            loginButton.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 8px 25px rgba(102, 126, 234, 0.4)");
        });

        loginButton.addClickListener(e -> {
            // Login automático - recuerda última cuenta
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/auth0");
        });

        return loginButton;
    }

    private Div createSeparator() {
        Div separator = new Div();
        separator.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("width", "100%")
            .set("margin", "1.5rem 0")
            .set("position", "relative");

        Div line1 = new Div();
        line1.getStyle()
            .set("flex", "1")
            .set("height", "1px")
            .set("background", "linear-gradient(to right, transparent, #e2e8f0, #e2e8f0)");

        Div orContainer = new Div();
        orContainer.getStyle()
            .set("background", "white")
            .set("padding", "0 1rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("min-width", "40px");

        Span orText = new Span("O");
        orText.getStyle()
            .set("color", "#9ca3af")
            .set("font-weight", "500")
            .set("font-size", "0.9rem");

        orContainer.add(orText);

        Div line2 = new Div();
        line2.getStyle()
            .set("flex", "1")
            .set("height", "1px")
            .set("background", "linear-gradient(to left, transparent, #e2e8f0, #e2e8f0)");

        separator.add(line1, orContainer, line2);
        return separator;
    }

    private Button createGoogleButton() {
        Button googleButton = new Button("Continuar con Google");
        
        // Crear el ícono de Google usando CSS
        Div googleIcon = new Div();
        googleIcon.getStyle()
            .set("width", "18px")
            .set("height", "18px")
            .set("background-image", "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%234285F4' d='M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z'/%3E%3Cpath fill='%2334A853' d='M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z'/%3E%3Cpath fill='%23FBBC05' d='M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z'/%3E%3Cpath fill='%23EA4335' d='M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z'/%3E%3C/svg%3E\")")
            .set("background-size", "cover")
            .set("margin-right", "0.5rem");

        googleButton.getElement().insertChild(0, googleIcon.getElement());
        
        googleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        googleButton.getStyle()
            .set("width", "100%")
            .set("height", "3rem")
            .set("font-size", "1rem")
            .set("font-weight", "500")
            .set("border-radius", "16px")
            .set("border", "2px solid #e5e7eb")
            .set("color", "#374151")
            .set("background", "white")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("transition", "all 0.3s ease")
            .set("cursor", "pointer");

        // Efectos hover para Google button
        googleButton.getElement().addEventListener("mouseenter", e -> {
            googleButton.getStyle()
                .set("border-color", "#d1d5db")
                .set("transform", "translateY(-1px)")
                .set("box-shadow", "0 6px 20px rgba(0,0,0,0.1)");
        });

        googleButton.getElement().addEventListener("mouseleave", e -> {
            googleButton.getStyle()
                .set("border-color", "#e5e7eb")
                .set("transform", "translateY(0)")
                .set("box-shadow", "none");
        });

        googleButton.addClickListener(e -> {
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/auth0?connection=google-oauth2&prompt=select_account");
        });

        return googleButton;
    }
}