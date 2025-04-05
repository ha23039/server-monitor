package com.monitoring.server.monitoring.alert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.monitoring.server.data.entity.AlertConfiguration;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.service.interfaces.AlertConfigService;
import com.monitoring.server.service.interfaces.MetricService;

/**
 * Componente que verifica los umbrales para generar alertas periódicamente.
 */
@Component
public class AlertGenerator {

    private static final Logger logger = LoggerFactory.getLogger(AlertGenerator.class);
    
    private final AlertConfigService alertConfigService;
    private final MetricService metricService;
    
    private boolean isGenerating = false;
    
    @Autowired
    public AlertGenerator(AlertConfigService alertConfigService, MetricService metricService) {
        this.alertConfigService = alertConfigService;
        this.metricService = metricService;
    }
    
    /**
     * Inicia la generación de alertas.
     */
    public void startGenerating() {
        logger.info("Iniciando generación de alertas");
        isGenerating = true;
    }
    
    /**
     * Detiene la generación de alertas.
     */
    public void stopGenerating() {
        logger.info("Deteniendo generación de alertas");
        isGenerating = false;
    }
    
    /**
     * Verifica si está generando alertas.
     * @return true si está generando alertas, false de lo contrario
     */
    public boolean isGenerating() {
        return isGenerating;
    }
    
    /**
     * Verifica umbrales y genera alertas si es necesario.
     * La anotación @Scheduled indica que se ejecutará cada 30 segundos.
     */
    @Scheduled(fixedRate = 30000)
    public void checkThresholds() {
        if (!isGenerating) {
            return;
        }
        
        try {
            logger.debug("Verificando umbrales para alertas");
            
            // Obtenemos las métricas más recientes
            SystemMetric latestMetric = metricService.getCurrentMetrics();
            Map<String, Double> metricsMap = new HashMap<>();
            metricsMap.put("CPU", latestMetric.getCpuUsage());
            metricsMap.put("Memory", latestMetric.getMemoryUsage());
            metricsMap.put("Disk", latestMetric.getDiskUsage());
            
            // Obtenemos configuraciones de alertas habilitadas
            List<AlertConfiguration> alertConfigs = alertConfigService.findByEnabled(true);
            
            // Verificamos cada configuración contra la métrica correspondiente
            for (AlertConfiguration config : alertConfigs) {
                String componentName = config.getComponentName();
                
                if (componentName == null) {
                    continue;
                }
                
                double thresholdValue = 0.0;
                
                // Obtenemoss el umbral según el componente
                if ("CPU".equals(componentName)) {
                    thresholdValue = config.getCpuThreshold();
                } else if ("Memory".equals(componentName)) {
                    thresholdValue = config.getMemoryThreshold();
                } else if ("Disk".equals(componentName)) {
                    thresholdValue = config.getDiskThreshold();
                }
                
                // Si no tenemos datos para este componente, continuar
                if (!metricsMap.containsKey(componentName)) {
                    continue;
                }
                
                double currentValue = metricsMap.get(componentName);
                
                // Si el valor supera el umbral, generar alerta
                if (currentValue >= thresholdValue) {
                    generateAlert(componentName, currentValue, thresholdValue);
                }
            }
        } catch (Exception e) {
            logger.error("Error al verificar umbrales para alertas", e);
        }
    }
    
    /**
     * Genera una alerta para un componente.
     * @param componentName nombre del componente
     * @param currentValue valor actual
     * @param thresholdValue valor umbral
     */
    private void generateAlert(String componentName, double currentValue, double thresholdValue) {
        logger.warn("ALERTA: {} ha superado el umbral. Valor actual: {}%, Umbral: {}%",
                componentName, String.format("%.1f", currentValue), String.format("%.1f", thresholdValue));
    }
}