package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monitoring.server.data.entity.AlertConfiguration;
import com.monitoring.server.data.repository.AlertConfigRepository;
import com.monitoring.server.service.interfaces.AlertConfigService;

/**
 * Implementación del servicio de configuración de alertas
 */
@Service
public class AlertConfigServiceImpl implements AlertConfigService {

    private static final Logger logger = LoggerFactory.getLogger(AlertConfigServiceImpl.class);
    
    @Autowired
    private AlertConfigRepository alertConfigRepository;
    
    // Valores por defecto
    private static final double DEFAULT_CPU_THRESHOLD = 80.0;
    private static final double DEFAULT_MEMORY_THRESHOLD = 80.0;
    private static final double DEFAULT_DISK_THRESHOLD = 90.0;
    private static final int DEFAULT_ALERT_INTERVAL = 30;
    
    private static final String[] DEFAULT_COMPONENTS = {
        "CPU", "Memory", "Disk", "Network"
    };
    
    @Override
    public AlertConfiguration getCurrentConfig() {
        List<AlertConfiguration> activeConfigs = alertConfigRepository.findAll().stream()
            .filter(AlertConfiguration::isActive)
            .collect(Collectors.toList());
        
        if (activeConfigs.isEmpty()) {
            // Si no hay configuraciones activas, crear una por defecto
            return createDefaultConfig();
        }
        
        // Ya no desactivamos múltiples configuraciones activas
        // Simplemente devolvemos la primera para mantener compatibilidad con el código existente
        
        return activeConfigs.get(0);
    }
    
    /**
     * Crea una configuración por defecto
     */
    private AlertConfiguration createDefaultConfig() {
        logger.info("Creando configuración de alertas por defecto");
        
        AlertConfiguration defaultConfig = new AlertConfiguration(
                "Default Configuration",
                DEFAULT_CPU_THRESHOLD,
                DEFAULT_MEMORY_THRESHOLD,
                DEFAULT_DISK_THRESHOLD
        );
        defaultConfig.setAlertInterval(DEFAULT_ALERT_INTERVAL);
        defaultConfig.setActive(true);
        defaultConfig.setEnabled(true);
        defaultConfig.setThresholdValue(DEFAULT_CPU_THRESHOLD); // Establecer valor threshold
        
        return alertConfigRepository.save(defaultConfig);
    }
    
    @Override
    public AlertConfiguration saveConfig(AlertConfiguration config) {
        // Asegurar que está habilitado
        config.setEnabled(true);
        
        // Asegurar que el threshold_value tenga un valor
        if (config.getThresholdValue() == null) {
            // Usar uno de los umbrales específicos como valor predeterminado
            if ("CPU".equals(config.getComponentName())) {
                config.setThresholdValue(config.getCpuThreshold());
            } else if ("Memory".equals(config.getComponentName())) {
                config.setThresholdValue(config.getMemoryThreshold());
            } else if ("Disk".equals(config.getComponentName())) {
                config.setThresholdValue(config.getDiskThreshold());
            } else {
                config.setThresholdValue(80.0); // Valor predeterminado
            }
        }
        
        // Activar la configuración automáticamente cuando se guarda desde la UI
        config.setActive(true);
        
        // Actualizar timestamp
        config.setUpdatedAt(LocalDateTime.now());
        
        // Modificamos este comportamiento para permitir múltiples configuraciones activas
        // Si esta configuración es de un tipo específico (CPU, Memory, Disk),
        // podemos desactivar otras del mismo tipo para evitar conflictos
        if (config.isActive() && config.getComponentName() != null) {
            List<AlertConfiguration> sameTypeConfigs = alertConfigRepository.findAll().stream()
                .filter(c -> c.getComponentName() != null && 
                      c.getComponentName().equals(config.getComponentName()) && 
                      c.getId() != config.getId() &&
                      c.isActive())
                .collect(Collectors.toList());
            
            // Desactivar otras configuraciones del mismo tipo
            if (!sameTypeConfigs.isEmpty()) {
                sameTypeConfigs.forEach(c -> {
                    c.setActive(false);
                    alertConfigRepository.save(c);
                    logger.info("Desactivando configuración anterior de {}: {}", 
                             c.getComponentName(), c.getName());
                });
            }
        }
        
        logger.info("Guardando configuración activa para {}: {}",
                 config.getComponentName(), config.getName());
        return alertConfigRepository.save(config);
    }
    
    @Override
    public AlertConfiguration save(AlertConfiguration config) {
        return saveConfig(config);
    }
    
