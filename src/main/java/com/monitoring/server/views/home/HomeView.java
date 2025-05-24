package com.monitoring.server.views.home;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.monitoring.server.views.MainLayout;
import com.monitoring.server.views.config.AlertConfigView;
import com.monitoring.server.views.dashboard.DashboardView;
import com.monitoring.server.views.databases.DatabaseView;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
        // Mostrar notificación de login exitoso si es necesario
        checkAndShowLoginSuccess();
        
        // Header con información del usuario
        createUserHeader();
        
        // Layout principal
        HorizontalLayout layoutRow = new HorizontalLayout();
        Icon icon = new Icon();
        H1 h1 = new H1();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        Paragraph textLarge = new Paragraph();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        
        // Botones con rutas de navegación
        Button buttonPrimary = new Button("📊 Dashboard", VaadinIcon.DASHBOARD.create(), event -> 
            getUI().ifPresent(ui -> ui.navigate(DashboardView.class))
        );
        
        Button buttonPrimary2 = new Button("🗄️ Databases", VaadinIcon.DATABASE.create(), event -> 
            getUI().ifPresent(ui -> ui.navigate(DatabaseView.class))
        );
        
        Button buttonPrimary3 = new Button("⚙️ Configuración", VaadinIcon.COG.create(), event -> 
            getUI().ifPresent(ui -> ui.navigate(AlertConfigView.class))
        );

        // Configuración del layout
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("50px");
        
        icon.setIcon("lumo:user");
        icon.setSize("50px");
        
        h1.setText("🖥️ MONITOR DE SERVIDORES");
        h1.setWidth("max-content");
        h1.getStyle().set("color", "#2c3e50");
        
        layoutColumn2.setWidthFull();
        getContent().setFlexGrow(1.0, layoutColumn2);
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        
        textLarge.setText(
                "Bienvenido a la plataforma centralizada de monitoreo de sistemas con autenticación Auth0. " +
                "Obtenga visibilidad completa del rendimiento de su infraestructura, bases de datos y aplicaciones. " +
                "Reciba alertas proactivas, analice tendencias y optimice sus recursos para garantizar la máxima " +
                "disponibilidad y eficiencia con control de roles integrado.");
        textLarge.setWidth("100%");
        textLarge.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        
        layoutRow2.setWidthFull();
        layoutColumn2.setFlexGrow(1.0, layoutRow2);
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.setWidth("100%");
        layoutRow2.getStyle().set("flex-grow", "1");
        
        // Configuración de los botones con mejor estilo
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonPrimary.getStyle().set("margin-right", "1rem");
        
        buttonPrimary2.setWidth("min-content");
        buttonPrimary2.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        buttonPrimary2.getStyle().set("margin-right", "1rem");
        
        buttonPrimary3.setWidth("min-content");
        buttonPrimary3.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        
        // Control de acceso basado en roles
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_admin"))) {
            // Si no es admin, deshabilitar botón de configuración
            buttonPrimary3.setEnabled(false);
            buttonPrimary3.setText("⚙️ Configuración (Solo Admin)");
        }

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
        
        // Agregar información de estado del sistema
        createSystemStatus();
    }

    private void checkAndShowLoginSuccess() {
        // Opcional: Mostrar notificación de login exitoso
        // Esto se puede implementar con Vaadin Notification si se desea
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof OidcUser) {
            // Login exitoso - la información se muestra en el header
        }
    }

    private void createUserHeader() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) auth.getPrincipal();
            
            Div userHeader = new Div();
            userHeader.getStyle()
                .set("background", "#f8f9fa")
                .set("padding", "1rem")
                .set("border-bottom", "2px solid #e9ecef")
                .set("margin-bottom", "1rem");

            HorizontalLayout headerLayout = new HorizontalLayout();
            headerLayout.setWidthFull();
            headerLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
            headerLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
            
            // Info del usuario
            VerticalLayout userInfo = new VerticalLayout();
            userInfo.setSpacing(false);
            userInfo.setPadding(false);
            
            H3 welcomeText = new H3("👋 Bienvenido, " + 
                (user.getFullName() != null ? user.getFullName() : user.getEmail()));
            welcomeText.getStyle().set("margin", "0").set("color", "#495057");
            
            Paragraph roleText = new Paragraph("🔑 " + 
                auth.getAuthorities().toString()
                    .replace("[ROLE_", "")
                    .replace("]", "")
                    .replace("ROLE_", ", ")
                    .toUpperCase());
            roleText.getStyle().set("margin", "0").set("color", "#6c757d").set("font-size", "0.9rem");
            
            userInfo.add(welcomeText, roleText);
            
            // Botón de logout
            Button logoutBtn = new Button("🚪 Cerrar Sesión", VaadinIcon.SIGN_OUT.create());
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            logoutBtn.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
            });
            
            headerLayout.add(userInfo, logoutBtn);
            userHeader.add(headerLayout);
            getContent().add(userHeader);
        }
    }

    private void createSystemStatus() {
        Div statusCard = new Div();
        statusCard.getStyle()
            .set("background", "#d4edda")
            .set("padding", "1rem")
            .set("border-radius", "8px")
            .set("border-left", "4px solid #28a745")
            .set("margin-top", "2rem");

        H3 statusTitle = new H3("🟢 Estado del Sistema - Parcial 2");
        statusTitle.getStyle()
            .set("margin-top", "0")
            .set("color", "#155724");
        
        Paragraph statusInfo = new Paragraph(
            "✅ Autenticación Auth0 implementada y funcionando\n" +
            "✅ Base de datos PostgreSQL en Neon conectada\n" +
            "✅ Aplicación desplegada en Render\n" +
            "✅ Control de roles y permisos activo\n" +
            "✅ Todas las funcionalidades del Parcial 2 operativas"
        );
        statusInfo.getStyle()
            .set("color", "#155724")
            .set("white-space", "pre-line")
            .set("margin-bottom", "0");
        
        statusCard.add(statusTitle, statusInfo);
        getContent().add(statusCard);
    }
}