package com.monitoring.server.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monitoring.server.data.entity.AlertConfiguration;

/**
 * Repositorio para acceder a las configuraciones de alertas en la base de datos
 */
@Repository
public interface AlertConfigRepository extends JpaRepository<AlertConfiguration, Long> {
    
    /**
     * Encuentra la configuración activa
     */
    Optional<AlertConfiguration> findByIsActiveTrue();
    
    /**
     * Encuentra configuraciones por estado de habilitación
     */
    List<AlertConfiguration> findByIsEnabled(boolean isEnabled);
    
    /**
     * Encuentra configuración por nombre
     */
    Optional<AlertConfiguration> findByName(String name);
    
    /**
     * Encuentra configuración por nombre de componente
     */
    Optional<AlertConfiguration> findFirstByComponentName(String componentName);
}