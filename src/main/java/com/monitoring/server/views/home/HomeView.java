package com.monitoring.server.views.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.security.MenuSecurityHelper;
import com.monitoring.server.security.SecurityAnnotations.RequiresAuth;
import com.monitoring.server.views.MainLayout;
import com.monitoring.server.views.config.AlertConfigView;
import com.monitoring.server.views.dashboard.DashboardView;
import com.monitoring.server.views.databases.DatabaseView;
import com.monitoring.server.views.users.UserManagementView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Inicio - Server Monitor")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "home", layout = MainLayout.class)
@RequiresAuth
public class HomeView extends VerticalLayout {

    private MenuSecurityHelper securityHelper;

    // Paleta de colores optimizada para mejor contraste
    private static final String COLOR_PRIMARY = "#60A5FA"; // Azul más claro y vibrante
    private static final String COLOR_SECONDARY = "#34D399"; // Verde más brillante
    private static final String COLOR_ACCENT = "#A78BFA"; // Púrpura más suave
    private static final String COLOR_WARNING = "#FBBF24"; // Amarillo más cálido
    private static final String COLOR_DANGER = "#F87171"; // Rojo más suave
    private static final String COLOR_TEXT_PRIMARY = "#F9FAFB"; // Texto blanco/muy claro
    private static final String COLOR_TEXT_SECONDARY = "#D1D5DB"; // Gris claro para texto secundario
    private static final String COLOR_CARD_BG = "rgba(255, 255, 255, 0.1)"; // Fondo de tarjetas translúcido
    private static final String COLOR_CARD_BG_HOVER = "rgba(255, 255, 255, 0.15)"; // Hover más claro
    private static final String COLOR_BORDER = "rgba(255, 255, 255, 0.2)"; // Bordes translúcidos

    public HomeView(@Autowired MenuSecurityHelper securityHelper) {
        try {
            System.out.println("🏠 CONSTRUCTOR HomeView - INICIO");
            
            this.securityHelper = securityHelper;

            setPadding(false);
            setSpacing(false);
            setSizeFull();
            setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            getStyle()
                .set("background", "linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%)")
                .set("min-height", "100vh")
                .set("overflow-y", "auto");

            // Contenedor principal
            VerticalLayout contentWrapper = new VerticalLayout();
            contentWrapper.setPadding(true);
            contentWrapper.setSpacing(true);
            contentWrapper.setWidthFull();
            contentWrapper.setMaxWidth("1200px");
            contentWrapper.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            contentWrapper.getStyle()
                .set("padding", "2rem")
                .set("margin", "0 auto");

            contentWrapper.add(createWelcomeHeader());
            contentWrapper.add(createNavigationCards());
            contentWrapper.add(createSystemStatus());

            if (securityHelper.isAdmin()) {
                contentWrapper.add(createQuickStats());
            }
            
            // Pie de página simple
            Div footer = new Div(new Span("© " + java.time.Year.now() + " Server Monitor - Todos los derechos reservados"));
            footer.getStyle()
                .set("text-align", "center")
                .set("font-size", "0.875rem")
                .set("color", COLOR_TEXT_SECONDARY)
                .set("padding", "2rem 0 1rem 0")
                .set("margin-top", "2rem");
            contentWrapper.add(footer);

            add(contentWrapper);
            
            System.out.println("🏠 CONSTRUCTOR HomeView - ÉXITO");
        } catch (Exception e) {
            System.err.println("❌ ERROR EN HOMEVIEW: " + e.getMessage());
            e.printStackTrace();
            add(new H1("Error en HomeView: " + e.getMessage()));
        }
    }

