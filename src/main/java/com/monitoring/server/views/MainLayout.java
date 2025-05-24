package com.monitoring.server.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.security.Auth0SecurityHelper;
import com.monitoring.server.views.config.AlertConfigView;
import com.monitoring.server.views.dashboard.DashboardView;
import com.monitoring.server.views.databases.DatabaseView;
import com.monitoring.server.views.home.HomeView;
import com.monitoring.server.views.users.UserManagementView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * MainLayout moderno para la aplicación Server Monitor
 */
public class MainLayout extends AppLayout {

    @Autowired
    private Auth0SecurityHelper securityHelper;

    private H1 viewTitle;

    public MainLayout(@Autowired Auth0SecurityHelper securityHelper) {
        this.securityHelper = securityHelper;
        
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        toggle.getStyle()
            .set("color", "white");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        viewTitle.getStyle()
            .set("color", "white")
            .set("font-weight", "600");

        // User info and logout
        HorizontalLayout userSection = createUserSection();

        HorizontalLayout header = new HorizontalLayout(toggle, viewTitle);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setFlexGrow(1, viewTitle);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        // Header container with gradient
        Div headerContainer = new Div();
        headerContainer.getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("padding", "0.75rem 1rem")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        HorizontalLayout fullHeader = new HorizontalLayout(header, userSection);
        fullHeader.setWidthFull();
        fullHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        fullHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        headerContainer.add(fullHeader);
        addToNavbar(headerContainer);
    }

    private HorizontalLayout createUserSection() {
        HorizontalLayout userSection = new HorizontalLayout();
        userSection.setSpacing(true);
        userSection.setAlignItems(FlexComponent.Alignment.CENTER);

        // User avatar and info
        Avatar avatar = new Avatar(securityHelper.getCurrentUserName());
        avatar.getStyle()
            .set("background", "rgba(255,255,255,0.2)")
            .set("color", "white");

        VerticalLayout userInfo = new VerticalLayout();
        userInfo.setSpacing(false);
        userInfo.setPadding(false);

        Span userName = new Span(securityHelper.getCurrentUserName());
        userName.getStyle()
            .set("color", "white")
            .set("font-weight", "600")
            .set("font-size", "0.9rem");

        Span userRole = new Span(securityHelper.getCurrentUserRoleDisplay());
        userRole.getStyle()
            .set("color", "rgba(255,255,255,0.8)")
            .set("font-size", "0.75rem");

        userInfo.add(userName, userRole);

        // Logout button
        Button logoutButton = new Button(new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.getStyle()
            .set("color", "white")
            .set("background", "rgba(255,255,255,0.1)")
            .set("border-radius", "8px");
        logoutButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"))
        );

        userSection.add(avatar, userInfo, logoutButton);
        return userSection;
    }

    private void addDrawerContent() {
        Div logoSection = createLogoSection();
        SideNav navigation = createNavigation();
        Footer footer = createFooter();

        VerticalLayout drawerLayout = new VerticalLayout();
        drawerLayout.setSizeFull();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);

        Scroller scroller = new Scroller(navigation);
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        drawerLayout.add(logoSection, scroller, footer);
        drawerLayout.setFlexGrow(1, scroller);

        addToDrawer(drawerLayout);
    }

    private Div createLogoSection() {
        Div logoSection = new Div();
        logoSection.getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("padding", "1.5rem 1rem")
            .set("text-align", "center")
            .set("color", "white");

        Icon serverIcon = new Icon(VaadinIcon.SERVER);
        serverIcon.setSize("2rem");
        serverIcon.getStyle().set("margin-bottom", "0.5rem");

        H1 appName = new H1("Server Monitor");
        appName.getStyle()
            .set("margin", "0")
            .set("font-size", "1.3rem")
            .set("font-weight", "700");

        Span subtitle = new Span("Sistema de Monitoreo");
        subtitle.getStyle()
            .set("font-size", "0.8rem")
            .set("opacity", "0.9");

        logoSection.add(serverIcon, appName, subtitle);
        return logoSection;
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.getStyle()
            .set("background", "white")
            .set("padding", "1rem 0");

        // Home
        SideNavItem homeItem = new SideNavItem("Inicio", HomeView.class, VaadinIcon.HOME.create());
        homeItem.getStyle().set("margin-bottom", "0.5rem");

        // Dashboard
        SideNavItem dashboardItem = new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create());
        dashboardItem.getStyle().set("margin-bottom", "0.5rem");

        // Databases
        SideNavItem databasesItem = new SideNavItem("Bases de Datos", DatabaseView.class, VaadinIcon.DATABASE.create());
        if (!securityHelper.canViewDatabases()) {
            databasesItem.getStyle().set("opacity", "0.5");
        }
        databasesItem.getStyle().set("margin-bottom", "0.5rem");

        // Configuration
        SideNavItem configItem = new SideNavItem("Configuración", AlertConfigView.class, VaadinIcon.COG.create());
        if (!securityHelper.canViewAlertConfig()) {
            configItem.getStyle().set("opacity", "0.5");
        }
        configItem.getStyle().set("margin-bottom", "0.5rem");

        nav.addItem(homeItem);
        nav.addItem(dashboardItem);
        nav.addItem(databasesItem);
        nav.addItem(configItem);

        // Admin only items
        if (securityHelper.isAdmin()) {
            SideNavItem usersItem = new SideNavItem("Gestión de Usuarios", UserManagementView.class, VaadinIcon.USERS.create());
            usersItem.getStyle().set("margin-bottom", "0.5rem");
            nav.addItem(usersItem);
        }

        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        footer.getStyle()
            .set("background", "#f8fafc")
            .set("padding", "1rem")
            .set("text-align", "center")
            .set("border-top", "1px solid #e2e8f0")
            .set("font-size", "0.8rem")
            .set("color", "#6b7280");

        Span footerText = new Span("Server Monitor v2.0");
        Span roleIndicator = new Span("Rol: " + securityHelper.getCurrentUserRoleDisplay());
        roleIndicator.getStyle()
            .set("display", "block")
            .set("font-weight", "600")
            .set("color", securityHelper.isAdmin() ? "#dc2626" : "#059669")
            .set("margin-top", "0.25rem");

        footer.add(footerText, roleIndicator);
        return footer;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "Server Monitor" : title.value();
    }
}