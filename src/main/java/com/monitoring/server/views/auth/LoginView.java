package com.monitoring.server.views.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Login view with Auth0 integration
 */
@Route("login")
@PageTitle("Login - Server Monitor")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    // Default values for development
    private static final String DEFAULT_AUTH0_DOMAIN = "your-domain.auth0.com";
    private static final String DEFAULT_CLIENT_ID = "your-client-id";
    private static final String DEFAULT_AUDIENCE = "https://servermonitor.api";

    public LoginView() {
        setSpacing(false);
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        // Add custom CSS class for styling
        addClassName("login-view");
        
        // Create main container
        VerticalLayout loginCard = createLoginCard();
        add(loginCard);
        
        // Add some styling
        getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
    }

    private VerticalLayout createLoginCard() {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("login-card");
        card.setWidth("400px");
        card.setPadding(true);
        card.setSpacing(true);
        
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)")
            .set("backdrop-filter", "blur(10px)");

        // Logo/Icon
        Icon serverIcon = VaadinIcon.SERVER.create();
        serverIcon.setSize("64px");
        serverIcon.getStyle().set("color", "#667eea");

        // Title
        H1 title = new H1("Server Monitor");
        title.getStyle()
            .set("margin", "0")
            .set("color", "#333")
            .set("font-weight", "700");
        
        H2 subtitle = new H2("Sistema de Monitoreo");
        subtitle.getStyle()
            .set("margin", "0 0 24px 0")
            .set("color", "#666")
            .set("font-weight", "400")
            .set("font-size", "1.2rem");

        // Description
        Paragraph description = new Paragraph(
            "Accede al sistema de monitoreo de infraestructura. " +
            "Controla servidores, bases de datos y métricas del sistema en tiempo real."
        );
        description.getStyle()
            .set("text-align", "center")
            .set("color", "#555")
            .set("margin", "0 0 32px 0");

        // Login buttons
        VerticalLayout buttonLayout = createLoginButtons();

        // Security notice
        Div securityNotice = new Div();
        securityNotice.add(new Paragraph("🔒 Autenticación segura con Auth0"));
        securityNotice.getStyle()
            .set("text-align", "center")
            .set("color", "#888")
            .set("font-size", "0.9rem")
            .set("margin-top", "24px");

        card.add(
            serverIcon,
            title,
            subtitle,
            description,
            buttonLayout,
            securityNotice
        );

        card.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        return card;
    }

    private VerticalLayout createLoginButtons() {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(false);
        buttonLayout.setWidth("100%");

        // OAuth2 Login button (uses Spring Security OAuth2)
        Button loginButton = new Button("Iniciar Sesión");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.setWidth("100%");
        loginButton.setIcon(VaadinIcon.SIGN_IN.create());
        loginButton.addClickListener(e -> {
            // Redirect to Spring Security OAuth2 login
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/auth0");
        });

        // Alternative direct Auth0 button (for custom flow if needed)
        Button customLoginButton = new Button("Login con Auth0 (Custom)");
        customLoginButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        customLoginButton.setWidth("100%");
        customLoginButton.setIcon(VaadinIcon.GLOBE.create());
        customLoginButton.addClickListener(e -> initiateAuth0Login("email"));

        buttonLayout.add(loginButton, customLoginButton);
        return buttonLayout;
    }

    private void initiateAuth0Login(String connection) {
        String auth0Domain = getConfigValue("auth0.domain", DEFAULT_AUTH0_DOMAIN);
        String clientId = getConfigValue("auth0.clientId", DEFAULT_CLIENT_ID);
        String audience = getConfigValue("auth0.audience", DEFAULT_AUDIENCE);
        String redirectUri = getBaseUrl() + "/callback";
        
        StringBuilder authUrl = new StringBuilder();
        authUrl.append("https://").append(auth0Domain).append("/authorize?");
        authUrl.append("response_type=code");
        authUrl.append("&client_id=").append(clientId);
        authUrl.append("&redirect_uri=").append(redirectUri);
        authUrl.append("&scope=openid profile email");
        authUrl.append("&audience=").append(audience);
        
        if ("google".equals(connection)) {
            authUrl.append("&connection=google-oauth2");
        }
        
        // Redirect to Auth0
        UI.getCurrent().getPage().setLocation(authUrl.toString());
    }

    private String getBaseUrl() {
        try {
            // Get the HttpServletRequest from VaadinService
            HttpServletRequest request = (HttpServletRequest) VaadinService.getCurrentRequest();
            if (request != null) {
                String scheme = request.isSecure() ? "https" : "http";
                String serverName = request.getServerName();
                int serverPort = request.getServerPort();
                
                StringBuilder baseUrl = new StringBuilder();
                baseUrl.append(scheme).append("://").append(serverName);
                
                if ((scheme.equals("http") && serverPort != 80) || 
                    (scheme.equals("https") && serverPort != 443)) {
                    baseUrl.append(":").append(serverPort);
                }
                
                return baseUrl.toString();
            }
        } catch (Exception e) {
            // Fallback if we can't get the request
            System.out.println("Could not get request, using fallback URL");
        }
        
        // Fallback for development
        return "http://localhost:8080";
    }

    private String getConfigValue(String key, String defaultValue) {
        // Simple way to get config values - you can enhance this
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key.toUpperCase().replace(".", "_"));
        }
        return value != null ? value : defaultValue;
    }
}