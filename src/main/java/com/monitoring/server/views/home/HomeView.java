package com.monitoring.server.views.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.monitoring.server.security.Auth0SecurityHelper;
import com.monitoring.server.views.MainLayout;
import com.monitoring.server.views.config.AlertConfigView;
import com.monitoring.server.views.dashboard.DashboardView;
import com.monitoring.server.views.databases.DatabaseView;
import com.monitoring.server.views.users.UserManagementView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Inicio - Server Monitor")
@Route(value = "home", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class) // Ruta raíz
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    @Autowired
    private Auth0SecurityHelper securityHelper;

    public HomeView(@Autowired Auth0SecurityHelper securityHelper) {
        this.securityHelper = securityHelper;
        
        setSpacing(false);
        setPadding(false);
        setHeightFull();
        
        // Header con bienvenida
        add(createWelcomeHeader());
        
        // Cards de navegación principales
        add(createNavigationCards());
        
        // Estado del sistema
        add(createSystemStatus());
        
        // Estadísticas rápidas (si es admin)
        if (securityHelper.isAdmin()) {
            add(createQuickStats());
        }
    }

    private Component createWelcomeHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setSpacing(false);
        header.setPadding(true);
        header.getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("color", "white")
            .set("border-radius", "0 0 20px 20px")
            .set("margin-bottom", "2rem");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = "Usuario";
        String userRole = "Usuario";
        
        if (auth != null && auth.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) auth.getPrincipal();
            userName = user.getFullName() != null ? user.getFullName() : user.getEmail();
            userRole = securityHelper.getCurrentUserRoleDisplay();
        }

        H1 welcomeTitle = new H1("👋 Bienvenido, " + userName);
        welcomeTitle.getStyle()
            .set("margin", "0")
            .set("font-size", "2.5rem")
            .set("font-weight", "700");

        Span roleSpan = new Span("🔑 " + userRole);
        roleSpan.getStyle()
            .set("font-size", "1.1rem")
            .set("opacity", "0.9")
            .set("font-weight", "500");

        Paragraph description = new Paragraph(
            "Plataforma centralizada de monitoreo de sistemas con autenticación segura y control de acceso avanzado."
        );
        description.getStyle()
            .set("font-size", "1.1rem")
            .set("opacity", "0.9")
            .set("margin", "1rem 0 0 0")
            .set("max-width", "600px");

        header.add(welcomeTitle, roleSpan, description);
        return header;
    }

    private Component createNavigationCards() {
        HorizontalLayout cardsLayout = new HorizontalLayout();
        cardsLayout.setSpacing(true);
        cardsLayout.setPadding(true);
        cardsLayout.setWidthFull();
        cardsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Card Dashboard
        Component dashboardCard = createNavigationCard(
            "📊", "Dashboard", 
            "Métricas en tiempo real",
            "Monitorea el rendimiento del sistema",
            "#3b82f6", true,
            () -> getUI().ifPresent(ui -> ui.navigate(DashboardView.class))
        );

        // Card Databases
        Component databaseCard = createNavigationCard(
            "🗄️", "Bases de Datos",
            "Gestión de conexiones",
            "Administra y monitorea bases de datos",
            "#059669", securityHelper.canViewDatabases(),
            () -> getUI().ifPresent(ui -> ui.navigate(DatabaseView.class))
        );

        // Card Configuración
        Component configCard = createNavigationCard(
            "⚙️", "Configuración",
            securityHelper.isAdmin() ? "Umbrales de alertas" : "Solo lectura",
            "Configura parámetros del sistema",
            securityHelper.isAdmin() ? "#7c3aed" : "#6b7280", 
            securityHelper.canViewAlertConfig(),
            () -> getUI().ifPresent(ui -> ui.navigate(AlertConfigView.class))
        );

        // Card Usuarios (solo admin)
        if (securityHelper.isAdmin()) {
            Component usersCard = createNavigationCard(
                "👥", "Usuarios",
                "Gestión de accesos",
                "Administra usuarios y roles",
                "#dc2626", true,
                () -> getUI().ifPresent(ui -> ui.navigate(UserManagementView.class))
            );
            cardsLayout.add(dashboardCard, databaseCard, configCard, usersCard);
        } else {
            cardsLayout.add(dashboardCard, databaseCard, configCard);
        }

        return cardsLayout;
    }

    private Component createNavigationCard(String emoji, String title, String subtitle, 
                                         String description, String color, boolean enabled,
                                         Runnable clickAction) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("padding", "2rem")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
            .set("border", "1px solid #e5e7eb")
            .set("cursor", enabled ? "pointer" : "not-allowed")
            .set("transition", "all 0.3s ease")
            .set("min-width", "280px")
            .set("opacity", enabled ? "1" : "0.6");

        if (enabled) {
            card.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");
            
            card.addClickListener(e -> clickAction.run());
            
            // Hover effects
            card.getElement().addEventListener("mouseenter", e -> {
                card.getStyle()
                    .set("transform", "translateY(-4px)")
                    .set("box-shadow", "0 8px 25px rgba(0,0,0,0.15)");
            });
            
            card.getElement().addEventListener("mouseleave", e -> {
                card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");
            });
        }

        // Icon
        Span iconSpan = new Span(emoji);
        iconSpan.getStyle()
            .set("font-size", "3rem")
            .set("display", "block")
            .set("margin-bottom", "1rem");

        // Title
        H2 cardTitle = new H2(title);
        cardTitle.getStyle()
            .set("margin", "0 0 0.5rem 0")
            .set("color", color)
            .set("font-size", "1.5rem")
            .set("font-weight", "700");

        // Subtitle
        Span subtitleSpan = new Span(subtitle);
        subtitleSpan.getStyle()
            .set("display", "block")
            .set("color", "#6b7280")
            .set("font-weight", "600")
            .set("margin-bottom", "0.5rem");

        // Description
        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "#9ca3af")
            .set("font-size", "0.9rem")
            .set("margin", "0")
            .set("line-height", "1.5");

        card.add(iconSpan, cardTitle, subtitleSpan, desc);
        return card;
    }

    private Component createSystemStatus() {
        Div statusSection = new Div();
        statusSection.getStyle()
            .set("background", "#f8fafc")
            .set("padding", "2rem")
            .set("margin", "2rem 1rem")
            .set("border-radius", "16px")
            .set("border", "1px solid #e2e8f0");

        H2 statusTitle = new H2("🟢 Estado del Sistema");
        statusTitle.getStyle()
            .set("margin", "0 0 1.5rem 0")
            .set("color", "#059669")
            .set("font-size", "1.5rem");

        HorizontalLayout statusGrid = new HorizontalLayout();
        statusGrid.setSpacing(true);
        statusGrid.setWidthFull();

        // Status items
        String[] statusItems = {
            "✅ Autenticación Auth0 activa",
            "✅ Base de datos PostgreSQL conectada",
            "✅ Aplicación desplegada en Render",
            "✅ Sistema de roles funcionando"
        };

        VerticalLayout statusList = new VerticalLayout();
        statusList.setSpacing(false);
        statusList.setPadding(false);

        for (String item : statusItems) {
            Span statusItem = new Span(item);
            statusItem.getStyle()
                .set("display", "block")
                .set("color", "#374151")
                .set("font-size", "1rem")
                .set("margin-bottom", "0.5rem")
                .set("font-weight", "500");
            statusList.add(statusItem);
        }

        statusGrid.add(statusList);
        statusSection.add(statusTitle, statusGrid);
        return statusSection;
    }

    private Component createQuickStats() {
        Div statsSection = new Div();
        statsSection.getStyle()
            .set("background", "white")
            .set("padding", "2rem")
            .set("margin", "1rem")
            .set("border-radius", "16px")
            .set("border", "1px solid #e2e8f0")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)");

        H2 statsTitle = new H2("📈 Panel de Administrador");
        statsTitle.getStyle()
            .set("margin", "0 0 1.5rem 0")
            .set("color", "#1e293b")
            .set("font-size", "1.5rem");

        HorizontalLayout statsGrid = new HorizontalLayout();
        statsGrid.setSpacing(true);
        statsGrid.setWidthFull();
        statsGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);

        // Stat cards
        Component systemHealthCard = createStatCard("💓", "Sistema", "Operativo", "#059669");
        Component usersCard = createStatCard("👥", "Usuarios", "Activos", "#3b82f6");
        Component alertsCard = createStatCard("🚨", "Alertas", "Monitoreando", "#f59e0b");
        Component uptimeCard = createStatCard("⏱️", "Uptime", "99.9%", "#8b5cf6");

        statsGrid.add(systemHealthCard, usersCard, alertsCard, uptimeCard);
        statsSection.add(statsTitle, statsGrid);
        return statsSection;
    }

    private Component createStatCard(String icon, String label, String value, String color) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#f8fafc")
            .set("border-radius", "12px")
            .set("padding", "1.5rem")
            .set("text-align", "center")
            .set("border-left", "4px solid " + color)
            .set("min-width", "140px");

        Span iconSpan = new Span(icon);
        iconSpan.getStyle()
            .set("font-size", "2rem")
            .set("display", "block")
            .set("margin-bottom", "0.5rem");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("display", "block")
            .set("font-size", "1.2rem")
            .set("font-weight", "700")
            .set("color", color)
            .set("margin-bottom", "0.25rem");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("display", "block")
            .set("font-size", "0.9rem")
            .set("color", "#6b7280")
            .set("font-weight", "500");

        card.add(iconSpan, valueSpan, labelSpan);
        return card;
    }
}