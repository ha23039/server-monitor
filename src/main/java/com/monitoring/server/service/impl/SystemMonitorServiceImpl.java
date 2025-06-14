package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.data.repository.SystemMetricRepository;
import com.monitoring.server.service.interfaces.SystemMonitorService;

/**
 * Implementación mejorada del servicio de monitoreo del sistema
 * Ahora funciona con datos reales capturados por MetricCollectorService
 */
@Service
public class SystemMonitorServiceImpl implements SystemMonitorService {

    @Autowired
    private SystemMetricRepository systemMetricRepository;
    
    @Autowired
    private MetricCollectorService metricCollectorService;

    @Override
    public SystemMetric getCurrentMetrics() {
        // Intentar obtener la métrica más reciente de la BD
        List<SystemMetric> recentMetrics = systemMetricRepository.findTop100ByOrderByTimestampDesc();
        
        if (!recentMetrics.isEmpty()) {
            return recentMetrics.get(0);
        }
        
        // Si no hay datos en BD, obtener snapshot en vivo
        return metricCollectorService.getCurrentMetricsSnapshot();
    }

    @Override
    public List<SystemMetric> getMetricsHistory(String period) {
        LocalDateTime startTime = calculateStartTime(period);
        LocalDateTime endTime = LocalDateTime.now();
        
        return systemMetricRepository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime);
    }

    @Override
    public List<SystemMetric> getMetricsWithAlerts() {
        return systemMetricRepository.findMetricsWithAlerts();
    }
    
    /**
     * Obtiene métricas desde una fecha específica
     */
    public List<SystemMetric> getMetricsSince(LocalDateTime since) {
        return systemMetricRepository.findMetricsSince(since);
    }
    
    /**
     * Obtiene estadísticas promedio para un período
     */
    public SystemMetric getAverageMetricsInPeriod(LocalDateTime start, LocalDateTime end) {
        Object[] averages = systemMetricRepository.getAverageMetricsInPeriod(start, end);
        
        if (averages != null && averages[0] != null) {
            double avgCpu = (Double) averages[0];
            double avgMemory = (Double) averages[1]; 
            double avgDisk = (Double) averages[2];
            
            SystemMetric avgMetric = new SystemMetric(avgCpu, avgMemory, avgDisk);
            avgMetric.setTimestamp(end);
            return avgMetric;
        }
        
        return new SystemMetric(0, 0, 0);
    }

    /**
     * Calcula la fecha de inicio según el período solicitado
     */
    private LocalDateTime calculateStartTime(String period) {
        LocalDateTime now = LocalDateTime.now();
        
        return switch (period.toUpperCase()) {
            case "1H" -> now.minusHours(1);
            case "24H" -> now.minusHours(24);
            case "7D" -> now.minusDays(7);
            case "1M" -> now.minusMonths(1);
            default -> now.minusHours(1); // Default a 1 hora
        };
    }
}