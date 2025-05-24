package com.monitoring.server.views.error;

import com.monitoring.server.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Acceso Denegado - Server Monitor")
@Route(value = "access-denied", layout = MainLayout.class)
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout {

    private static final String COLOR_WARNING = "#F59E0B";
    private static final String COLOR_TEXT_PRIMARY = "#1F2937";
    private static final String COLOR_TEXT_SECONDARY = "#6B7280";

    public AccessDeniedView() {
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        Div container = createErrorContainer();
        add(container);
    }

    private Div createErrorContainer() {
        Div container = new Div();
        container.getStyle()
            .set("background-color", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-xl)")
            .set("box-shadow", "var(--lumo-box-shadow-l)")
            .set("text-align", "center")
            .set("max-width", "500px");

        Icon lockIcon = VaadinIcon.LOCK.create();
        lockIcon.setSize("4em");
        lockIcon.setColor(COLOR_WARNING);
        lockIcon.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        H1 title = new H1("🔒 Acceso Denegado");
        title.getStyle()
            .set("color", COLOR_TEXT_PRIMARY)
            .set("font-size", "2rem")
            .set("margin", "0 0 var(--lumo-space-m) 0");

        Paragraph message = new Paragraph(
            "No tienes permisos suficientes para acceder a esta sección. " +
            "Tu rol actual no permite el acceso a esta funcionalidad. " +
            "Contacta al administrador si necesitas permisos adicionales."
        );
        message.getStyle()
            .set("color", COLOR_TEXT_SECONDARY)
            .set("font-size", "1rem")
            .set("margin-bottom", "var(--lumo-space-l)")
            .set("line-height", "1.6");

        Button homeButton = new Button("🏠 Volver al Inicio", VaadinIcon.HOME.create());
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.getStyle().set("margin-right", "var(--lumo-space-s)");
        homeButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(""))
        );

        Button dashboardButton = new Button("📊 Dashboard", VaadinIcon.DASHBOARD.create());
        dashboardButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dashboardButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate("dashboard"))
        );

        VerticalLayout content = new VerticalLayout(lockIcon, title, message, 
            new Div(homeButton, dashboardButton));
        content.setSpacing(false);
        content.setPadding(false);
        content.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        container.add(content);
        return container;
    }
}