package com.monitoring.server.views;

import com.monitoring.server.views.config.AlertConfigView;
import com.monitoring.server.views.dashboard.DashboardView;
import com.monitoring.server.views.databases.DatabaseView;
import com.monitoring.server.views.home.HomeView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Diseño principal de la aplicación.
 * Contiene la barra superior y el menú lateral.
 */
public class MainLayout extends AppLayout {

    public MainLayout() {
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

        addToNavbar(toggle, logo);
    }

    private void createDrawer() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addClassNames(LumoUtility.Width.FULL);

        // Home
        tabs.add(createTab(HomeView.class, VaadinIcon.HOME, "Home"));
        
        // Dashboard
        tabs.add(createTab(DashboardView.class, VaadinIcon.DASHBOARD, "Dashboard"));
        
        // Bases de datos
        tabs.add(createTab(DatabaseView.class, VaadinIcon.DATABASE, "Bases de Datos"));
    
        // Configuraciones
        tabs.add(createTab(AlertConfigView.class, VaadinIcon.COGS, "Configuración"));
        
        addToDrawer(tabs);
    }
    
    private Tab createTab(Class<? extends Component> viewClass, VaadinIcon viewIcon, String viewName) {
        RouterLink link = new RouterLink(viewClass);
        
        Icon icon = viewIcon.create();
        icon.setSize("18px");
        
        link.add(icon);
        link.add(new Span(viewName));
        
        return new Tab(link);
    }
}