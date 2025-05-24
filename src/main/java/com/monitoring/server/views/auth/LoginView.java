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
            .set("background", "white")
            .set("padding", "3rem")
            .set("border-radius", "20px")
            .set("box-shadow", "0 20px 60px rgba(0,0,0,0.3)")
            .set("backdrop-filter", "blur(10px)")
            .set("max-width", "500px")
            .set("width", "90%")
            .set("position", "relative")
            .set("z-index", "10");

        // Header con logo y título
        VerticalLayout header = createHeader();
        
        // Sección principal de autenticación
        VerticalLayout authSection = createAuthSection();
        
        // Información adicional
        VerticalLayout infoSection = createInfoSection();

        container.add(header, authSection, infoSection);
        return container;
    }

    private VerticalLayout createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setSpacing(false);
        header.setPadding(false);
        header.setAlignItems(Alignment.CENTER);

        // Logo y título
        HorizontalLayout logoSection = new HorizontalLayout();
        logoSection.setAlignItems(Alignment.CENTER);
        logoSection.setSpacing(true);

        Icon serverIcon = new Icon(VaadinIcon.SERVER);
        serverIcon.setSize("3rem");
        serverIcon.getStyle().set("color", "#667eea");

        H1 title = new H1("Server Monitor");
        title.getStyle()
            .set("margin", "0")
            .set("color", "#2c3e50")
            .set("font-weight", "700")
            .set("font-size", "2.5rem");

        logoSection.add(serverIcon, title);

        H3 subtitle = new H3("Sistema de Monitoreo Empresarial");
        subtitle.getStyle()
            .set("margin", "0.5rem 0 2rem 0")
            .set("color", "#64748b")
            .set("font-weight", "400")
            .set("text-align", "center");

        header.add(logoSection, subtitle);
        return header;
    }

    private VerticalLayout createAuthSection() {
        VerticalLayout authSection = new VerticalLayout();
        authSection.setSpacing(true);
        authSection.setPadding(false);
        authSection.getStyle().set("margin-bottom", "2rem");

        // Título de la sección
        Paragraph welcomeText = new Paragraph("Accede a tu cuenta");
        welcomeText.getStyle()
            .set("font-size", "1.2rem")
            .set("font-weight", "600")
            .set("color", "#374151")
            .set("text-align", "center")
            .set("margin", "0 0 1.5rem 0");

        // Botón principal de Auth0
        Button loginButton = createMainLoginButton();

        // Separador
        Div separator = createSeparator();

        // Botón de Google
        Button googleButton = createGoogleButton();

        // Información de usuarios de prueba
        Div testUserInfo = createTestUserInfo();

        authSection.add(welcomeText, loginButton, separator, googleButton, testUserInfo);
        return authSection;
    }

    private Button createMainLoginButton() {
        Button loginButton = new Button("Iniciar Sesión con Email");
        loginButton.setIcon(new Icon(VaadinIcon.ENVELOPE));
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.getStyle()
            .set("width", "100%")
            .set("height", "3.5rem")
            .set("font-size", "1.1rem")
            .set("font-weight", "600")
            .set("border-radius", "12px")
            .set("background", "linear-gradient(45deg, #667eea, #764ba2)")
            .set("border", "none");

        loginButton.addClickListener(e -> {
            // Redirigir a Auth0 sin el parámetro de Google para mostrar la pantalla universal
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/auth0");
        });

        return loginButton;
    }

    private Div createSeparator() {
        Div separator = new Div();
        separator.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("margin", "1.5rem 0")
            .set("color", "#9ca3af");

        Div line1 = new Div();
        line1.getStyle()
            .set("flex", "1")
            .set("height", "1px")
            .set("background", "#e5e7eb");

        Span orText = new Span("O");
        orText.getStyle()
            .set("padding", "0 1rem")
            .set("font-weight", "500");

        Div line2 = new Div();
        line2.getStyle()
            .set("flex", "1")
            .set("height", "1px")
            .set("background", "#e5e7eb");

        separator.add(line1, orText, line2);
        return separator;
    }

    private Button createGoogleButton() {
        Button googleButton = new Button("Continuar con Google");
        
        // Crear el ícono de Google usando CSS
        Div googleIcon = new Div();
        googleIcon.getStyle()
            .set("width", "20px")
            .set("height", "20px")
            .set("background-image", "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='%234285F4' d='M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z'/%3E%3Cpath fill='%2334A853' d='M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z'/%3E%3Cpath fill='%23FBBC05' d='M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z'/%3E%3Cpath fill='%23EA4335' d='M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z'/%3E%3C/svg%3E\")")
            .set("background-size", "cover")
            .set("margin-right", "0.5rem");

        googleButton.getElement().insertChild(0, googleIcon.getElement());
        
        googleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        googleButton.getStyle()
            .set("width", "100%")
            .set("height", "3.5rem")
            .set("font-size", "1.1rem")
            .set("font-weight", "500")
            .set("border-radius", "12px")
            .set("border", "2px solid #e5e7eb")
            .set("color", "#374151")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        googleButton.addClickListener(e -> {
            // Redirigir directamente con Google como hint
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/auth0?connection=google-oauth2");
        });

        return googleButton;
    }

    private Div createTestUserInfo() {
        Div testInfo = new Div();
        testInfo.getStyle()
            .set("background", "linear-gradient(135deg, #e0f2fe, #f3e5f5)")
            .set("padding", "1.5rem")
            .set("border-radius", "12px")
            .set("border-left", "4px solid #667eea")
            .set("margin-top", "1.5rem");

        Span title = new Span("👨‍💼 Usuario de Prueba");
        title.getStyle()
            .set("display", "block")
            .set("font-weight", "600")
            .set("color", "#1e293b")
            .set("margin-bottom", "0.5rem");

        Span email = new Span("📧 ha23039@ues.edu.sv");
        email.getStyle()
            .set("display", "block")
            .set("color", "#475569")
            .set("font-size", "0.9rem");

        Span role = new Span("🔑 Rol: Administrador");
        role.getStyle()
            .set("display", "block")
            .set("color", "#059669")
            .set("font-size", "0.9rem")
            .set("font-weight", "500");

        testInfo.add(title, email, role);
        return testInfo;
    }

    private VerticalLayout createInfoSection() {
        VerticalLayout infoSection = new VerticalLayout();
        infoSection.setSpacing(false);
        infoSection.setPadding(false);

        // Features del sistema
        Div featuresCard = new Div();
        featuresCard.getStyle()
            .set("background", "#f8fafc")
            .set("padding", "1.5rem")
            .set("border-radius", "12px")
            .set("border", "1px solid #e2e8f0");

        Span featuresTitle = new Span("✨ Características del Sistema");
        featuresTitle.getStyle()
            .set("display", "block")
            .set("font-weight", "600")
            .set("color", "#1e293b")
            .set("margin-bottom", "1rem");

        VerticalLayout featuresList = new VerticalLayout();
        featuresList.setSpacing(false);
        featuresList.setPadding(false);

        String[] features = {
            "🚀 Monitoreo en tiempo real",
            "🔒 Autenticación segura con Auth0",
            "📊 Dashboard interactivo",
            "🎯 Control de acceso por roles",
            "📱 Diseño responsivo moderno"
        };

        for (String feature : features) {
            Span featureItem = new Span(feature);
            featureItem.getStyle()
                .set("display", "block")
                .set("color", "#64748b")
                .set("font-size", "0.9rem")
                .set("margin-bottom", "0.5rem");
            featuresList.add(featureItem);
        }

        featuresCard.add(featuresTitle, featuresList);
        infoSection.add(featuresCard);

        return infoSection;
    }
}