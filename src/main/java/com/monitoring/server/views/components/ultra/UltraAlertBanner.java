package com.monitoring.server.views.components.ultra;

import java.util.Map;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * 🚀 Banner de alertas ultra avanzado
 */
public class UltraAlertBanner extends HorizontalLayout {
    
    private final Icon alertIcon;
    private final Span messageLabel;
    private final Button dismissButton;
    private final Div pulseIndicator;
    
    public UltraAlertBanner() {
        addClassName("ultra-alert-banner");
        setVisible(false);
        setupBannerStyling();
        
        // Crear componentes
        alertIcon = VaadinIcon.WARNING.create();
        alertIcon.setSize("1.5rem");
        
        messageLabel = new Span();
        messageLabel.addClassName("alert-message");
        
        pulseIndicator = new Div();
        pulseIndicator.addClassName("pulse-indicator");
        pulseIndicator.getStyle()
            .set("width", "12px")
            .set("height", "12px")
            .set("border-radius", "50%")
            .set("background", "#EF4444")
            .set("margin-right", "0.5rem");
        
        dismissButton = new Button("✕");
        dismissButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        dismissButton.addClickListener(e -> setVisible(false));
        dismissButton.getStyle()
            .set("color", "white")
            .set("font-weight", "bold");
        
        // Ensamblar banner
        add(pulseIndicator, alertIcon, messageLabel, dismissButton);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        expand(messageLabel);
    }
    
    private void setupBannerStyling() {
        getStyle()
            .set("background", "linear-gradient(135deg, rgba(239, 68, 68, 0.9) 0%, rgba(220, 38, 38, 0.8) 100%)")
            .set("border-radius", "16px")
            .set("padding", "1rem 1.5rem")
            .set("border", "1px solid rgba(239, 68, 68, 0.3)")
            .set("backdrop-filter", "blur(10px)")
            .set("box-shadow", "0 8px 32px rgba(239, 68, 68, 0.2)")
            .set("color", "white")
            .set("font-weight", "600")
            .set("width", "100%");
    }
    
    public void updateAlerts(Map<String, Double> alerts) {
        if (alerts.isEmpty()) {
            setVisible(false);
            return;
        }
        
        // Construir mensaje
        StringBuilder message = new StringBuilder("🚨 ");
        if (alerts.size() == 1) {
            String metric = alerts.keySet().iterator().next();
            message.append("Alerta crítica en ").append(metric);
        } else {
            message.append(alerts.size()).append(" métricas en estado crítico");
        }
        
        messageLabel.setText(message.toString());
        setVisible(true);
        
        // Activar animación de pulso
        activatePulseAnimation();
    }
    
    private void activatePulseAnimation() {
        getElement().executeJs("""
            // Animación de pulso para el indicador
            const indicator = this.querySelector('.pulse-indicator');
            if (indicator) {
                indicator.style.animation = 'pulse 1.5s infinite';
            }
            
            // Definir animación CSS si no existe
            if (!document.querySelector('#pulse-animation')) {
                const style = document.createElement('style');
                style.id = 'pulse-animation';
                style.textContent = `
                    @keyframes pulse {
                        0% { transform: scale(1); opacity: 1; }
                        50% { transform: scale(1.2); opacity: 0.7; }
                        100% { transform: scale(1); opacity: 1; }
                    }
                `;
                document.head.appendChild(style);
            }
        """);
    }
}