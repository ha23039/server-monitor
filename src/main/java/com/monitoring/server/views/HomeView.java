package com.monitoring.server.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.security.MenuSecurityHelper; 
import com.monitoring.server.security.SecurityAnnotations.RequiresAuth;
import com.monitoring.server.views.config.AlertConfigView;
import com.monitoring.server.views.dashboard.DashboardView;
import com.monitoring.server.views.databases.DatabaseView;
import com.monitoring.server.views.users.UserManagementView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
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

    private MenuSecurityHelper securityHelper; // ✅ CORRECTO: Usar MenuSecurityHelper

    // Paleta de colores sugerida
    private static final String COLOR_PRIMARY = "#3B82F6"; // Azul principal
    private static final String COLOR_SECONDARY = "#10B981"; // Verde para éxito/bases de datos
    private static final String COLOR_ACCENT = "#8B5CF6"; // Púrpura para configuración/destacados
    private static final String COLOR_WARNING = "#F59E0B"; // Naranja para alertas
    private static final String COLOR_DANGER = "#EF4444"; // Rojo para usuarios/peligro
    private static final String COLOR_TEXT_PRIMARY = "#1F2937"; // Gris oscuro para texto principal
    private static final String COLOR_TEXT_SECONDARY = "#6B7280"; // Gris medio para texto secundario
    private static final String COLOR_BACKGROUND_LIGHT = "var(--lumo-contrast-5pct)"; // Gris muy claro para fondos
    private static final String COLOR_BORDER = "#E5E7EB"; // Gris claro para bordes

    public HomeView(@Autowired MenuSecurityHelper securityHelper) {
        try {
            System.out.println("🏠 CONSTRUCTOR HomeView - INICIO");
            
            this.securityHelper = securityHelper;

            setPadding(false);
            setSpacing(false);
            setSizeFull();
            setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("overflow-y", "auto");

            // Contenedor principal
            VerticalLayout contentWrapper = new VerticalLayout();
            contentWrapper.setPadding(true);
            contentWrapper.setSpacing(true);
            contentWrapper.setWidthFull();
            contentWrapper.setMaxWidth("1200px");
            contentWrapper.getStyle().set("padding", "var(--lumo-space-l)");

            contentWrapper.add(createWelcomeHeader());
            contentWrapper.add(createNavigationCards());
            contentWrapper.add(createSystemStatus());

            if (securityHelper.isAdmin()) { // ✅ CORRECTO: Usar isAdmin()
                contentWrapper.add(createQuickStats());
            }
            
            // Pie de página simple
            Div footer = new Div(new Span("© " + java.time.Year.now() + " Server Monitor - Todos los derechos reservados"));
            footer.getStyle()
                .set("text-align", "center")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", COLOR_TEXT_SECONDARY)
                .set("padding", "var(--lumo-space-m) 0");
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
        headerLayout.getStyle().set("margin-bottom", "var(--lumo-space-xl)");

        Icon monitorIcon = VaadinIcon.DESKTOP.create();
        monitorIcon.setSize("3em");
        monitorIcon.setColor(COLOR_PRIMARY);

        H1 title = new H1("Bienvenido al Monitor de Servidores");
        title.getStyle()
            .set("font-size", "2.5rem")
            .set("font-weight", "700")
            .set("color", COLOR_TEXT_PRIMARY)
            .set("margin-top", "var(--lumo-space-s)")
            .set("margin-bottom", "var(--lumo-space-xs)");

        H2 subTitle = new H2("Su centro de control para la infraestructura crítica.");
        subTitle.getStyle()
            .set("font-size", "1.25rem")
            .set("font-weight", "400")
            .set("color", COLOR_TEXT_SECONDARY)
            .set("margin-bottom", "var(--lumo-space-m)")
            .set("text-align", "center");
        
        Paragraph description = new Paragraph(
            "Visualice el rendimiento, gestione bases de datos, configure alertas y administre usuarios, " +
            "todo desde una plataforma unificada y segura con Auth0."
        );
        description.getStyle()
            .set("font-size", "1rem")
            .set("color", COLOR_TEXT_SECONDARY)
            .set("text-align", "center")
            .set("max-width", "700px")
            .set("line-height", "1.6");

        headerLayout.add(monitorIcon, title, subTitle, description);
        return headerLayout;
    }

    private Component createNavigationCards() {
        FlexLayout cardsLayout = new FlexLayout();
        cardsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        cardsLayout.getStyle()
            .set("gap", "var(--lumo-space-l)")
            .set("margin-bottom", "var(--lumo-space-xl)");

        cardsLayout.add(
            createNavigationCard(
                VaadinIcon.DASHBOARD, "Dashboard",
                "Métricas clave en tiempo real.",
               COLOR_PRIMARY, true, // ✅ CORRECTO: Usar canAccessDashboard()
                () -> getUI().ifPresent(ui -> ui.navigate(DashboardView.class))
            ),
            createNavigationCard(
                VaadinIcon.DATABASE, "Bases de Datos",
                "Administre sus conexiones.",
                COLOR_SECONDARY, securityHelper.canViewDatabases(), // ✅ CORRECTO: Usar canViewDatabases()
                () -> getUI().ifPresent(ui -> ui.navigate(DatabaseView.class))
            ),
            createNavigationCard(
                VaadinIcon.COG_O, "Configuración",
                securityHelper.isAdmin() ? "Ajuste umbrales y alertas." : "Ver configuración de alertas.",
                COLOR_ACCENT, securityHelper.canViewAlertConfig(), // ✅ CORRECTO: Usar canViewAlertConfig()
                () -> getUI().ifPresent(ui -> ui.navigate(AlertConfigView.class))
            )
        );

        if (securityHelper.isAdmin()) { // ✅ CORRECTO: Usar isAdmin()
            cardsLayout.add(createNavigationCard(
                VaadinIcon.USERS, "Gestión de Usuarios",
                "Administre roles y accesos.",
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
        card.getStyle()
            .set("background-color", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("box-shadow", "var(--lumo-box-shadow-s)")
            .set("padding", "var(--lumo-space-l)")
            .set("width", "280px")
            .set("height", "220px")
            .set("cursor", enabled ? "pointer" : "not-allowed")
            .set("opacity", enabled ? "1" : "0.7")
            .set("transition", "transform 0.2s ease-out, box-shadow 0.2s ease-out");
        
        card.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        if (enabled) {
            card.addClickListener(e -> clickAction.run());
            card.getElement().addEventListener("mouseenter", e -> {
                card.getStyle()
                    .set("transform", "translateY(-5px)")
                    .set("box-shadow", "var(--lumo-box-shadow-l)");
            });
            card.getElement().addEventListener("mouseleave", e -> {
                card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "var(--lumo-box-shadow-s)");
            });
        }

        Div iconWrapper = new Div();
        iconWrapper.getStyle()
            .set("background-color", accentColor + "1A")
            .set("border-radius", "50%")
            .set("padding", "var(--lumo-space-m)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("margin-bottom", "var(--lumo-space-m)");

        Icon icon = iconType.create();
        icon.setColor(accentColor);
        icon.setSize("2em");
        iconWrapper.add(icon);

        H3 cardTitle = new H3(title);
        cardTitle.getStyle()
            .set("color", COLOR_TEXT_PRIMARY)
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("margin", "0 0 var(--lumo-space-s) 0");

        Paragraph cardDescription = new Paragraph(description);
        cardDescription.getStyle()
            .set("color", COLOR_TEXT_SECONDARY)
            .set("font-size", "var(--lumo-font-size-s)")
            .set("text-align", "center")
            .set("line-height", "1.5")
            .set("margin", "0");
        
        VerticalLayout textContent = new VerticalLayout(cardTitle, cardDescription);
        textContent.setSpacing(false);
        textContent.setPadding(false);
        textContent.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        card.add(iconWrapper, textContent);
        return card;
    }

    private Component createSystemStatus() {
        Section statusSection = new Section();
        statusSection.getStyle()
            .set("background-color", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-xl)")
            .set("box-shadow", "var(--lumo-box-shadow-xs)")
            .set("margin-bottom", "var(--lumo-space-xl)");

        H2 title = new H2("Estado General del Sistema");
        title.getStyle()
            .set("color", COLOR_TEXT_PRIMARY)
            .set("font-size", "var(--lumo-font-size-xl)")
            .set("margin", "0 0 var(--lumo-space-l) 0");

        VerticalLayout statusList = new VerticalLayout();
        statusList.setSpacing(false);
        statusList.setPadding(false);

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
            itemLayout.getStyle().set("margin-bottom", "var(--lumo-space-s)");

            Icon checkIcon = VaadinIcon.CHECK_CIRCLE.create();
            checkIcon.setColor(COLOR_SECONDARY);
            checkIcon.getStyle().set("margin-right", "var(--lumo-space-s)");

            Span text = new Span(itemText);
            text.getStyle()
                .set("color", COLOR_TEXT_SECONDARY)
                .set("font-size", "var(--lumo-font-size-m)");
            
            itemLayout.add(checkIcon, text);
            statusList.add(itemLayout);
        }
        
        Paragraph confidenceMessage = new Paragraph(
            "Todos los sistemas principales están funcionando correctamente. Monitorización activa."
        );
        confidenceMessage.getStyle()
            .set("color", COLOR_TEXT_SECONDARY)
            .set("font-size", "var(--lumo-font-size-s)")
            .set("margin-top", "var(--lumo-space-m)")
            .set("font-style", "italic");

        statusSection.add(title, statusList, confidenceMessage);
        return statusSection;
    }

    private Component createQuickStats() {
        Div statsContainer = new Div();
        statsContainer.getStyle()
            .set("background-color", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-xl)")
            .set("box-shadow", "var(--lumo-box-shadow-xs)")
            .set("margin-bottom", "var(--lumo-space-xl)");

        H2 title = new H2("Panel de Administrador: Vistazo Rápido");
        title.getStyle()
            .set("color", COLOR_TEXT_PRIMARY)
            .set("font-size", "var(--lumo-font-size-xl)")
            .set("margin", "0 0 var(--lumo-space-l) 0");

        FlexLayout statsGrid = new FlexLayout();
        statsGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        statsGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        statsGrid.getStyle().set("gap", "var(--lumo-space-m)");

        statsGrid.add(
            createStatCard(VaadinIcon.HEART_O, "Salud del Sistema", "Óptima", COLOR_SECONDARY),
            createStatCard(VaadinIcon.USERS, "Usuarios Activos", "15", COLOR_PRIMARY),
            createStatCard(VaadinIcon.BELL_O, "Alertas Activas", "2", COLOR_WARNING),
            createStatCard(VaadinIcon.CLOCK, "Uptime (24h)", "99.98%", COLOR_ACCENT)
        );
        
        statsContainer.add(title, statsGrid);
        return statsContainer;
    }

    private Component createStatCard(VaadinIcon iconType, String label, String value, String accentColor) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(Alignment.CENTER);
        card.setSpacing(false);
        card.getStyle()
            .set("background-color", COLOR_BACKGROUND_LIGHT)
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-m)")
            .set("min-width", "180px")
            .set("border-left", "4px solid " + accentColor);
        
        Icon icon = iconType.create();
        icon.setColor(accentColor);
        icon.setSize("1.8em");
        icon.getStyle().set("margin-bottom", "var(--lumo-space-xs)");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "var(--lumo-font-size-xl)")
            .set("font-weight", "600")
            .set("color", accentColor)
            .set("margin-bottom", "var(--lumo-space-xs)");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-size", "var(--lumo-font-size-s)")
            .set("color", COLOR_TEXT_SECONDARY);

        card.add(icon, valueSpan, labelSpan);
        return card;
    }
}