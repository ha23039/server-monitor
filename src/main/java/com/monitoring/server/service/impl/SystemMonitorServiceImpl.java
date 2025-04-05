package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.monitoring.server.data.entity.AlertConfiguration;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.data.repository.MetricRepository;
import com.monitoring.server.monitoring.collector.SystemMetricCollector;
import com.monitoring.server.service.interfaces.AlertConfigService;
import com.monitoring.server.service.interfaces.SystemMonitorService;

/**
 * Implementación del servicio de monitoreo del sistema
 */
@Service
public class SystemMonitorServiceImpl implements SystemMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(SystemMonitorServiceImpl.class);
    
    @Autowired
    private SystemMetricCollector metricCollector;
    
    @Autowired
    private MetricRepository metricRepository;
    
    @Autowired
    private AlertConfigService alertConfigService;
    
    // Almacenamiento en memoria para acceso rápido a los datos actuales
    private SystemMetric currentMetrics;
    
    // Última vez que se envió una alerta para evitar spam
    private LocalDateTime lastCpuAlertTime;
    private LocalDateTime lastMemoryAlertTime;
    private LocalDateTime lastDiskAlertTime;
    
    /**
     * Tarea programada que se ejecuta cada cierto intervalo para recolectar métricas
     */
    @Scheduled(fixedRateString = "${system.metric.collection.interval:10000}")
    public void collectAndProcessMetrics() {
        try {
            // Obtener las métricas actuales
            SystemMetric metrics = metricCollector.collectMetrics();
            
            // Aplicar reglas de alerta
            checkAlerts(metrics);
            
            // Actualizar métricas actuales
            this.currentMetrics = metrics;
            
            // Guardar en la base de datos
            metricRepository.save(metrics);
            
            logger.debug("Métricas recolectadas: CPU={}%, MEM={}%, Disk={}%", 
                    metrics.getCpuUsage(), metrics.getMemoryUsage(), metrics.getDiskUsage());
        } catch (Exception e) {
            logger.error("Error al recolectar y procesar métricas", e);
        }
    }
    
    /**
     * Verifica las reglas de alerta basadas en las configuraciones
     */
    private void checkAlerts(SystemMetric metrics) {
        AlertConfiguration config = alertConfigService.getCurrentConfig();
        LocalDateTime now = LocalDateTime.now();
        
        // Verificar CPU
        if (metrics.getCpuUsage() > config.getCpuThreshold()) {
            metrics.setCpuAlert(true);
            if (shouldSendAlert(lastCpuAlertTime, config.getAlertInterval())) {
                lastCpuAlertTime = now;
                triggerCpuAlert(metrics.getCpuUsage(), config.getCpuThreshold());
            }
        } else {
            metrics.setCpuAlert(false); 
        }
        
        // Verificar Memoria
        if (metrics.getMemoryUsage() > config.getMemoryThreshold()) {
            metrics.setMemoryAlert(true);
            if (shouldSendAlert(lastMemoryAlertTime, config.getAlertInterval())) {
                lastMemoryAlertTime = now;
                triggerMemoryAlert(metrics.getMemoryUsage(), config.getMemoryThreshold());
            }
        } else {
            metrics.setMemoryAlert(false);
        }
        
        // Verificar Disco
        if (metrics.getDiskUsage() > config.getDiskThreshold()) {
            metrics.setDiskAlert(true);
            if (shouldSendAlert(lastDiskAlertTime, config.getAlertInterval())) {
                lastDiskAlertTime = now;
                triggerDiskAlert(metrics.getDiskUsage(), config.getDiskThreshold());
            }
        } else {
            metrics.setDiskAlert(false); 
        }
    }
    
    /**
     * Determinamos si debemos enviar una alerta basada en el intervalo de tiempo
     */
    private boolean shouldSendAlert(LocalDateTime lastAlertTime, int intervalMinutes) {
        if (lastAlertTime == null) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastAlert = ChronoUnit.MINUTES.between(lastAlertTime, now);
        return minutesSinceLastAlert >= intervalMinutes;
    }
    
    /**
     * Envía una alerta de CPU
     */
    private void triggerCpuAlert(double currentValue, double threshold) {
        logger.warn("ALERTA: Uso de CPU alto: {}% (umbral: {}%)", currentValue, threshold);
    }
    
    /**
     * Envía una alerta de Memoria
     */
    private void triggerMemoryAlert(double currentValue, double threshold) {
        logger.warn("ALERTA: Uso de memoria alto: {}% (umbral: {}%)", currentValue, threshold);
    }
    
    /**
     * Envía una alerta de Disco
     */
    private void triggerDiskAlert(double currentValue, double threshold) {
        logger.warn("ALERTA: Uso de disco alto: {}% (umbral: {}%)", currentValue, threshold);
    }
    
    @Override
    public SystemMetric getCurrentMetrics() {
        if (currentMetrics == null) {
            // Si no hay métricas aún, forzamos recolección
            currentMetrics = metricCollector.collectMetrics();
        }
        return currentMetrics;
    }
    
    @Override
    public List<SystemMetric> getMetricsHistory(String period) {
        LocalDateTime startTime;
        LocalDateTime endTime = LocalDateTime.now();
        
        switch (period.toLowerCase()) {
            case "1h":
                startTime = endTime.minusHours(1);
                break;
            case "24h":
                startTime = endTime.minusHours(24);
                break;
            case "7d":
                startTime = endTime.minusDays(7);
                break;
            case "1m":
                startTime = endTime.minusMonths(1);
                break;
            default:
                startTime = endTime.minusHours(1);
                break;
        }
        
        return metricRepository.findByTimestampBetween(startTime, endTime);
    }
    
    @Override
    public List<SystemMetric> getMetricsWithAlerts() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        List<SystemMetric> recentMetrics = metricRepository.findByTimestampAfter(startTime);
        
        return recentMetrics.stream()
                .filter(m -> m.isCpuAlert() || m.isMemoryAlert() || m.isDiskAlert())
                .collect(Collectors.toList());
    }
}