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
 * Entidad unificada que representa métricas del sistema.
 * Soporta tanto métricas genéricas (metricName/value) para bases de datos
 * como métricas específicas (cpuUsage, memoryUsage, diskUsage) para el sistema.
 */
@Entity
@Table(name = "system_metrics")
public class SystemMetric implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Campos para métricas genéricas (bases de datos)
    @Column(name = "metric_name")
    private String metricName;
    
    @Column(name = "value")
    private Double value;
    
    @Column(name = "unit")
    private String unit;
    
    // Campos para métricas del sistema operativo
    @Column(name = "cpu_usage")
    private double cpuUsage;
    
    @Column(name = "memory_usage")
    private double memoryUsage;
    
    @Column(name = "disk_usage")
    private double diskUsage;
    
    @Column(name = "cpu_alert")
    private boolean cpuAlert;
    
    @Column(name = "memory_alert")
    private boolean memoryAlert;
    
    @Column(name = "disk_alert")
    private boolean diskAlert;
    
    // Campo común
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    // Constructores
    
    public SystemMetric() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructor para métricas genéricas (bases de datos)
     */
    public SystemMetric(String metricName, Double value, String unit, LocalDateTime timestamp) {
        this.metricName = metricName;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }
    
    /**
     * Constructor para métricas del sistema operativo
     */
    public SystemMetric(double cpuUsage, double memoryUsage, double diskUsage) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMetricName() {
        return metricName;
    }
    
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
    
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public boolean isCpuAlert() {
        return cpuAlert;
    }

    public void setCpuAlert(boolean cpuAlert) {
        this.cpuAlert = cpuAlert;
    }

    public boolean isMemoryAlert() {
        return memoryAlert;
    }

    public void setMemoryAlert(boolean memoryAlert) {
        this.memoryAlert = memoryAlert;
    }

    public boolean isDiskAlert() {
        return diskAlert;
    }

    public void setDiskAlert(boolean diskAlert) {
        this.diskAlert = diskAlert;
    }
    
    @Override
    public String toString() {
        if (metricName != null) {
            // Para métricas genéricas
            return "SystemMetric{" +
                    "id=" + id +
                    ", metricName='" + metricName + '\'' +
                    ", value=" + value +
                    ", unit='" + unit + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        } else {
            // Para métricas del sistema
            return "SystemMetric{" +
                    "id=" + id +
                    ", cpuUsage=" + cpuUsage +
                    ", memoryUsage=" + memoryUsage +
                    ", diskUsage=" + diskUsage +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}