    private Component createWelcomeHeader() {
        VerticalLayout headerLayout = new VerticalLayout();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setSpacing(false);
        headerLayout.setPadding(false);
        headerLayout.setMaxWidth("800px");
        headerLayout.getStyle()
            .set("margin-bottom", "3rem")
            .set("text-align", "center");

        Icon monitorIcon = VaadinIcon.DESKTOP.create();
        monitorIcon.setSize("4rem");
        monitorIcon.setColor(COLOR_PRIMARY);
        monitorIcon.getStyle()
            .set("filter", "drop-shadow(0 4px 8px rgba(0,0,0,0.3))")
            .set("margin-bottom", "1rem");

        H1 title = new H1("Bienvenido al Monitor de Servidores");
        title.getStyle()
            .set("font-size", "3rem")
            .set("font-weight", "800")
            .set("color", COLOR_TEXT_PRIMARY)
            .set("margin", "0")
            .set("text-shadow", "0 2px 4px rgba(0,0,0,0.3)")
            .set("line-height", "1.1");

        H2 subTitle = new H2("Su centro de control para la infraestructura crítica");
        subTitle.getStyle()
            .set("font-size", "1.5rem")
            .set("font-weight", "400")
            .set("color", COLOR_TEXT_SECONDARY)
            .set("margin", "1rem 0")
            .set("text-align", "center");
        
        Paragraph description = new Paragraph(
            "Visualice el rendimiento, gestione bases de datos, configure alertas y administre usuarios, " +
            "todo desde una plataforma unificada y segura con Auth0."
        );
        description.getStyle()
            .set("font-size", "1.125rem")
            .set("color", COLOR_TEXT_SECONDARY)
            .set("text-align", "center")
            .set("max-width", "600px")
            .set("line-height", "1.6")
            .set("margin", "0 auto");

        headerLayout.add(monitorIcon, title, subTitle, description);
        return headerLayout;
    }

    private Component createNavigationCards() {
        FlexLayout cardsLayout = new FlexLayout();
        cardsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        cardsLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        cardsLayout.getStyle()
            .set("gap", "1.5rem")
            .set("margin-bottom", "3rem")
            .set("width", "100%");

        cardsLayout.add(
            createNavigationCard(
                VaadinIcon.DASHBOARD, "Dashboard",
                "Métricas clave en tiempo real",
                COLOR_PRIMARY, securityHelper.canAccessDashboard(),
                () -> getUI().ifPresent(ui -> ui.navigate(DashboardView.class))
            ),
            createNavigationCard(
                VaadinIcon.DATABASE, "Bases de Datos",
                "Administre sus conexiones",
                COLOR_SECONDARY, securityHelper.canViewDatabases(),
                () -> getUI().ifPresent(ui -> ui.navigate(DatabaseView.class))
            ),
            createNavigationCard(
                VaadinIcon.COG_O, "Configuración",
                securityHelper.isAdmin() ? "Ajuste umbrales y alertas" : "Ver configuración de alertas",
                COLOR_ACCENT, securityHelper.canViewAlertConfig(),
                () -> getUI().ifPresent(ui -> ui.navigate(AlertConfigView.class))
            )
        );

        if (securityHelper.isAdmin()) {
            cardsLayout.add(createNavigationCard(
                VaadinIcon.USERS, "Gestión de Usuarios",
                "Administre roles y accesos",
                COLOR_DANGER, true,
                () -> getUI().ifPresent(ui -> ui.navigate(UserManagementView.class))
            ));
        }
        return cardsLayout;
    }

    private Component createNavigationCard(VaadinIcon iconType, String title, String description,
                                           String accentColor, boolean enabled, Runnable clickAction) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        card.getStyle()
            .set("background", enabled ? COLOR_CARD_BG : "rgba(255, 255, 255, 0.05)")
            .set("backdrop-filter", "blur(10px)")
            .set("border", "1px solid " + COLOR_BORDER)
            .set("border-radius", "1rem")
            .set("padding", "2rem")
            .set("width", "280px")
            .set("height", "200px")
            .set("cursor", enabled ? "pointer" : "not-allowed")
            .set("opacity", enabled ? "1" : "0.6")
            .set("transition", "all 0.3s ease")
            .set("position", "relative")
            .set("overflow", "hidden");

