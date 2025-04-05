package com.monitoring.server.data.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa una base de datos a monitorear en el sistema.
 * Almacena información como nombre, tipo, host, puerto y estado de monitoreo.
 */
@Entity
@Table(name = "monitored_databases")
public class Database implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "type", nullable = false)
    private String type;
    
    @Column(name = "host", nullable = false)
    private String host;
    
    @Column(name = "port", nullable = false)
    private Integer port;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "status")
    private String status; // "Activa", "Inactiva", "En espera"
    
    @Column(name = "monitor_enabled")
    private Boolean monitorEnabled;
    
    // Constructores
    public Database() {
    }
    
    public Database(String name, String type, String host, Integer port, 
                   String username, String password, String status, Boolean monitorEnabled) {
        this.name = name;
        this.type = type;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.status = status;
        this.monitorEnabled = monitorEnabled;
    }
    
    // Getters y Setters
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getMonitorEnabled() {
        return monitorEnabled;
    }

    public void setMonitorEnabled(Boolean monitorEnabled) {
        this.monitorEnabled = monitorEnabled;
    }

    @Override
    public String toString() {
        return "Database{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", status='" + status + '\'' +
                ", monitorEnabled=" + monitorEnabled +
                '}';
    }
}