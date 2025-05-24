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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    private final Auth0SecurityHelper securityHelper;

    // Reutilizar colores si son globales o usar variables Lumo
    private static final String COLOR_TEXT_PRIMARY_APP = "#1F2937"; // Ejemplo de HomeView
    private static final String COLOR_TEXT_SECONDARY_APP = "#6B7280"; // Ejemplo de HomeView
    private static final String COLOR_PRIMARY_APP = "#3B82F6"; // Ejemplo de HomeView
    private static final String COLOR_DANGER_APP = "#EF4444"; // Ejemplo de HomeView
    private static final String COLOR_SUCCESS_APP = "#10B981"; // Ejemplo de HomeView


    public MainLayout(@Autowired Auth0SecurityHelper securityHelper) {
        this.securityHelper = securityHelper;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("🖥️ Server Monitor");
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.NONE, // Controlar margen con el layout contenedor
            LumoUtility.TextColor.PRIMARY // Usa color primario del tema Lumo
        );
        logo.getStyle().set("line-height", "1"); // Ajuste para alineación vertical

        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Toggle navigation menu");

        HorizontalLayout userSection = createUserSection();

        HorizontalLayout headerLeft = new HorizontalLayout(toggle, logo);
        headerLeft.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerLeft.setSpacing(true); // Espacio entre toggle y logo

        HorizontalLayout header = new HorizontalLayout(headerLeft, userSection);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.getStyle()
            .set("background-color", "var(--lumo-base-color)")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("box-shadow", "var(--lumo-box-shadow-s)");

        addToNavbar(true, header); // true para que el contenido del navbar sea flexible
    }

    private HorizontalLayout createUserSection() {
        HorizontalLayout userSectionLayout = new HorizontalLayout();
        userSectionLayout.setSpacing(true);
        userSectionLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = "Usuario";
        String userEmail = "No autenticado";
        String userRole = "Invitado";

        if (auth != null && auth.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) auth.getPrincipal();
            userName = oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getGivenName();
            if (userName == null) userName = oidcUser.getEmail(); // Fallback
            userEmail = oidcUser.getEmail();
            userRole = securityHelper.getCurrentUserRoleDisplay();
        }

        VerticalLayout userInfoText = new VerticalLayout();
        userInfoText.setSpacing(false);
        userInfoText.setPadding(false);
        userInfoText.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.END); // Alinear texto a la derecha

        Span userNameSpan = new Span(userName);
        userNameSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.TextColor.BODY);

        Span userRoleSpan = new Span(userRole);
        userRoleSpan.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);
        // Podrías añadir un icono al rol si quieres
        // Icon roleIcon = VaadinIcon.KEY_A.create();
        // roleIcon.setSize("0.75em");
        // userRoleSpan.addComponentAsFirst(roleIcon);
        // userRoleSpan.getStyle().set("gap", "var(--lumo-space-xs)");

        userInfoText.add(userNameSpan, userRoleSpan);

        Button logoutButton = new Button("Salir", VaadinIcon.SIGN_OUT_ALT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR); // Error para destacar más la salida
        logoutButton.getStyle().set("font-size", "var(--lumo-font-size-s)");
        logoutButton.addClickListener(e ->
            getUI().ifPresent(ui -> ui.getPage().setLocation(securityHelper.getLogoutUrl())) // Usar helper para URL de logout
        );

        userSectionLayout.add(userInfoText, logoutButton);
        return userSectionLayout;
    }

    private void createDrawer() {
        Div drawerHeader = new Div();
        drawerHeader.addClassNames(
            LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER,
            LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Vertical.SMALL,
            LumoUtility.Background.CONTRAST_5 // Fondo sutil
        );
        drawerHeader.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        Icon appIcon = VaadinIcon.CONNECT_O.create(); // Un icono diferente para el drawer
        appIcon.setColor(COLOR_PRIMARY_APP);
        appIcon.getStyle().set("margin-right", "var(--lumo-space-s)");

        H1 drawerTitle = new H1("Navegación");
        drawerTitle.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.TextColor.BODY, LumoUtility.Margin.NONE);
        drawerHeader.add(appIcon, drawerTitle);


        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.setWidthFull();
        // tabs.getStyle().set("padding", "var(--lumo-space-s) 0"); // Un poco menos de padding vertical

        tabs.add(
            createTab(HomeView.class, VaadinIcon.HOME_O, "Inicio"),
            createTab(DashboardView.class, VaadinIcon.CHART_TIMELINE, "Dashboard")
        );

        Tab databaseTab = createTab(DatabaseView.class, VaadinIcon.DATABASE, "Bases de Datos");
        if (!securityHelper.canViewDatabases()) {
            databaseTab.setEnabled(false); // Mejor deshabilitar que solo opacidad
        }
        tabs.add(databaseTab);

        Tab configTab = createTab(AlertConfigView.class, VaadinIcon.COG_O, "Configuración");
        if (!securityHelper.canViewAlertConfig()) {
            configTab.setEnabled(false);
        }
        tabs.add(configTab);

        if (securityHelper.isAdmin()) {
            tabs.add(createTab(UserManagementView.class, VaadinIcon.USERS, "Gestión Usuarios"));
        }

        Div drawerFooter = new Div();
        drawerFooter.addClassNames(
            LumoUtility.Padding.MEDIUM, LumoUtility.Background.CONTRAST_5,
            LumoUtility.TextAlignment.CENTER
        );
        drawerFooter.getStyle()
            .set("border-top", "1px solid var(--lumo-contrast-10pct)")
            .set("margin-top", "auto"); // Empuja al final

        Span footerText = new Span("Server Monitor v2.0");
        footerText.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        Span roleIndicator = new Span("Rol: " + securityHelper.getCurrentUserRoleDisplay());
        roleIndicator.addClassNames(LumoUtility.Display.BLOCK, LumoUtility.FontSize.XSMALL, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.Margin.Top.XSMALL);
        roleIndicator.getStyle().set("color", securityHelper.isAdmin() ? COLOR_DANGER_APP : COLOR_SUCCESS_APP);

        drawerFooter.add(footerText, roleIndicator);

        addToDrawer(drawerHeader, tabs, drawerFooter);
        setDrawerOpened(false); // Iniciar cerrado por defecto en pantallas pequeñas
    }

    private Tab createTab(Class<? extends Component> viewClass, VaadinIcon viewIcon, String viewName) {
        RouterLink link = new RouterLink();
        link.setRoute(viewClass); // Establecer ruta así es más robusto
        link.addClassNames(
            LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER,
            LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Vertical.SMALL,
            LumoUtility.TextColor.SECONDARY // Color por defecto para links no activos
        );
        link.getStyle()
            .set("text-decoration", "none")
            .set("border-radius", "var(--lumo-border-radius-s)")
            .set("margin", "var(--lumo-space-xs) var(--lumo-space-s)") // Margen horizontal
            .set("transition", "background-color 0.2s, color 0.2s");

        // Efecto hover usando pseudo-clases de Lumo si es posible o JS
        // Para Lumo, se puede añadir un atributo de tema y estilizarlo en CSS
        // O mantener el JS si es simple:
        link.getElement().addEventListener("mouseenter", e -> {
            link.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
            link.getStyle().set("color", "var(--lumo-primary-text-color)");
        });
        link.getElement().addEventListener("mouseleave", e -> {
            link.getStyle().set("background-color", "transparent");
            link.getStyle().set("color", "var(--lumo-secondary-text-color)");
        });


        Icon icon = viewIcon.create();
        icon.addClassNames(LumoUtility.IconSize.SMALL); // Usar tamaños de icono de Lumo
        icon.getStyle().set("margin-right", "var(--lumo-space-m)");

        Span label = new Span(viewName);
        label.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);

        link.add(icon, label);

        Tab tab = new Tab(link);
        // Lumo se encarga del estilo de Tab activo, no necesitamos mucho más
        // tab.getStyle().set("padding", "0"); // Remover padding del Tab si el link ya lo tiene
        return tab;
    }
}