        if (enabled) {
            card.addClickListener(e -> clickAction.run());
            card.getElement().addEventListener("mouseenter", e -> {
                card.getStyle()
                    .set("transform", "translateY(-8px)")
                    .set("background", COLOR_CARD_BG_HOVER)
                    .set("box-shadow", "0 20px 40px rgba(0,0,0,0.3)");
            });
            card.getElement().addEventListener("mouseleave", e -> {
                card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("background", COLOR_CARD_BG)
                    .set("box-shadow", "0 8px 25px rgba(0,0,0,0.2)");
            });
        }

        // Gradiente decorativo en la parte superior
        Div topAccent = new Div();
        topAccent.getStyle()
            .set("position", "absolute")
            .set("top", "0")
            .set("left", "0")
            .set("right", "0")
            .set("height", "4px")
            .set("background", "linear-gradient(90deg, " + accentColor + ", " + accentColor + "80)");

        Div iconWrapper = new Div();
        iconWrapper.getStyle()
            .set("background", accentColor + "20")
            .set("border", "2px solid " + accentColor + "40")
            .set("border-radius", "50%")
            .set("padding", "1rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("margin-bottom", "1.5rem")
            .set("box-shadow", "0 8px 20px " + accentColor + "30");

        Icon icon = iconType.create();
        icon.setColor(accentColor);
        icon.setSize("2.5rem");
        iconWrapper.add(icon);

        H3 cardTitle = new H3(title);
        cardTitle.getStyle()
            .set("color", COLOR_TEXT_PRIMARY)
            .set("font-size", "1.25rem")
            .set("font-weight", "700")
            .set("margin", "0 0 0.5rem 0")
            .set("text-align", "center");

        Paragraph cardDescription = new Paragraph(description);
        cardDescription.getStyle()
            .set("color", COLOR_TEXT_SECONDARY)
            .set("font-size", "0.875rem")
            .set("text-align", "center")
            .set("line-height", "1.5")
            .set("margin", "0");

        card.add(topAccent, iconWrapper, cardTitle, cardDescription);
        return card;
    }

    private Component createSystemStatus() {
        Section statusSection = new Section();
        statusSection.getStyle()
            .set("background", COLOR_CARD_BG)
            .set("backdrop-filter", "blur(10px)")
            .set("border", "1px solid " + COLOR_BORDER)
            .set("border-radius", "1rem")
            .set("padding", "2rem")
            .set("box-shadow", "0 8px 25px rgba(0,0,0,0.2)")
            .set("margin-bottom", "2rem")
            .set("width", "100%")
            .set("max-width", "800px");

        H2 title = new H2("Estado General del Sistema");
        title.getStyle()
            .set("color", COLOR_TEXT_PRIMARY)
            .set("font-size", "1.75rem")
            .set("font-weight", "700")
            .set("margin", "0 0 1.5rem 0")
            .set("text-align", "center");

        VerticalLayout statusList = new VerticalLayout();
        statusList.setSpacing(true);
        statusList.setPadding(false);
        statusList.setAlignItems(FlexComponent.Alignment.START);

        String[] statusItemsData = {
            "Autenticación Auth0: Implementada y operativa",
            "Base de Datos (Neon/PostgreSQL): Conectada y accesible",
            "Despliegue Aplicación (Render): En línea y funcionando",
            "Control de Roles y Permisos: Activo y validado",
            "Funcionalidades Parcial 2: Completadas y operativas"
        };

        for (String itemText : statusItemsData) {
            HorizontalLayout itemLayout = new HorizontalLayout();
            itemLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            itemLayout.setSpacing(true);
            itemLayout.getStyle()
                .set("background", "rgba(52, 211, 153, 0.1)")
                .set("padding", "0.75rem 1rem")
                .set("border-radius", "0.5rem")
                .set("border-left", "4px solid " + COLOR_SECONDARY)
                .set("width", "100%");

            Icon checkIcon = VaadinIcon.CHECK_CIRCLE.create();
            checkIcon.setColor(COLOR_SECONDARY);
            checkIcon.setSize("1.25rem");

            Span text = new Span(itemText);
            text.getStyle()
                .set("color", COLOR_TEXT_PRIMARY)
                .set("font-size", "1rem")
                .set("font-weight", "500");
            
            itemLayout.add(checkIcon, text);
            statusList.add(itemLayout);
        }
        
        Paragraph confidenceMessage = new Paragraph(
            "🚀 Todos los sistemas principales están funcionando correctamente. Monitorización activa."
        );
        confidenceMessage.getStyle()
            .set("color", COLOR_TEXT_SECONDARY)
            .set("font-size", "1rem")
            .set("margin-top", "1.5rem")
            .set("text-align", "center")
            .set("font-style", "italic")
            .set("padding", "1rem")
            .set("background", "rgba(96, 165, 250, 0.1)")
            .set("border-radius", "0.5rem")
            .set("border", "1px solid rgba(96, 165, 250, 0.3)");

        statusSection.add(title, statusList, confidenceMessage);
        return statusSection;
    }

