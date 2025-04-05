package com.monitoring.server.views.components;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * Componente personalizado para mostrar una métrica del sistema con barra de progreso
 */
public class MetricProgressBar extends VerticalLayout {

    private final H3 titleLabel;
    private final Span valueLabel;
    private final ProgressBar progressBar;
    private final Span statusLabel;
    
    private double value = 0;
    private final double threshold;
    private boolean isAlert = false;
    
    /**
     * Constructor para crear una barra de progreso para una métrica
     * @param title Título de la métrica
     * @param initialValue Valor inicial
     * @param threshold Umbral para considerar alerta
     */
    public MetricProgressBar(String title, double initialValue, double threshold) {
        this.threshold = threshold;
        
        // Configurar el layout
        setPadding(true);
        setSpacing(false);
        addClassName("metric-progress-bar");
        getStyle().set("border-radius", "var(--lumo-border-radius)");
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("box-shadow", "var(--lumo-box-shadow-xs)");
        
        // Crear componentes
        titleLabel = new H3(title);
        titleLabel.getStyle().set("margin-top", "0");
        titleLabel.getStyle().set("margin-bottom", "0.5em");
        
        valueLabel = new Span("0%");
        valueLabel.getStyle().set("font-size", "2em");
        valueLabel.getStyle().set("font-weight", "bold");
        
        progressBar = new ProgressBar();
        progressBar.setMin(0);
        progressBar.setMax(100);
        progressBar.setValue(initialValue);
        progressBar.setWidth("100%");
        
        statusLabel = new Span("Óptimo");
        statusLabel.getStyle().set("color", "var(--lumo-success-color)");
        
        // Layout para el valor y estado
        HorizontalLayout statusLayout = new HorizontalLayout(valueLabel, statusLabel);
        statusLayout.setWidthFull();
        statusLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        statusLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        
        // Añadir componentes
        add(titleLabel, statusLayout, progressBar);
        
        // Establecer valor inicial
        setValue(initialValue);
    }
    
    /**
     * Establece el valor actual de la métrica
     * @param newValue Nuevo valor
     */
    public void setValue(double newValue) {
        this.value = newValue;
        
        // Actualizar etiqueta de valor
        valueLabel.setText(String.format("%.0f%%", newValue));
        
        // Actualizar barra de progreso
        progressBar.setValue(newValue);
        
        // Determinar color según el umbral
        updateProgressBarColor();
        
        // Actualizar estado
        updateStatus();
    }
    
    /**
     * Establece el estado de alerta
     * @param alert true si hay alerta, false en caso contrario
     */
    public void setAlert(boolean alert) {
        this.isAlert = alert;
        updateStatus();
        updateProgressBarColor();
    }
    
    /**
     * Actualiza el color de la barra de progreso según el valor y el umbral
     */
    private void updateProgressBarColor() {
        String color;
        
        if (isAlert) {
            color = "var(--lumo-error-color)";
        } else if (value >= threshold * 0.8) {
            color = "var(--lumo-warning-color)";
        } else {
            color = "var(--lumo-success-color)";
        }
        
        // Aplicar color a la barra
        progressBar.getStyle().set("--lumo-primary-color", color);
    }
    
    /**
     * Actualiza la etiqueta de estado según el valor actual
     */
    private void updateStatus() {
        String status;
        String color;
        
        if (isAlert) {
            status = "Crítico";
            color = "var(--lumo-error-color)";
        } else if (value >= threshold * 0.8) {
            status = "Advertencia";
            color = "var(--lumo-warning-color)";
        } else {
            status = "Óptimo";
            color = "var(--lumo-success-color)";
        }
        
        statusLabel.setText(status);
        statusLabel.getStyle().set("color", color);
    }
}