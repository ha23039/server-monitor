package com.monitoring.server.data.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "alert_configurations")
public class AlertConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "component_name")
    private String componentName;
    
    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue = 80.0;
    
    @Column(name = "cpu_threshold", nullable = false)
    private double cpuThreshold = 80.0;
    
    @Column(name = "memory_threshold", nullable = false)
    private double memoryThreshold = 80.0;
    
    @Column(name = "disk_threshold", nullable = false)
    private double diskThreshold = 90.0;
    
    @Column(name = "alert_interval", nullable = false)
    private int alertInterval = 30;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;
    
    @Column(name = "is_enabled", nullable = false, columnDefinition = "boolean DEFAULT true")
    private boolean isEnabled = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public AlertConfiguration() {
        this.createdAt = LocalDateTime.now();
        this.alertInterval = 30;
        this.isEnabled = true;
        this.isActive = false;
        this.thresholdValue = 80.0;
    }
    
    public AlertConfiguration(String name, double cpuThreshold, double memoryThreshold, double diskThreshold) {
        this.name = name;
        this.cpuThreshold = cpuThreshold;
        this.memoryThreshold = memoryThreshold;
        this.diskThreshold = diskThreshold;
        this.thresholdValue = cpuThreshold; // Usamos cpuThreshold como valor por defecto
        this.alertInterval = 30;
        this.isActive = true;
        this.isEnabled = true;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        // Asegurar que los campos críticos tengan valores
        if (this.name == null) {
            this.name = "Default Configuration";
        }
        
        if (this.componentName == null) {
            this.componentName = "System";
        }
        
        // Establecemos valores por defecto para umbrales
        if (this.cpuThreshold <= 0) {
            this.cpuThreshold = 80.0;
        }
        
        if (this.memoryThreshold <= 0) {
            this.memoryThreshold = 80.0;
        }
        
        if (this.diskThreshold <= 0) {
            this.diskThreshold = 90.0;
        }
        
        // Aseguramos que thresholdValue tenga un valor predeterminado
        if (this.thresholdValue == null) {
            this.thresholdValue = 80.0;
        }
        
        if (this.alertInterval <= 0) {
            this.alertInterval = 30;
        }
        
        // Forzar habilitación
        this.isEnabled = true;
        
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.isEnabled = true; // Aseguramos que siempre esté habilitado durante updates
    }

    // Getters y setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getComponentName() {
        return componentName;
    }
    
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    public Double getThresholdValue() {
        return thresholdValue;
    }
    
    public void setThresholdValue(Double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public double getCpuThreshold() {
        return cpuThreshold;
    }

    public void setCpuThreshold(double cpuThreshold) {
        this.cpuThreshold = cpuThreshold;
    }

    public double getMemoryThreshold() {
        return memoryThreshold;
    }

    public void setMemoryThreshold(double memoryThreshold) {
        this.memoryThreshold = memoryThreshold;
    }

    public double getDiskThreshold() {
        return diskThreshold;
    }

    public void setDiskThreshold(double diskThreshold) {
        this.diskThreshold = diskThreshold;
    }

    public int getAlertInterval() {
        return alertInterval;
    }

    public void setAlertInterval(int alertInterval) {
        this.alertInterval = alertInterval;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "AlertConfiguration{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", componentName='" + componentName + '\'' +
                ", thresholdValue=" + thresholdValue +
                ", cpuThreshold=" + cpuThreshold +
                ", memoryThreshold=" + memoryThreshold +
                ", diskThreshold=" + diskThreshold +
                ", alertInterval=" + alertInterval +
                ", isActive=" + isActive +
                ", isEnabled=" + isEnabled +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}