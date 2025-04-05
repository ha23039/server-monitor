package com.monitoring.server.views.config;

import java.util.HashMap;
import java.util.Map;

import com.monitoring.server.data.entity.AlertConfiguration;
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

import jakarta.annotation.security.PermitAll;

/**
 * Vista para configurar los umbrales de alertas del sistema.
 * Permite establecer los valores para generar alertas en componentes como CPU, RAM y disco.
 */
@PageTitle("Configuración de Umbrales de Alerta")
@Route(value = "config", layout = MainLayout.class)
@PermitAll
public class AlertConfigView extends VerticalLayout {

    private final AlertConfigService alertConfigService;
    private final Map<String, NumberField> thresholdFields = new HashMap<>();
    
    /**
     * Constructor de la vista de configuración de alertas.
     * @param alertConfigService servicio para gestionar configuraciones de alertas
     */
    public AlertConfigView(AlertConfigService alertConfigService) {
        this.alertConfigService = alertConfigService;
        
        addClassName("alert-config-view");
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        
        add(createTitle());
        add(createConfigForm());
        add(createSaveButton());
        
        // Inicializar configuraciones por defecto si no existen
        alertConfigService.initDefaultConfigurations();
        
        // Cargar los valores actuales
        loadCurrentValues();
    }
    
    /**
     * Crea el título de la vista.
     * @return componente con el título
     */
    private Component createTitle() {
        H2 title = new H2("Configuración de Umbrales de Alerta");
        title.addClassNames(
            LumoUtility.FontSize.XLARGE,
            LumoUtility.Margin.Bottom.MEDIUM
        );
        
        VerticalLayout header = new VerticalLayout(title);
        header.setPadding(false);
        header.setSpacing(false);
        
        return header;
    }
    
    /**
     * Crea el formulario de configuración.
     * @return componente con el formulario
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
        
        form.addFormItem(cpuThreshold, "Umbral de CPU (%)");
        form.addFormItem(ramThreshold, "Umbral de RAM (%)");
        form.addFormItem(diskThreshold, "Umbral de Disco (%)");
        
        return form;
    }
    
    /**
     * Crea un campo para valor umbral.
     * @param name nombre del componente (CPU, RAM, Disco)
     * @param unit unidad de medida (%, MB, etc.)
     * @return campo configurado para valores umbral
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
     * Crea el botón para guardar la configuración.
     * @return botón de guardar
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
    }
    
    /**
     * Guarda la configuración en la base de datos.
     */
    private void saveConfiguration() {
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
}