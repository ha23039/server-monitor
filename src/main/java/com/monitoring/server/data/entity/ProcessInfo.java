package com.monitoring.server.data.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa información de un proceso del sistema
 */
@Entity
@Table(name = "process_info")
public class ProcessInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "process_id")
    private String processId;
    
    @Column(name = "process_name", nullable = false)
    private String processName;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "cpu_usage", nullable = false)
    private double cpuUsage;
    
    @Column(name = "memory_usage", nullable = false)
    private double memoryUsage;
    
    @Column(name = "disk_usage")
    private double diskUsage;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    public ProcessInfo() {
        // Constructor por defecto necesario para JPA
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructor para crear información de proceso con los valores principales
     */
    public ProcessInfo(String processId, String processName, double cpuUsage, double memoryUsage) {
        this.processId = processId;
        this.processName = processName;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters y setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ProcessInfo{" +
                "id=" + id +
                ", processId='" + processId + '\'' +
                ", processName='" + processName + '\'' +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                ", cpuUsage=" + cpuUsage +
                ", memoryUsage=" + memoryUsage +
                ", diskUsage=" + diskUsage +
                ", timestamp=" + timestamp +
                '}';
    }
}