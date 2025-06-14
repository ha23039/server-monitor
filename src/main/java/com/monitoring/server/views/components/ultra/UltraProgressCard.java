package com.monitoring.server.views.components.ultra;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * 🚀 Tarjeta de progreso ultra avanzada con animaciones
 */
public class UltraProgressCard extends Div {
    
    private final H3 titleLabel;
    private final Span valueLabel;
    private final Span statusLabel;
    private final Div progressRing;
    private final Icon metricIcon;
    private final double threshold;
    private double currentValue = 0;
    
    public UltraProgressCard(String title, String iconType, double threshold) {
        this.threshold = threshold;
        
        addClassName("ultra-progress-card");
        setupCardStyling();
        
        // Crear icono dinámico
        metricIcon = createDynamicIcon(iconType);
        
        // Crear elementos
        titleLabel = new H3(title);
        titleLabel.addClassName("card-title");
        titleLabel.getStyle()
            .set("margin", "0")
            .set("color", "#F9FAFB")
            .set("font-size", "1.1rem")
            .set("font-weight", "600");
        
        valueLabel = new Span("0%");
        valueLabel.addClassName("card-value");
        valueLabel.getStyle()
            .set("font-size", "2.5rem")
            .set("font-weight", "700")
            .set("color", "#4F46E5")
            .set("line-height", "1");
        
        statusLabel = new Span("Óptimo");
        statusLabel.addClassName("card-status");
        statusLabel.getStyle()
            .set("font-size", "0.9rem")
            .set("font-weight", "500")
            .set("color", "#10B981");
        
        progressRing = createProgressRing();
        
        // Ensamblar layout
        assembleCard();
        
        // Configurar animaciones iniciales
        setupAnimations();
    }
    
    private void setupCardStyling() {
        getStyle()
            .set("background", "linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)")
            .set("border-radius", "20px")
            .set("padding", "2rem")
            .set("border", "1px solid rgba(255,255,255,0.1)")
            .set("backdrop-filter", "blur(20px)")
            .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)")
            .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
            .set("cursor", "pointer")
            .set("position", "relative")
            .set("overflow", "hidden")
            .set("min-height", "200px");
            
        // Efectos hover ultra suaves
        getElement().addEventListener("mouseenter", e -> {
            getStyle()
                .set("transform", "translateY(-8px)")
                .set("box-shadow", "0 16px 48px rgba(0,0,0,0.2)");
        });
        
        getElement().addEventListener("mouseleave", e -> {
            getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)");
        });
    }
    
    private Icon createDynamicIcon(String iconType) {
        Icon icon = switch(iconType) {
            case "processor" -> VaadinIcon.CLUSTER.create();
            case "memory" -> VaadinIcon.RECORDS.create();
            case "storage" -> VaadinIcon.STORAGE.create();
            default -> VaadinIcon.CHART.create();
        };
        
        icon.setSize("2rem");
        icon.setColor("#4F46E5");
        icon.addClassName("metric-icon");
        return icon;
    }
    
    private Div createProgressRing() {
        Div ring = new Div();
        ring.addClassName("progress-ring");
        ring.getStyle()
            .set("width", "80px")
            .set("height", "80px")
            .set("position", "relative");
            
        ring.getElement().setProperty("innerHTML", 
            "<svg width='80' height='80' style='transform: rotate(-90deg);'>" +
            "<circle cx='40' cy='40' r='35' fill='none' stroke='rgba(255,255,255,0.1)' stroke-width='6'/>" +
            "<circle cx='40' cy='40' r='35' fill='none' stroke='#4F46E5' stroke-width='6' " +
            "stroke-linecap='round' stroke-dasharray='220' stroke-dashoffset='220' " +
            "style='transition: stroke-dashoffset 1s ease;'/>" +
            "</svg>");
        return ring;
    }
    
    private void assembleCard() {
        // Header con icono y título
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);
        header.add(metricIcon, titleLabel);
        header.setSpacing(true);
        header.getStyle().set("margin-bottom", "1rem");
        
        // Sección central con valor y anillo
        HorizontalLayout centerSection = new HorizontalLayout();
        centerSection.setAlignItems(HorizontalLayout.Alignment.CENTER);
        centerSection.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        centerSection.setWidthFull();
        
        VerticalLayout valueSection = new VerticalLayout();
        valueSection.setPadding(false);
        valueSection.setSpacing(false);
        valueSection.add(valueLabel, statusLabel);
        
        centerSection.add(valueSection, progressRing);
        
        // Layout principal
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);
        mainLayout.add(header, centerSection);
        
        add(mainLayout);
    }
    
    private void setupAnimations() {
        getElement().executeJs("""
            // Animación de entrada suave
            this.style.opacity = '0';
            this.style.transform = 'scale(0.95) translateY(20px)';
            
            requestAnimationFrame(() => {
                this.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
                this.style.opacity = '1';
                this.style.transform = 'scale(1) translateY(0)';
            });
        """);
    }
    
    public void updateValue(double value, boolean isAlert) {
        this.currentValue = value;
        
        getUI().ifPresent(ui -> ui.access(() -> {
            // Actualizar valor con animación
            valueLabel.setText(String.format("%.1f%%", value));
            
            // Actualizar estado
            updateStatus(value, isAlert);
            
            // Animar anillo de progreso
            animateProgressRing(value);
            
            // Efectos visuales según estado
            updateVisualEffects(value, isAlert);
        }));
    }
    
    private void updateStatus(double value, boolean isAlert) {
        String status;
        String color;
        
        if (isAlert) {
            status = "🚨 Crítico";
            color = "#EF4444";
        } else if (value >= threshold * 0.8) {
            status = "⚠️ Alto";
            color = "#F59E0B";
        } else if (value >= threshold * 0.6) {
            status = "📊 Normal";
            color = "#10B981";
        } else {
            status = "✅ Óptimo";
            color = "#06B6D4";
        }
        
        statusLabel.setText(status);
        statusLabel.getStyle().set("color", color);
        valueLabel.getStyle().set("color", color);
    }
    
    private void animateProgressRing(double value) {
        double percentage = Math.min(value, 100);
        double circumference = 2 * Math.PI * 35; // radio = 35
        double offset = circumference - (percentage / 100) * circumference;
        
        getElement().executeJs("""
            const circles = this.querySelectorAll('circle');
            if (circles.length > 1) {
                const progressCircle = circles[1];
                progressCircle.style.strokeDashoffset = $0;
            }
        """, offset);
    }
    
    private void updateVisualEffects(double value, boolean isAlert) {
        if (isAlert) {
            // Efecto de pulso para alertas críticas
            getElement().executeJs("""
                this.style.borderColor = '#EF4444';
                this.style.boxShadow = '0 16px 48px rgba(239, 68, 68, 0.3)';
                
                // Añadir animación de pulso
                if (!document.querySelector('#card-pulse-animation')) {
                    const style = document.createElement('style');
                    style.id = 'card-pulse-animation';
                    style.textContent = `
                        @keyframes cardPulse {
                            0%, 100% { transform: scale(1); }
                            50% { transform: scale(1.02); }
                        }
                    `;
                    document.head.appendChild(style);
                }
                this.style.animation = 'cardPulse 2s infinite';
            """);
        } else {
            // Restaurar estado normal
            getElement().executeJs("""
                this.style.animation = 'none';
                this.style.borderColor = 'rgba(255,255,255,0.1)';
                this.style.boxShadow = '0 8px 32px rgba(0,0,0,0.1)';
            """);
        }
    }
}