    @Override
    public List<AlertConfiguration> getAllConfigs() {
        return alertConfigRepository.findAll();
    }
    
    @Override
    public List<AlertConfiguration> findByEnabled(boolean isEnabled) {
        return alertConfigRepository.findByIsEnabled(isEnabled);
    }
    
    @Override
    public AlertConfiguration findFirstByComponentName(String componentName) {
        return alertConfigRepository.findFirstByComponentName(componentName).orElse(null);
    }
    
    @Override
    @Transactional
    public boolean updateThreshold(Long configId, Double threshold) {
        Optional<AlertConfiguration> configOpt = alertConfigRepository.findById(configId);
        
        if (configOpt.isPresent()) {
            AlertConfiguration config = configOpt.get();
            
            // Determinar qué umbral actualizar según el componente
            if ("CPU".equals(config.getComponentName())) {
                config.setCpuThreshold(threshold);
            } else if ("Memory".equals(config.getComponentName())) {
                config.setMemoryThreshold(threshold);
            } else if ("Disk".equals(config.getComponentName())) {
                config.setDiskThreshold(threshold);
            }
            
            // Actualizar también el threshold_value
            config.setThresholdValue(threshold);
            
            // Activar la configuración automáticamente cuando se actualiza el umbral
            config.setActive(true);
            
            config.setUpdatedAt(LocalDateTime.now());
            alertConfigRepository.save(config);
            
            logger.info("Umbral actualizado para {}: {}", config.getComponentName(), threshold);
            return true;
        } else {
            logger.warn("No se encontró la configuración con ID: {}", configId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean activateConfig(Long configId) {
        Optional<AlertConfiguration> configOpt = alertConfigRepository.findById(configId);
        
        if (configOpt.isPresent()) {
            AlertConfiguration selectedConfig = configOpt.get();
            
            // Desactivar solo otras configuraciones del mismo tipo
            if (selectedConfig.getComponentName() != null) {
                List<AlertConfiguration> sameTypeConfigs = alertConfigRepository.findAll().stream()
                    .filter(c -> c.getComponentName() != null && 
                          c.getComponentName().equals(selectedConfig.getComponentName()) && 
                          c.getId() != selectedConfig.getId())
                    .collect(Collectors.toList());
                
                sameTypeConfigs.forEach(c -> {
                    c.setActive(false);
                    alertConfigRepository.save(c);
                });
            }
            
            // Activar la configuración seleccionada
            selectedConfig.setActive(true);
            alertConfigRepository.save(selectedConfig);
            
            logger.info("Configuración activada: {}", selectedConfig.getName());
            return true;
        } else {
            logger.warn("No se encontró la configuración con ID: {}", configId);
            return false;
        }
    }
    
    @Override
    @Transactional
    public void initDefaultConfigurations() {
        // Verificar si ya existen configuraciones
        if (alertConfigRepository.count() > 0) {
            logger.info("Ya existen configuraciones, omitiendo inicialización");
            return;
        }
        
        logger.info("Inicializando configuraciones por defecto");
        
        // Crear configuraciones para cada componente
        for (String component : DEFAULT_COMPONENTS) {
            AlertConfiguration config = new AlertConfiguration();
            config.setName(component + " Default Alert");
            config.setComponentName(component);
            config.setEnabled(true);
            
            double thresholdValue = 80.0; // Valor predeterminado
            
            // Establecer umbrales según el componente
            if ("CPU".equals(component)) {
                config.setCpuThreshold(DEFAULT_CPU_THRESHOLD);
                thresholdValue = DEFAULT_CPU_THRESHOLD;
            } else if ("Memory".equals(component)) {
                config.setMemoryThreshold(DEFAULT_MEMORY_THRESHOLD);
                thresholdValue = DEFAULT_MEMORY_THRESHOLD;
            } else if ("Disk".equals(component)) {
                config.setDiskThreshold(DEFAULT_DISK_THRESHOLD);
                thresholdValue = DEFAULT_DISK_THRESHOLD;
            } else {
                // Valor predeterminado para otros componentes
                thresholdValue = 90.0; // 90% es un valor razonable para la mayoría
            }
            
            config.setThresholdValue(thresholdValue);
            config.setAlertInterval(DEFAULT_ALERT_INTERVAL);
            config.setCreatedAt(LocalDateTime.now());
            
            // Ahora activamos todas las configuraciones por defecto
            config.setActive(true);
            
            alertConfigRepository.save(config);
            logger.info("Configuración creada para {}", component);
        }
    }
}