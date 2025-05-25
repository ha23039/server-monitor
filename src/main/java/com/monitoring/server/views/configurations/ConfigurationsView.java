package com.monitoring.server.views.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.monitoring.server.security.MenuSecurityHelper; // ✅ CORRECTO: Usar MenuSecurityHelper
import com.monitoring.server.security.SecurityAnnotations.RequiresOperator;
import com.monitoring.server.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

@PageTitle("Configurations")
@RequiresOperator
@Route(value = "configurations", layout = MainLayout.class)
@Menu(order = 3, icon = LineAwesomeIconUrl.COG_SOLID)
public class ConfigurationsView extends Composite<VerticalLayout> {

    private MenuSecurityHelper securityHelper; // ✅ CORRECTO: Usar MenuSecurityHelper

    private TextField cpuThresholdField;
    private TextField ramThresholdField;
    private TextField diskThresholdField;
    private Button saveButton;

    public ConfigurationsView(@Autowired MenuSecurityHelper securityHelper) { // ✅ CORRECTO
        try {
            System.out.println("🔧 CONSTRUCTOR ConfigurationsView - INICIO");
            
            this.securityHelper = securityHelper;
            initializeComponents();
            setupLayout();
            setupPermissions();
            
            System.out.println("🔧 CONSTRUCTOR ConfigurationsView - ÉXITO");
        } catch (Exception e) {
            System.err.println("❌ ERROR EN CONFIGURATIONSVIEW: " + e.getMessage());
            e.printStackTrace();
            getContent().add(new H1("Error en ConfigurationsView: " + e.getMessage()));
        }
    }

    private void initializeComponents() {
        cpuThresholdField = new TextField("Umbral de CPU (%)");
        cpuThresholdField.setWidth("100%");
        cpuThresholdField.setValue("80"); // Valor por defecto
        cpuThresholdField.setPlaceholder("Ej: 80");

        ramThresholdField = new TextField("Umbral de RAM (%)");
        ramThresholdField.setWidth("100%");
        ramThresholdField.setValue("85"); // Valor por defecto
        ramThresholdField.setPlaceholder("Ej: 85");

        diskThresholdField = new TextField("Umbral de Disco (%)");
        diskThresholdField.setWidth("100%");
        diskThresholdField.setValue("90"); // Valor por defecto
        diskThresholdField.setPlaceholder("Ej: 90");

        saveButton = new Button("Guardar Configuración");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveConfiguration());
    }

    private void setupLayout() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        Icon icon = new Icon();
        H1 h1 = new H1();

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(JustifyContentMode.START);
        getContent().setAlignItems(Alignment.CENTER);
        getContent().setPadding(true);
        getContent().setSpacing(true);

        layoutRow.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("50px");

        layoutRow2.setHeightFull();
        layoutRow.setFlexGrow(1.0, layoutRow2);
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.setWidth("100%");
        layoutRow2.setHeight("50px");

        icon.setIcon("lumo:user");
        icon.setSize("50px");

        h1.setText("Configuración de Umbrales de Alerta");
        h1.setWidth("max-content");

        getContent().setAlignSelf(FlexComponent.Alignment.END, saveButton);
        saveButton.setWidth("min-content");

        getContent().add(layoutRow);
        layoutRow.add(layoutRow2);
        layoutRow2.add(icon);
        layoutRow2.add(h1);

        // Agregar indicador de permisos
        getContent().add(createPermissionIndicator());

        getContent().add(cpuThresholdField);
        getContent().add(ramThresholdField);
        getContent().add(diskThresholdField);
        getContent().add(saveButton);
    }

    private void setupPermissions() {
        boolean isReadOnly = !securityHelper.canConfigureAlerts();
        
        cpuThresholdField.setReadOnly(isReadOnly);
        ramThresholdField.setReadOnly(isReadOnly);
        diskThresholdField.setReadOnly(isReadOnly);
        saveButton.setVisible(!isReadOnly);

        if (isReadOnly) {
            cpuThresholdField.setHelperText("Solo lectura - Requiere permisos de administrador");
            ramThresholdField.setHelperText("Solo lectura - Requiere permisos de administrador");
            diskThresholdField.setHelperText("Solo lectura - Requiere permisos de administrador");
        }
    }

    private Div createPermissionIndicator() {
        Div indicator = new Div();
        indicator.getStyle()
            .set("padding", "1rem")
            .set("margin-bottom", "1rem")
            .set("border-radius", "8px")
            .set("border", "2px solid")
            .set("text-align", "center");

        Span statusIcon = new Span();
        Span statusText = new Span();
        Span userInfo = new Span();

        if (securityHelper.canConfigureAlerts()) {
            statusIcon.setText("✅");
            statusText.setText("Modo Administrador - Acceso completo");
            userInfo.setText("Usuario: " + securityHelper.getCurrentUserName() + " | Rol: " + securityHelper.getCurrentUserRoleDisplay());
            
            indicator.getStyle()
                .set("background", "#d4edda")
                .set("border-color", "#28a745")
                .set("color", "#155724");
        } else {
            statusIcon.setText("👁️");
            statusText.setText("Modo Solo Lectura");
            userInfo.setText("Usuario: " + securityHelper.getCurrentUserName() + " | Rol: " + securityHelper.getCurrentUserRoleDisplay());
            
            indicator.getStyle()
                .set("background", "#fff3cd")
                .set("border-color", "#ffc107")
                .set("color", "#856404");
        }

        statusIcon.getStyle().set("font-size", "1.5rem");
        statusText.getStyle().set("font-weight", "bold").set("display", "block");
        userInfo.getStyle().set("font-size", "0.9rem").set("display", "block").set("margin-top", "0.5rem");

        indicator.add(statusIcon, statusText, userInfo);
        return indicator;
    }

    private void saveConfiguration() {
        if (!securityHelper.canConfigureAlerts()) {
            Notification notification = Notification.show(
                "❌ No tienes permisos para modificar la configuración. Se requiere rol de Administrador."
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            // Validar valores
            double cpuValue = Double.parseDouble(cpuThresholdField.getValue());
            double ramValue = Double.parseDouble(ramThresholdField.getValue());
            double diskValue = Double.parseDouble(diskThresholdField.getValue());

            if (cpuValue < 0 || cpuValue > 100 || ramValue < 0 || ramValue > 100 || diskValue < 0 || diskValue > 100) {
                throw new IllegalArgumentException("Los valores deben estar entre 0 y 100");
            }

            // Aquí iría la lógica para guardar en la base de datos
            // Por ahora solo mostramos notificación de éxito
            
            Notification notification = Notification.show(
                "✅ Configuración guardada exitosamente\n" +
                "CPU: " + cpuValue + "% | RAM: " + ramValue + "% | Disco: " + diskValue + "%"
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(5000);

        } catch (NumberFormatException e) {
            Notification notification = Notification.show(
                "❌ Error: Ingresa valores numéricos válidos"
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (IllegalArgumentException e) {
            Notification notification = Notification.show(
                "❌ Error: " + e.getMessage()
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}