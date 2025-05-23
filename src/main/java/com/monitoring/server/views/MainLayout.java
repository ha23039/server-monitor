package com.monitoring.server.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.security.MenuSecurityHelper;
import com.monitoring.server.views.config.AlertConfigView;
import com.monitoring.server.views.dashboard.DashboardView;
import com.monitoring.server.views.databases.DatabaseView;
import com.monitoring.server.views.home.HomeView;
import com.monitoring.server.views.users.UserManagementView;
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
 * Diseño principal de la aplicación con seguridad basada en roles.
 * Contiene la barra superior y el menú lateral.
 */
public class MainLayout extends AppLayout {

    @Autowired
    private MenuSecurityHelper securityHelper;

    public MainLayout(@Autowired MenuSecurityHelper securityHelper) {
        this.securityHelper = securityHelper;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Server Monitor");
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM
        );

        DrawerToggle toggle = new DrawerToggle();

        // User info and logout section
        HorizontalLayout userSection = createUserSection();

        HorizontalLayout header = new HorizontalLayout(toggle, logo);
        header.setFlexGrow(1, logo);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");

        addToNavbar(header, userSection);
    }

    private HorizontalLayout createUserSection() {
        HorizontalLayout userSection = new HorizontalLayout();
        userSection.setAlignItems(FlexComponent.Alignment.CENTER);
        userSection.setSpacing(true);

        if (securityHelper.isAuthenticated()) {
            // User info
            VerticalLayout userInfo = new VerticalLayout();
            userInfo.setSpacing(false);
            userInfo.setPadding(false);

            Span userName = new Span(securityHelper.getCurrentUserName());
            userName.getStyle().set("font-weight", "bold");

            Span userRole = new Span(securityHelper.getCurrentUserRoleDisplay());
            userRole.getStyle().set("font-size", "0.8rem");
            userRole.getStyle().set("color", "var(--lumo-secondary-text-color)");

            userInfo.add(userName, userRole);

            // Logout button
            Button logoutButton = new Button("Cerrar Sesión", VaadinIcon.SIGN_OUT.create());
            logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            logoutButton.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
            });

            userSection.add(userInfo, logoutButton);
        }

        return userSection;
    }

    private void createDrawer() {
        VerticalLayout drawerContent = new VerticalLayout();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);

        // Navigation tabs
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addClassNames(LumoUtility.Width.FULL);

        // Add tabs based on user permissions
        if (securityHelper.canAccessDashboard()) {
            tabs.add(createTab(HomeView.class, VaadinIcon.HOME, "Home"));
            tabs.add(createTab(DashboardView.class, VaadinIcon.DASHBOARD, "Dashboard"));
        }

        // Database management - only for sysadmin
        if (securityHelper.canManageDatabases()) {
            tabs.add(createTab(DatabaseView.class, VaadinIcon.DATABASE, "Bases de Datos"));
        } else if (securityHelper.canViewDatabases()) {
            // Read-only access for operators
            tabs.add(createTab(DatabaseView.class, VaadinIcon.DATABASE, "Bases de Datos (Solo Lectura)"));
        }

        // Alert configuration - sysadmin can edit, operators can view
        if (securityHelper.canConfigureAlerts()) {
            tabs.add(createTab(AlertConfigView.class, VaadinIcon.COGS, "Configuración"));
        } else if (securityHelper.canViewAlertConfig()) {
            tabs.add(createTab(AlertConfigView.class, VaadinIcon.COGS, "Configuración (Solo Lectura)"));
        }

        // User management - only for sysadmin
        if (securityHelper.canManageUsers()) {
            tabs.add(createTab(UserManagementView.class, VaadinIcon.USERS, "Gestión de Usuarios"));
        }

        drawerContent.add(tabs);

        // Add role indicator at bottom
        Div roleIndicator = createRoleIndicator();
        drawerContent.add(roleIndicator);

        addToDrawer(drawerContent);
    }

    private Tab createTab(Class<? extends Component> viewClass, VaadinIcon viewIcon, String viewName) {
        RouterLink link = new RouterLink(viewClass);
        
        Icon icon = viewIcon.create();
        icon.setSize("18px");
        
        link.add(icon);
        link.add(new Span(viewName));
        
        return new Tab(link);
    }

    private Div createRoleIndicator() {
        Div roleIndicator = new Div();
        roleIndicator.addClassName("role-indicator");
        roleIndicator.getStyle()
            .set("padding", "1rem")
            .set("border-top", "1px solid var(--lumo-contrast-20pct)")
            .set("margin-top", "auto");

        if (securityHelper.isAuthenticated()) {
            Span roleLabel = new Span("Rol Actual:");
            roleLabel.getStyle().set("font-size", "0.8rem");
            roleLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");

            Span roleName = new Span(securityHelper.getCurrentUserRoleDisplay());
            roleName.getStyle()
                .set("font-weight", "bold")
                .set("color", getRoleColor(securityHelper.getCurrentUserRoleDisplay()));

            VerticalLayout roleInfo = new VerticalLayout(roleLabel, roleName);
            roleInfo.setSpacing(false);
            roleInfo.setPadding(false);

            roleIndicator.add(roleInfo);
        }

        return roleIndicator;
    }

    private String getRoleColor(String role) {
        switch (role) {
            case "Administrador del Sistema":
                return "var(--lumo-error-color)";
            case "Operador":
                return "var(--lumo-warning-color)";
            case "Visualizador":
                return "var(--lumo-success-color)";
            default:
                return "var(--lumo-secondary-text-color)";
        }
    }
}