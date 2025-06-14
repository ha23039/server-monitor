// UltraMetricChart.java - Versión simplificada pero funcional
package com.monitoring.server.views.components.ultra;

import java.util.List;
import com.monitoring.server.data.entity.SystemMetric;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * 🚀 Gráfico ultra simplificado pero funcional
 * (Versión básica mientras implementamos Chart.js completo)
 */
public class UltraMetricChart extends Div {
    
    private final VerticalLayout chartContainer;
    private final Span cpuDisplay;
    private final Span memoryDisplay;
    private final Span diskDisplay;
    private final Span statusDisplay;
    private String chartType = "LINE";
    
    public UltraMetricChart() {
        addClassName("ultra-metric-chart");
        setupChartStyling();
        
        chartContainer = new VerticalLayout();
        chartContainer.setPadding(false);
        chartContainer.setSpacing(true);
        
        // Crear displays de métricas
        cpuDisplay = createMetricDisplay("🖥️ CPU", "0%", "#4F46E5");
        memoryDisplay = createMetricDisplay("💾 RAM", "0%", "#10B981");
        diskDisplay = createMetricDisplay("💽 Disco", "0%", "#F59E0B");
        
        statusDisplay = new Span("📊 Cargando métricas...");
        statusDisplay.getStyle()
            .set("text-align", "center")
            .set("color", "#9CA3AF")
            .set("font-style", "italic")
            .set("padding", "1rem");
        
        chartContainer.add(
            new Span("📈 Métricas en Tiempo Real"),
            cpuDisplay,
            memoryDisplay, 
            diskDisplay,
            statusDisplay
        );
        
        add(chartContainer);
        setupRealTimeAnimation();
    }
    
    private void setupChartStyling() {
        getStyle()
            .set("background", "linear-gradient(135deg, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0.02) 100%)")
            .set("border-radius", "16px")
            .set("padding", "2rem")
            .set("border", "1px solid rgba(255,255,255,0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("min-height", "300px")
            .set("width", "100%");
    }
    
    private Span createMetricDisplay(String label, String value, String color) {
        Span display = new Span();
        display.getElement().setProperty("innerHTML", 
            "<div style='display: flex; justify-content: space-between; align-items: center; padding: 1rem; " +
            "background: rgba(255,255,255,0.05); border-radius: 12px; margin: 0.5rem 0; " +
            "border-left: 4px solid " + color + ";'>" +
            "<span style='font-weight: 600; color: " + color + ";'>" + label + "</span>" +
            "<span style='font-size: 1.5rem; font-weight: 700; color: " + color + ";'>" + value + "</span>" +
            "</div>");
        return display;
    }
    
    private void setupRealTimeAnimation() {
        getElement().executeJs("""
            // Animación de entrada
            this.style.opacity = '0';
            this.style.transform = 'translateY(30px)';
            
            setTimeout(() => {
                this.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
                this.style.opacity = '1';
                this.style.transform = 'translateY(0)';
            }, 200);
        """);
    }
    
    public void updateData(List<SystemMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            statusDisplay.setText("📊 Sin datos disponibles");
            return;
        }
        
        // Tomar la métrica más reciente
        SystemMetric latest = metrics.get(metrics.size() - 1);
        
        // Actualizar displays con animación
        updateMetricDisplay(cpuDisplay, "🖥️ CPU", latest.getCpuUsage(), "#4F46E5");
        updateMetricDisplay(memoryDisplay, "💾 RAM", latest.getMemoryUsage(), "#10B981");
        updateMetricDisplay(diskDisplay, "💽 Disco", latest.getDiskUsage(), "#F59E0B");
        
        statusDisplay.setText("📊 Última actualización: " + 
            java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    private void updateMetricDisplay(Span display, String label, double value, String color) {
        String alertColor = value > 80 ? "#EF4444" : color;
        
        display.getElement().setProperty("innerHTML", 
            "<div style='display: flex; justify-content: space-between; align-items: center; padding: 1rem; " +
            "background: rgba(255,255,255,0.05); border-radius: 12px; margin: 0.5rem 0; " +
            "border-left: 4px solid " + alertColor + "; transition: all 0.3s ease;'>" +
            "<span style='font-weight: 600; color: " + alertColor + ";'>" + label + "</span>" +
            "<span style='font-size: 1.5rem; font-weight: 700; color: " + alertColor + ";'>" + 
            String.format("%.1f%%", value) + "</span>" +
            "</div>");
    }
    
    public void setChartType(String type) {
        this.chartType = type;
        statusDisplay.setText("📊 Tipo de gráfico: " + type + " (Próximamente)");
    }
}
