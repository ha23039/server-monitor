package com.monitoring.server.views.home;

import org.springframework.beans.factory.annotation.Autowired;

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
@Route(value = "", layout = MainLayout.class) 
@RouteAlias(value = "home", layout = MainLayout.class) 
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    private Auth0SecurityHelper securityHelper;

    public HomeView(@Autowired Auth0SecurityHelper securityHelper) {
        this.securityHelper = securityHelper;
        
        setSpacing(true);
        setPadding(true);
        setHeightFull();
        
        // Header principal
        add(createWelcomeHeader());
        
        // Cards de navegación
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
        header.setPadding(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 welcomeTitle = new H1("🖥️ MONITOR DE SERVIDORES");
        welcomeTitle.getStyle()
            .set("margin", "0")
            .set("color", "#2c3e50")
            .set("text-align", "center")
            .set("font-weight", "700");

        Paragraph description = new Paragraph(
            "Bienvenido a la plataforma centralizada de monitoreo de sistemas con autenticación Auth0. " +
            "Obtenga visibilidad completa del rendimiento de su infraestructura, bases de datos y aplicaciones. " +
            "Reciba alertas proactivas, analice tendencias y optimice sus recursos para garantizar la máxima " +
            "disponibilidad y eficiencia con control de roles integrado."
        );
        description.getStyle()
            .set("font-size", "1.1rem")
            .set("color", "#6c757d")
            .set("text-align", "center")
            .set("max-width", "800px")
            .set("margin", "1rem auto");

        header.add(welcomeTitle, description);
        return header;
    }

    private Component createNavigationCards() {
        HorizontalLayout cardsLayout = new HorizontalLayout();
        cardsLayout.setSpacing(true);
        cardsLayout.setWidthFull();
        cardsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Card Dashboard
        Component dashboardCard = createNavigationCard(
            VaadinIcon.DASHBOARD, "Dashboard", 
            "Métricas en tiempo real",
            "#3b82f6", true,
            () -> getUI().ifPresent(ui -> ui.navigate(DashboardView.class))
        );

        // Card Databases
        Component databaseCard = createNavigationCard(
            VaadinIcon.DATABASE, "Bases de Datos",
            "Gestión de conexiones",
            "#059669", securityHelper.canViewDatabases(),
            () -> getUI().ifPresent(ui -> ui.navigate(DatabaseView.class))
        );

        // Card Configuración
        Component configCard = createNavigationCard(
            VaadinIcon.COG, "Configuración",
            securityHelper.isAdmin() ? "Umbrales de alertas" : "Solo lectura",
            securityHelper.isAdmin() ? "#7c3aed" : "#6b7280", 
            securityHelper.canViewAlertConfig(),
            () -> getUI().ifPresent(ui -> ui.navigate(AlertConfigView.class))
        );

        cardsLayout.add(dashboardCard, databaseCard, configCard);

        // Card Usuarios (solo admin)
        if (securityHelper.isAdmin()) {
            Component usersCard = createNavigationCard(
                VaadinIcon.USERS, "Usuarios",
                "Gestión de accesos",
                "#dc2626", true,
                () -> getUI().ifPresent(ui -> ui.navigate(UserManagementView.class))
            );
            cardsLayout.add(usersCard);
        }

        return cardsLayout;
    }

    private Component createNavigationCard(VaadinIcon icon, String title, 
                                         String description, String color, boolean enabled,
                                         Runnable clickAction) {
        // Cambiar de Button a Div clickeable
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border", "2px solid #e1e5e9")
            .set("border-radius", "12px")
            .set("padding", "2rem")
            .set("min-width", "200px")
            .set("height", "150px")
            .set("cursor", enabled ? "pointer" : "not-allowed")
            .set("opacity", enabled ? "1" : "0.6")
            .set("transition", "all 0.3s ease")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "center")
            .set("justify-content", "center");

        if (enabled) {
            card.addClickListener(e -> clickAction.run());
            
            card.getElement().addEventListener("mouseenter", e -> {
                card.getStyle()
                    .set("transform", "translateY(-4px)")
                    .set("box-shadow", "0 8px 25px rgba(0,0,0,0.15)")
                    .set("border-color", color);
            });
            
            card.getElement().addEventListener("mouseleave", e -> {
                card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "none")
                    .set("border-color", "#e1e5e9");
            });
        }

        // Icon
        Icon cardIcon = icon.create();
        cardIcon.setSize("2.5rem");
        cardIcon.getStyle()
            .set("color", color)
            .set("margin-bottom", "1rem");

        // Title
        H2 cardTitle = new H2(title);
        cardTitle.getStyle()
            .set("margin", "0 0 0.5rem 0")
            .set("color", "#2c3e50")
            .set("font-size", "1.2rem")
            .set("font-weight", "600")
            .set("text-align", "center");

        // Description
        Span desc = new Span(description);
        desc.getStyle()
            .set("color", "#6c757d")
            .set("font-size", "0.9rem")
            .set("text-align", "center")
            .set("margin", "0");

        // Agregar todos los elementos al card
        card.add(cardIcon, cardTitle, desc);
        
        return card;
    }

    private Component createSystemStatus() {
        Div statusSection = new Div();
        statusSection.getStyle()
            .set("background", "#f8f9fa")
            .set("border", "1px solid #e1e5e9")
            .set("border-radius", "12px")
            .set("padding", "2rem")
            .set("margin-top", "2rem");

        H2 statusTitle = new H2("🟢 Estado del Sistema - Parcial 2");
        statusTitle.getStyle()
            .set("margin", "0 0 1rem 0")
            .set("color", "#28a745")
            .set("font-size", "1.3rem");

        VerticalLayout statusList = new VerticalLayout();
        statusList.setSpacing(false);
        statusList.setPadding(false);

        String[] statusItems = {
            "✅ Autenticación Auth0 implementada y funcionando",
            "✅ Base de datos PostgreSQL en Neon conectada", 
            "✅ Aplicación desplegada en Render",
            "✅ Control de roles y permisos activo",
            "✅ Todas las funcionalidades del Parcial 2 operativas"
        };

        for (String item : statusItems) {
            Span statusItem = new Span(item);
            statusItem.getStyle()
                .set("display", "block")
                .set("color", "#495057")
                .set("font-size", "1rem")
                .set("margin-bottom", "0.5rem")
                .set("font-weight", "500");
            statusList.add(statusItem);
        }

        statusSection.add(statusTitle, statusList);
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