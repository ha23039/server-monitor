package com.monitoring.server.views.components;

import java.util.List;

import com.monitoring.server.data.entity.SystemMetric;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Componente alternativo para mostrar métricas sin usar Charts comerciales
 */
public class MetricChart extends Div {
    
    private final VerticalLayout chartLayout;
    private final Span cpuLabel;
    private final Span memoryLabel;
    private final Span diskLabel;
    
    /**
     * Constructor para crear un gráfico de métricas simple
     */
    public MetricChart() {
        addClassName("metric-chart");
        setWidthFull();
        setHeight("300px");
        
        chartLayout = new VerticalLayout();
        chartLayout.setSpacing(true);
        chartLayout.setPadding(false);
        
        // Etiquetas para mostrar datos
        cpuLabel = new Span("CPU: N/A");
        memoryLabel = new Span("Memoria: N/A");
        diskLabel = new Span("Disco: N/A");
        
        chartLayout.add(
            new Span("Historial de Métricas del Sistema"),
            cpuLabel,
            memoryLabel,
            diskLabel
        );
        
        add(chartLayout);
    }
    
    /**
     * Actualiza el gráfico con nuevos datos
     * @param metrics Lista de métricas para mostrar
     */
    public void updateChart(List<SystemMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            resetLabels();
            return;
        }
        
        // Tomar la última métrica
        SystemMetric latestMetric = metrics.get(metrics.size() - 1);
        
        // Actualizar etiquetas con los últimos valores
        cpuLabel.setText(String.format("CPU: %.2f%%", latestMetric.getCpuUsage()));
        memoryLabel.setText(String.format("Memoria: %.2f%%", latestMetric.getMemoryUsage()));
        diskLabel.setText(String.format("Disco: %.2f%%", latestMetric.getDiskUsage()));
    }
    
    /**
     * Reinicia las etiquetas a valores predeterminados
     */
    private void resetLabels() {
        cpuLabel.setText("CPU: N/A");
        memoryLabel.setText("Memoria: N/A");
        diskLabel.setText("Disco: N/A");
    }
}