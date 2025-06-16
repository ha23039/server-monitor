package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SystemMonitorServiceImpl.class);

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
    public List<SystemMetric> getMetricsByDateRange(LocalDateTime start, LocalDateTime end) {
        logger.info("📅 Obteniendo métricas por rango de fechas: {} - {}", start, end);
        
        try {
            // Validación de parámetros
            if (start == null || end == null) {
                logger.warn("⚠️ Fechas de inicio o fin son nulas, retornando lista vacía");
                return List.of();
            }
            
            if (start.isAfter(end)) {
                logger.warn("⚠️ Fecha de inicio es posterior a fecha de fin, intercambiando");
                LocalDateTime temp = start;
                start = end;
                end = temp;
            }
            
            // Buscar métricas en el repositorio por rango de fechas
            List<SystemMetric> metrics = systemMetricRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
            
            logger.info("✅ Encontradas {} métricas en el rango especificado", metrics.size());
            return metrics;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo métricas por rango de fechas", e);
            return List.of();
        }
    }

    @Override
    public List<SystemMetric> getMetricsWithAlerts() {
        return systemMetricRepository.findMetricsWithAlerts();
    }
    
    @Override
    public List<SystemMetric> getMetricsSince(LocalDateTime since) {
        return systemMetricRepository.findMetricsSince(since);
    }
    
    @Override
    public List<SystemMetric> getAverageMetrics(String period) {
        LocalDateTime startTime = calculateStartTime(period);
        LocalDateTime endTime = LocalDateTime.now();
        
        // Obtener estadísticas promedio
        Object[] averages = systemMetricRepository.getAverageMetricsInPeriod(startTime, endTime);
        
        if (averages != null && averages[0] != null) {
            double avgCpu = (Double) averages[0];
            double avgMemory = (Double) averages[1]; 
            double avgDisk = (Double) averages[2];
            
            SystemMetric avgMetric = new SystemMetric(avgCpu, avgMemory, avgDisk);
            avgMetric.setTimestamp(endTime);
            return List.of(avgMetric);
        }
        
        return List.of(new SystemMetric(0, 0, 0));
    }
    
    // === IMPLEMENTACIONES BÁSICAS PARA MÉTODOS ADICIONALES ===
    
    @Override
    public List<SystemMetric> getResourceUsageMetrics() {
        // Obtener métricas de las últimas 24 horas para análisis de recursos
        return getMetricsHistory("24H");
    }
    
    @Override
    public List<SystemMetric> getPerformanceMetrics() {
        // Obtener métricas de rendimiento (últimas 6 horas)
        return getMetricsHistory("6H");
    }
    
    @Override
    public List<SystemMetric> getAvailabilityMetrics() {
        // Obtener métricas de disponibilidad (última semana)
        return getMetricsHistory("7D");
    }
    
    @Override
    public List<SystemMetric> getSecurityMetrics() {
        // Por ahora retornar métricas con alertas como indicador de seguridad
        return getMetricsWithAlerts();
    }
    
    @Override
    public List<SystemMetric> getCapacityMetrics() {
        // Métricas de capacidad (último mes para tendencias)
        return getMetricsHistory("1M");
    }
    
    @Override
    public List<SystemMetric> getEfficiencyMetrics() {
        // Métricas de eficiencia (últimas 24 horas)
        return getMetricsHistory("24H");
    }
    
    @Override
    public List<SystemMetric> getServiceQualityMetrics() {
        // Métricas de calidad del servicio (últimas 24 horas)
        return getMetricsHistory("24H");
    }
    
    @Override
    public List<SystemMetric> getComplianceMetrics() {
        // Métricas de cumplimiento (último mes)
        return getMetricsHistory("1M");
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
            case "6H" -> now.minusHours(6);
            case "12H" -> now.minusHours(12);
            case "24H" -> now.minusHours(24);
            case "7D" -> now.minusDays(7);
            case "30D", "1M" -> now.minusMonths(1);
            case "90D" -> now.minusDays(90);
            default -> now.minusHours(1); // Default a 1 hora
        };
    }
}