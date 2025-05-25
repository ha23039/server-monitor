package com.monitoring.server.views.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.data.entity.AlertConfiguration;
import com.monitoring.server.security.Auth0SecurityHelper;
import com.monitoring.server.security.SecurityAnnotations.RequiresOperator;
import com.monitoring.server.service.interfaces.AlertConfigService;
import com.monitoring.server.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Vista para configurar los umbrales de alertas del sistema con seguridad basada en roles.
 * ADMIN: Puede modificar configuraciones
 * OPERATOR: Solo lectura
 */
@PageTitle("Configuración de Umbrales de Alerta")
@Route(value = "config", layout = MainLayout.class)
@RequiresOperator // Requires OPERATOR or higher
public class AlertConfigView extends VerticalLayout {

    private final AlertConfigService alertConfigService;
    private final Auth0SecurityHelper securityHelper;
    private final Map<String, NumberField> thresholdFields = new HashMap<>();
    
    /**
     * Constructor de la vista de configuración de alertas.
     */
    public AlertConfigView(@Autowired AlertConfigService alertConfigService,
                          @Autowired Auth0SecurityHelper securityHelper) {
        this.alertConfigService = alertConfigService;
        this.securityHelper = securityHelper;
        
        try {
            System.out.println("⚙️ CONSTRUCTOR AlertConfigView - INICIO");
            
            addClassName("alert-config-view");
            setSizeFull();
            setSpacing(true);
            setPadding(true);
            
            add(createTitle());
            add(createConfigForm());
            
            // Only show save button for admin
            if (securityHelper.canConfigureAlerts()) {
                add(createSaveButton());
            }
            
            // Inicializar configuraciones por defecto si no existen
            alertConfigService.initDefaultConfigurations();
            
            // Cargar los valores actuales
            loadCurrentValues();
            
            System.out.println("⚙️ CONSTRUCTOR AlertConfigView - ÉXITO");
        } catch (Exception e) {
            System.err.println("❌ ERROR EN AlertConfigView: " + e.getMessage());
            e.printStackTrace();
            add(new H2("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Crea el título de la vista.
     */
    private Component createTitle() {
        H2 title = new H2("Configuración de Umbrales de Alerta");
        title.addClassNames(
            LumoUtility.FontSize.XLARGE,
            LumoUtility.Margin.Bottom.MEDIUM
        );
        
        // Add role indicator
        Span roleInfo = new Span();
        if (securityHelper.canConfigureAlerts()) {
            roleInfo.setText("Modo Administrador - Puede modificar configuraciones");
            roleInfo.getStyle().set("color", "var(--lumo-success-color)");
        } else {
            roleInfo.setText("Modo Solo Lectura - Configuraciones actuales");
            roleInfo.getStyle().set("color", "var(--lumo-warning-color)");
        }
        
        VerticalLayout header = new VerticalLayout(title, roleInfo);
        header.setPadding(false);
        header.setSpacing(false);
        
        return header;
    }
    
    /**
     * Crea el formulario de configuración.
     */
    private Component createConfigForm() {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 1)
        );
        
        NumberField cpuThreshold = createThresholdField("CPU", "%");
        NumberField ramThreshold = createThresholdField("Memory", "%");
        NumberField diskThreshold = createThresholdField("Disk", "%");
        
        thresholdFields.put("CPU", cpuThreshold);
        thresholdFields.put("Memory", ramThreshold);
        thresholdFields.put("Disk", diskThreshold);
        
        // Set read-only for operators
        boolean isReadOnly = !securityHelper.canConfigureAlerts();
        cpuThreshold.setReadOnly(isReadOnly);
        ramThreshold.setReadOnly(isReadOnly);
        diskThreshold.setReadOnly(isReadOnly);
        
        form.addFormItem(cpuThreshold, "Umbral de CPU (%)");
        form.addFormItem(ramThreshold, "Umbral de RAM (%)");
        form.addFormItem(diskThreshold, "Umbral de Disco (%)");
        
        // Add help text for read-only mode
        if (isReadOnly) {
            Span helpText = new Span("📖 Estás viendo las configuraciones actuales en modo solo lectura. " +
                                   "Se requiere rol de Administrador del Sistema para modificar estos valores.");
            helpText.getStyle()
                .set("font-style", "italic")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-top", "1rem");
            
            VerticalLayout formWrapper = new VerticalLayout(form, helpText);
            formWrapper.setPadding(false);
            return formWrapper;
        }
        
        return form;
    }
    
    /**
     * Crea un campo para valor umbral.
     */
    private NumberField createThresholdField(String name, String unit) {
        NumberField field = new NumberField();
        field.setMin(0);
        field.setMax(100);
        field.setStep(1);
        field.setValue(80.0); // Valor por defecto
        field.setId("threshold-" + name.toLowerCase());
        
        Span suffix = new Span(unit);
        suffix.getStyle().set("opacity", "0.7");
        field.setSuffixComponent(suffix);
        
        return field;
    }
    
    /**
     * Crea el botón para guardar la configuración (solo para admin).
     */
    private Component createSaveButton() {
        Button saveButton = new Button("Guardar Configuración");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveConfiguration());
        
        return saveButton;
    }
    
    /**
     * Carga los valores actuales de configuración desde la base de datos.
     */
    private void loadCurrentValues() {
        try {
            for (String componentName : thresholdFields.keySet()) {
                AlertConfiguration config = alertConfigService.findFirstByComponentName(componentName);
                if (config != null) {
                    // Determinar qué valor de umbral usar según el componente
                    double value = 80.0; // Valor por defecto
                    if ("CPU".equals(componentName)) {
                        value = config.getCpuThreshold();
                    } else if ("Memory".equals(componentName)) {
                        value = config.getMemoryThreshold();
                    } else if ("Disk".equals(componentName)) {
                        value = config.getDiskThreshold();
                    }
                    thresholdFields.get(componentName).setValue(value);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error cargando configuraciones: " + e.getMessage());
        }
    }
    
    /**
     * Guarda la configuración en la base de datos (solo para admin).
     */
    private void saveConfiguration() {
        if (!securityHelper.canConfigureAlerts()) {
            showPermissionDeniedNotification();
            return;
        }
        
        try {
            for (String componentName : thresholdFields.keySet()) {
                Double value = thresholdFields.get(componentName).getValue();
                AlertConfiguration config = alertConfigService.findFirstByComponentName(componentName);
                
                if (config != null) {
                    // Actualizar el umbral según el componente
                    if ("CPU".equals(componentName)) {
                        config.setCpuThreshold(value);
                        config.setThresholdValue(value);
                    } else if ("Memory".equals(componentName)) {
                        config.setMemoryThreshold(value);
                        config.setThresholdValue(value);
                    } else if ("Disk".equals(componentName)) {
                        config.setDiskThreshold(value);
                        config.setThresholdValue(value);
                    }
                    
                    // Activar la configuración automáticamente al guardarla
                    config.setActive(true);
                    config.setEnabled(true);
                    alertConfigService.save(config);
                } else {
                    // Si no existe, crear una nueva configuración
                    AlertConfiguration newConfig = new AlertConfiguration();
                    newConfig.setName(componentName + " Alert Config");
                    newConfig.setComponentName(componentName);
                    
                    // Establecer el umbral según el componente
                    if ("CPU".equals(componentName)) {
                        newConfig.setCpuThreshold(value);
                        newConfig.setThresholdValue(value);
                    } else if ("Memory".equals(componentName)) {
                        newConfig.setMemoryThreshold(value);
                        newConfig.setThresholdValue(value);
                    } else if ("Disk".equals(componentName)) {
                        newConfig.setDiskThreshold(value);
                        newConfig.setThresholdValue(value);
                    }
                    
                    // Activar la configuración automáticamente
                    newConfig.setActive(true);
                    newConfig.setEnabled(true);
                    alertConfigService.save(newConfig);
                }
            }
            
            Notification notification = Notification.show("Configuración guardada con éxito");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification notification = Notification.show("Error al guardar la configuración: " + e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    /**
     * Muestra notificación de permisos insuficientes.
     */
    private void showPermissionDeniedNotification() {
        Notification notification = Notification.show(
            "No tienes permisos suficientes para realizar esta acción. Se requiere rol de Administrador del Sistema."
        );
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}