package com.monitoring.server.views.home;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.monitoring.server.views.MainLayout;
import com.monitoring.server.views.config.AlertConfigView;
import com.monitoring.server.views.dashboard.DashboardView;
import com.monitoring.server.views.databases.DatabaseView;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

@PageTitle("Home")
@Route(value = "home", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class HomeView extends Composite<VerticalLayout> {

    public HomeView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        Icon icon = new Icon();
        H1 h1 = new H1();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        Paragraph textLarge = new Paragraph();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        
        // Botones con rutas de navegación
        Button buttonPrimary = new Button("Dashboard", event -> 
            getUI().ifPresent(ui -> ui.navigate(DashboardView.class))
        );
        
        Button buttonPrimary2 = new Button("Databases", event -> 
            getUI().ifPresent(ui -> ui.navigate(DatabaseView.class))
        );
        
        Button buttonPrimary3 = new Button("Config", event -> 
            getUI().ifPresent(ui -> ui.navigate(AlertConfigView.class))
        );

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("50px");
        
        icon.setIcon("lumo:user");
        icon.setSize("50px");
        
        h1.setText("CRUD MONITOR DE SERVIDORES");
        h1.setWidth("max-content");
        
        layoutColumn2.setWidthFull();
        getContent().setFlexGrow(1.0, layoutColumn2);
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        
        textLarge.setText(
                "Bienvenido a la plataforma centralizada de monitoreo de sistemas. Obtenga visibilidad completa del rendimiento de su infraestructura, bases de datos y aplicaciones. Reciba alertas proactivas, analice tendencias y optimice sus recursos para garantizar la máxima disponibilidad y eficiencia.");
        textLarge.setWidth("100%");
        textLarge.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        
        layoutRow2.setWidthFull();
        layoutColumn2.setFlexGrow(1.0, layoutRow2);
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.setWidth("100%");
        layoutRow2.getStyle().set("flex-grow", "1");
        
        // Configuración de los botones
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        buttonPrimary2.setWidth("min-content");
        buttonPrimary2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        buttonPrimary3.setWidth("min-content");
        buttonPrimary3.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        

        // Añadir componentes al layout
        getContent().add(layoutRow);
        layoutRow.add(icon);
        layoutRow.add(h1);
        
        getContent().add(layoutColumn2);
        layoutColumn2.add(textLarge);
        layoutColumn2.add(layoutRow2);
        
        layoutRow2.add(buttonPrimary);
        layoutRow2.add(buttonPrimary2);
        layoutRow2.add(buttonPrimary3);
    }
}