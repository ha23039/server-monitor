package com.monitoring.server.service.interfaces;

import java.util.List;

import com.monitoring.server.data.entity.AlertConfiguration;

/**
 * Interfaz para el servicio de configuración de alertas
 */
public interface AlertConfigService {
    
    /**
     * Obtiene la configuración de alertas actual
     * @return Configuración de alertas activa
     */
    AlertConfiguration getCurrentConfig();
    
    /**
     * Guarda una nueva configuración de alertas
     * @param config Nueva configuración
     * @return Configuración guardada
     */
    AlertConfiguration saveConfig(AlertConfiguration config);
    
    /**
     * Guarda una configuración de alertas
     * @param config Configuración a guardar
     * @return Configuración guardada
     */
    AlertConfiguration save(AlertConfiguration config);
    
    /**
     * Obtiene todas las configuraciones disponibles
     * @return Lista de configuraciones
     */
    List<AlertConfiguration> getAllConfigs();
    
    /**
     * Encuentra configuraciones por estado de habilitación
     * @param isEnabled Estado de habilitación
     * @return Lista de configuraciones
     */
    List<AlertConfiguration> findByEnabled(boolean isEnabled);
    
    /**
     * Encuentra una configuración por nombre de componente
     * @param componentName Nombre del componente
     * @return Configuración o null si no existe
     */
    AlertConfiguration findFirstByComponentName(String componentName);
    
    /**
     * Actualiza el umbral de una configuración
     * @param configId ID de la configuración
     * @param threshold Nuevo valor de umbral
     * @return true si se actualizó correctamente
     */
    boolean updateThreshold(Long configId, Double threshold);
    
    /**
     * Activa una configuración específica
     * @param configId ID de la configuración a activar
     * @return true si se activó correctamente
     */
    boolean activateConfig(Long configId);
    
    /**
     * Inicializamos las configuraciones por defecto
     */
    void initDefaultConfigurations();
}