package com.monitoring.server.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.monitoring.server.security.Auth0SecurityHelper;
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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * MainLayout limpio y profesional para la aplicación Server Monitor
 */
public class MainLayout extends AppLayout {

    @Autowired
    private Auth0SecurityHelper securityHelper;

    public MainLayout(@Autowired Auth0SecurityHelper securityHelper) {
        this.securityHelper = securityHelper;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        // Logo principal
        H1 logo = new H1("🖥️ Server Monitor");
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM
        );
        logo.getStyle().set("color", "#2c3e50");

        // Toggle del menú
        DrawerToggle toggle = new DrawerToggle();

        // Información del usuario y logout
        HorizontalLayout userSection = createUserSection();

        // Layout del header
        HorizontalLayout header = new HorizontalLayout(toggle, logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();

        // Header completo
        HorizontalLayout fullHeader = new HorizontalLayout(header, userSection);
        fullHeader.setWidthFull();
        fullHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        fullHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        fullHeader.setPadding(true);
        fullHeader.getStyle()
            .set("background", "white")
            .set("border-bottom", "1px solid #e1e5e9")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        addToNavbar(fullHeader);
    }

    private HorizontalLayout createUserSection() {
        HorizontalLayout userSection = new HorizontalLayout();
        userSection.setSpacing(true);
        userSection.setAlignItems(FlexComponent.Alignment.CENTER);

        // Información del usuario
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = "Usuario";
        String userRole = "Usuario";
        
        if (auth != null && auth.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) auth.getPrincipal();
            userName = user.getFullName() != null ? user.getFullName() : user.getEmail();
            userRole = securityHelper.getCurrentUserRoleDisplay();
        }

        Div userInfo = new Div();
        userInfo.getStyle().set("text-align", "right");

        Span userNameSpan = new Span(userName);
        userNameSpan.getStyle()
            .set("font-weight", "600")
            .set("color", "#2c3e50")
            .set("display", "block")
            .set("font-size", "0.9rem");

        Span userRoleSpan = new Span("🔑 " + userRole);
        userRoleSpan.getStyle()
            .set("color", "#6c757d")
            .set("font-size", "0.8rem")
            .set("display", "block");

        userInfo.add(userNameSpan, userRoleSpan);

        // Botón de logout
        Button logoutButton = new Button("Cerrar Sesión", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"))
        );

        userSection.add(userInfo, logoutButton);
        return userSection;
    }

    private void createDrawer() {
        // Título del drawer
        Div drawerHeader = new Div();
        drawerHeader.getStyle()
            .set("padding", "1rem")
            .set("border-bottom", "1px solid #e1e5e9")
            .set("background", "#f8f9fa");

        H1 drawerTitle = new H1("Navegación");
        drawerTitle.getStyle()
            .set("margin", "0")
            .set("font-size", "1.1rem")
            .set("color", "#495057");

        drawerHeader.add(drawerTitle);

        // Tabs de navegación
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addClassNames(LumoUtility.Width.FULL);
        tabs.getStyle().set("padding", "1rem 0");

        // Home
        tabs.add(createTab(HomeView.class, VaadinIcon.HOME, "Inicio"));
        
        // Dashboard  
        tabs.add(createTab(DashboardView.class, VaadinIcon.DASHBOARD, "Dashboard"));
        
        // Bases de datos
        Tab databaseTab = createTab(DatabaseView.class, VaadinIcon.DATABASE, "Bases de Datos");
        if (!securityHelper.canViewDatabases()) {
            databaseTab.getStyle().set("opacity", "0.5");
        }
        tabs.add(databaseTab);
    
        // Configuraciones
        Tab configTab = createTab(AlertConfigView.class, VaadinIcon.COG, "Configuración");
        if (!securityHelper.canViewAlertConfig()) {
            configTab.getStyle().set("opacity", "0.5");
        }
        tabs.add(configTab);

        // Gestión de usuarios (solo admin)
        if (securityHelper.isAdmin()) {
            tabs.add(createTab(UserManagementView.class, VaadinIcon.USERS, "Gestión de Usuarios"));
        }

        // Footer del drawer
        Div drawerFooter = new Div();
        drawerFooter.getStyle()
            .set("padding", "1rem")
            .set("border-top", "1px solid #e1e5e9")
            .set("background", "#f8f9fa")
            .set("text-align", "center")
            .set("margin-top", "auto");

        Span footerText = new Span("Server Monitor v2.0");
        footerText.getStyle()
            .set("font-size", "0.8rem")
            .set("color", "#6c757d");

        Span roleIndicator = new Span("Rol: " + securityHelper.getCurrentUserRoleDisplay());
        roleIndicator.getStyle()
            .set("display", "block")
            .set("font-size", "0.75rem")
            .set("color", securityHelper.isAdmin() ? "#dc3545" : "#28a745")
            .set("font-weight", "600")
            .set("margin-top", "0.25rem");

        drawerFooter.add(footerText, roleIndicator);

        addToDrawer(drawerHeader, tabs, drawerFooter);
    }
    
    private Tab createTab(Class<? extends Component> viewClass, VaadinIcon viewIcon, String viewName) {
        RouterLink link = new RouterLink(viewClass);
        link.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("padding", "0.75rem 1rem")
            .set("text-decoration", "none")
            .set("color", "#495057")
            .set("border-radius", "6px")
            .set("margin", "0.25rem")
            .set("transition", "all 0.2s ease");

        // Hover effect
        link.getElement().addEventListener("mouseenter", e -> {
            link.getStyle().set("background", "#e9ecef");
        });
        
        link.getElement().addEventListener("mouseleave", e -> {
            link.getStyle().set("background", "transparent");
        });
        
        Icon icon = viewIcon.create();
        icon.setSize("18px");
        icon.getStyle().set("margin-right", "0.75rem");
        
        Span label = new Span(viewName);
        label.getStyle().set("font-weight", "500");
        
        link.add(icon, label);
        
        Tab tab = new Tab(link);
        tab.getStyle().set("margin", "0");
        
        return tab;
    }
}