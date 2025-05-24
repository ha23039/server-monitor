package com.monitoring.server.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.security.Auth0SecurityHelper;
import com.monitoring.server.views.configurations.ConfigurationsView;
import com.monitoring.server.views.dashboard.DashboardView;
import com.monitoring.server.views.home.HomeView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Layout principal con integración Auth0
 */
public class MainLayout extends AppLayout {

    @Autowired
    private Auth0SecurityHelper auth0SecurityHelper;

    public MainLayout(@Autowired Auth0SecurityHelper auth0SecurityHelper) {
        this.auth0SecurityHelper = auth0SecurityHelper;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("🖥️ Server Monitor");
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM
        );
        logo.getStyle().set("color", "#1976d2");

        DrawerToggle toggle = new DrawerToggle();

        // Sección de usuario
        HorizontalLayout userSection = createUserSection();

        HorizontalLayout header = new HorizontalLayout(toggle, logo);
        header.setFlexGrow(1, logo);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.12)");

        addToNavbar(header, userSection);
    }

    private HorizontalLayout createUserSection() {
        HorizontalLayout userSection = new HorizontalLayout();
        userSection.setAlignItems(FlexComponent.Alignment.CENTER);
        userSection.setSpacing(true);
        userSection.getStyle().set("margin-right", "1rem");

        if (auth0SecurityHelper.isAuthenticated()) {
            // Info del usuario
            VerticalLayout userInfo = new VerticalLayout();
            userInfo.setSpacing(false);
            userInfo.setPadding(false);
            userInfo.getStyle().set("margin-right", "1rem");

            Span userName = new Span(auth0SecurityHelper.getCurrentUserName());
            userName.getStyle()
                .set("font-weight", "bold")
                .set("color", "#1976d2");

            Span userRole = new Span("🔑 " + auth0SecurityHelper.getCurrentUserRoleDisplay());
            userRole.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "var(--lumo-secondary-text-color)");

            userInfo.add(userName, userRole);

            // Botón de logout
            Button logoutButton = new Button("Cerrar Sesión", VaadinIcon.SIGN_OUT.create());
            logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            logoutButton.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
            });

            userSection.add(userInfo, logoutButton);
        } else {
            Button loginButton = new Button("Iniciar Sesión", VaadinIcon.SIGN_IN.create());
            loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            loginButton.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.navigate("login"));
            });
            userSection.add(loginButton);
        }

        return userSection;
    }

    private void createDrawer() {
        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.getStyle()
            .set("background", "linear-gradient(180deg, #f8f9fa 0%, #e9ecef 100%)")
            .set("height", "100%");

        // Header del drawer
        Div drawerHeader = new Div();
        drawerHeader.getStyle()
            .set("padding", "1rem")
            .set("background", "#1976d2")
            .set("color", "white")
            .set("text-align", "center");
        
        Span headerText = new Span("📊 Panel de Control");
        headerText.getStyle().set("font-weight", "bold");
        drawerHeader.add(headerText);

        // Tabs de navegación
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addClassNames(LumoUtility.Width.FULL);
        tabs.getStyle().set("background", "transparent");

        // Agregar tabs según permisos
        if (auth0SecurityHelper.canAccessDashboard()) {
            tabs.add(createTab(HomeView.class, VaadinIcon.HOME, "🏠 Home"));
            tabs.add(createTab(DashboardView.class, VaadinIcon.DASHBOARD, "📊 Dashboard"));
        }

        // Configuración - admin puede editar, usuarios solo ver
        if (auth0SecurityHelper.canConfigureAlerts()) {
            tabs.add(createTab(ConfigurationsView.class, VaadinIcon.COG, "⚙️ Configuración"));
        } else if (auth0SecurityHelper.canViewAlertConfig()) {
            tabs.add(createTab(ConfigurationsView.class, VaadinIcon.COG, "👁️ Configuración (Ver)"));
        }

        drawerContent.add(drawerHeader, tabs);

        // Indicador de rol en la parte inferior
        if (auth0SecurityHelper.isAuthenticated()) {
            Div roleIndicator = createRoleIndicator();
            drawerContent.add(roleIndicator);
        }

        addToDrawer(drawerContent);
    }

    private Tab createTab(Class<? extends Component> viewClass, VaadinIcon viewIcon, String viewName) {
        RouterLink link = new RouterLink(viewClass);
        
        Icon icon = viewIcon.create();
        icon.setSize("18px");
        
        link.add(icon);
        link.add(new Span(viewName));
        link.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.5rem")
            .set("padding", "0.75rem 1rem")
            .set("text-decoration", "none")
            .set("color", "#495057")
            .set("border-radius", "8px")
            .set("margin", "0.25rem");
        
        // Hover effect
        link.getElement().addEventListener("mouseenter", e -> {
            link.getStyle().set("background", "#e3f2fd");
        });
        
        link.getElement().addEventListener("mouseleave", e -> {
            link.getStyle().set("background", "transparent");
        });
        
        Tab tab = new Tab(link);
        tab.getStyle().set("background", "transparent");
        return tab;
    }

    private Div createRoleIndicator() {
        Div roleIndicator = new Div();
        roleIndicator.getStyle()
            .set("padding", "1rem")
            .set("margin-top", "auto")
            .set("border-top", "1px solid #dee2e6")
            .set("background", "rgba(255,255,255,0.8)");

        VerticalLayout roleInfo = new VerticalLayout();
        roleInfo.setSpacing(false);
        roleInfo.setPadding(false);

        Span roleLabel = new Span("👤 Sesión Activa:");
        roleLabel.getStyle()
            .set("font-size", "0.8rem")
            .set("color", "#6c757d")
            .set("font-weight", "500");

        Span userName = new Span(auth0SecurityHelper.getCurrentUserName());
        userName.getStyle()
            .set("font-weight", "bold")
            .set("color", "#1976d2");

        Span roleName = new Span(auth0SecurityHelper.getCurrentUserRoleDisplay());
        roleName.getStyle()
            .set("font-size", "0.9rem")
            .set("color", getRoleColor(auth0SecurityHelper.getCurrentUserRoleDisplay()))
            .set("font-weight", "600");

        roleInfo.add(roleLabel, userName, roleName);
        roleIndicator.add(roleInfo);

        return roleIndicator;
    }

    private String getRoleColor(String role) {
        switch (role) {
            case "Administrador":
                return "#dc3545"; // Rojo para admin
            case "Usuario":
                return "#28a745"; // Verde para usuario
            default:
                return "#6c757d"; // Gris por defecto
        }
    }
}