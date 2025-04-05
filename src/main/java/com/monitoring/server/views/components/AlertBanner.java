package com.monitoring.server.views.components;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Componente para mostrar un banner de alertas cuando alguna métrica supera el umbral
 */
public class AlertBanner extends HorizontalLayout {

    private final Span messageLabel;
    private final Button detailsButton;
    private Map<String, Double> alertValues;
    private Map<String, Double> thresholdValues;
    
    /**
     * Constructor para crear un banner de alertas
     */
    public AlertBanner() {
        addClassName("alert-banner");
        
        // Configurar layout
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
        
        // Configurar estilos
        getStyle().set("background-color", "var(--lumo-error-color-10)");
        getStyle().set("border-left", "4px solid var(--lumo-error-color)");
        getStyle().set("border-radius", "var(--lumo-border-radius)");
        getStyle().set("margin-bottom", "1em");
        
        // Crear componentes
        Icon alertIcon = VaadinIcon.EXCLAMATION_CIRCLE.create();
        alertIcon.setColor("var(--lumo-error-color)");
        alertIcon.getStyle().set("margin-right", "0.5em");
        
        messageLabel = new Span("Hay alertas activas en el sistema");
        messageLabel.getStyle().set("flex-grow", "1");
        
        detailsButton = new Button("Detalles", new Icon(VaadinIcon.ANGLE_RIGHT));
        detailsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        detailsButton.addClickListener(e -> showAlertDetails());
        
        // Añadir componentes
        add(alertIcon, messageLabel, detailsButton);
        
        // Inicializar estado
        alertValues = new HashMap<>();
        thresholdValues = new HashMap<>();
    }
    
    /**
     * Establece el estado de alerta del banner
     * @param hasAlerts true si hay alertas, false en caso contrario
     * @param alertValues Mapa con los valores actuales de las métricas en alerta
     * @param thresholdValues Mapa con los umbrales de las métricas en alerta
     */
    public void setAlerts(boolean hasAlerts, Map<String, Double> alertValues, Map<String, Double> thresholdValues) {
        this.alertValues = alertValues != null ? alertValues : new HashMap<>();
        this.thresholdValues = thresholdValues != null ? thresholdValues : new HashMap<>();
        
        // Actualizar visibilidad
        setVisible(hasAlerts);
        
        if (hasAlerts) {
            // Actualizar mensaje según las métricas en alerta
            StringBuilder message = new StringBuilder("¡Alerta! ");
            
            if (this.alertValues.size() == 1) {
                String metric = this.alertValues.keySet().iterator().next();
                message.append("El uso de ").append(metric).append(" ha superado el umbral crítico.");
            } else {
                message.append("Hay ").append(this.alertValues.size()).append(" métricas que superan los umbrales críticos.");
            }
            
            messageLabel.setText(message.toString());
        }
    }
    
    /**
     * Muestra una notificación con los detalles de las alertas
     */
    private void showAlertDetails() {
        if (alertValues == null || alertValues.isEmpty()) {
            return;
        }
        
        // Crear contenido de la notificación
        Div content = new Div();
        content.getStyle().set("white-space", "pre-line");
        
        StringBuilder message = new StringBuilder("Detalles de las alertas:\n\n");
        
        for (Map.Entry<String, Double> entry : alertValues.entrySet()) {
            String metric = entry.getKey();
            double value = entry.getValue();
            double threshold = thresholdValues.getOrDefault(metric, 0.0);
            
            message.append("• ").append(metric).append(": ")
                   .append(String.format("%.1f%%", value))
                   .append(" (umbral: ").append(String.format("%.1f%%", threshold))
                   .append(")\n");
        }
        
        content.setText(message.toString());
        
        // Mostrar notificación
        Notification notification = new Notification(content);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.setDuration(5000);
        notification.open();
    }
}