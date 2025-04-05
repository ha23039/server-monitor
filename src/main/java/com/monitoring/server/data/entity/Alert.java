package com.monitoring.server.data.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa una alerta generada en el sistema.
 * Se crea cuando alguna métrica supera los umbrales configurados.
 */
@Entity
@Table(name = "alerts")
public class Alert implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "component_name", nullable = false)
    private String componentName;  // Componente que generó la alerta (CPU, RAM, Disco, etc.)
    
    @Column(name = "current_value", nullable = false)
    private Double currentValue;  // Valor actual que generó la alerta
    
    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue;  // Umbral configurado
    
    @Column(name = "message", nullable = false)
    private String message;  // Mensaje descriptivo de la alerta
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;  // Momento en que se generó la alerta
    
    @Column(name = "status", nullable = false)
    private String status;  // Estado de la alerta (Nueva, Leída, Resuelta)
    
    // Constructores
    public Alert() {
    }
    
    public Alert(String componentName, Double currentValue, Double thresholdValue, 
                String message, LocalDateTime timestamp, String status) {
        this.componentName = componentName;
        this.currentValue = currentValue;
        this.thresholdValue = thresholdValue;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public Double getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(Double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", componentName='" + componentName + '\'' +
                ", currentValue=" + currentValue +
                ", thresholdValue=" + thresholdValue +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}