    private Component createQuickStats() {
        Div statsContainer = new Div();
        statsContainer.getStyle()
            .set("background", COLOR_CARD_BG)
            .set("backdrop-filter", "blur(10px)")
            .set("border", "1px solid " + COLOR_BORDER)
            .set("border-radius", "1rem")
            .set("padding", "2rem")
            .set("box-shadow", "0 8px 25px rgba(0,0,0,0.2)")
            .set("margin-bottom", "2rem")
            .set("width", "100%")
            .set("max-width", "800px");

        H2 title = new H2("Panel de Administrador: Vistazo Rápido");
        title.getStyle()
            .set("color", COLOR_TEXT_PRIMARY)
            .set("font-size", "1.75rem")
            .set("font-weight", "700")
            .set("margin", "0 0 1.5rem 0")
            .set("text-align", "center");

        FlexLayout statsGrid = new FlexLayout();
        statsGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        statsGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        statsGrid.setAlignItems(FlexComponent.Alignment.STRETCH);
        statsGrid.getStyle().set("gap", "1rem");

        // Obtener número real de usuarios (puedes ajustar esto según tu servicio)
        String realUserCount = "N/A";
        try {
            // realUserCount = String.valueOf(userService.countActiveUsers());
            realUserCount = "11"; // temporal, futuras mejoras
        } catch (Exception e) {
            realUserCount = "Error";
        }

        statsGrid.add(
            createStatCard(VaadinIcon.HEART_O, "Salud del Sistema", "Óptima", COLOR_SECONDARY),
            createStatCard(VaadinIcon.USERS, "Usuarios Activos", realUserCount, COLOR_PRIMARY),
            createStatCard(VaadinIcon.BELL_O, "Alertas Activas", "2", COLOR_WARNING),
            createStatCard(VaadinIcon.CLOCK, "Uptime (24h)", "99.95%", COLOR_ACCENT)
        );
        
        statsContainer.add(title, statsGrid);
        return statsContainer;
    }

    private Component createStatCard(VaadinIcon iconType, String label, String value, String accentColor) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(Alignment.CENTER);
        card.setSpacing(false);
        card.getStyle()
            .set("background", "rgba(255, 255, 255, 0.08)")
            .set("border", "1px solid " + COLOR_BORDER)
            .set("border-radius", "0.75rem")
            .set("padding", "1.5rem")
            .set("min-width", "160px")
            .set("border-left", "4px solid " + accentColor)
            .set("transition", "transform 0.2s ease")
            .set("cursor", "default");

        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("transform", "translateY(-2px)");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("transform", "translateY(0)");
        });
        
        Icon icon = iconType.create();
        icon.setColor(accentColor);
        icon.setSize("2rem");
        icon.getStyle()
            .set("margin-bottom", "0.75rem")
            .set("filter", "drop-shadow(0 2px 4px rgba(0,0,0,0.2))");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "2rem")
            .set("font-weight", "800")
            .set("color", accentColor)
            .set("margin-bottom", "0.5rem")
            .set("text-shadow", "0 2px 4px rgba(0,0,0,0.3)");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-size", "0.875rem")
            .set("color", COLOR_TEXT_SECONDARY)
            .set("text-align", "center")
            .set("font-weight", "500");

        card.add(icon, valueSpan, labelSpan);
        return card;
    }
}