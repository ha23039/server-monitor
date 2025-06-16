package com.monitoring.server.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import com.monitoring.server.data.entity.SystemMetric;

/**
 * Interfaz para el servicio de monitoreo del sistema
 */
public interface SystemMonitorService {
    
    /**
     * Obtiene las métricas actuales del sistema
     * @return Objeto SystemMetric con las métricas actuales
     */
    SystemMetric getCurrentMetrics();
    
    /**
     * Obtiene el historial de métricas para un período determinado
     * @param period Período de tiempo ("1h", "24h", "7d", "1m")
     * @return Lista de métricas del período
     */
    List<SystemMetric> getMetricsHistory(String period);
    
    /**
     * 📅 NUEVO - Obtiene métricas por rango de fechas personalizado
     * @param start Fecha de inicio
     * @param end Fecha de fin
     * @return Lista de métricas en el rango especificado
     */
    List<SystemMetric> getMetricsByDateRange(LocalDateTime start, LocalDateTime end);
    
    /**
     * Obtiene las métricas que han generado alertas
     * @return Lista de métricas con alertas
     */
    List<SystemMetric> getMetricsWithAlerts();

    /**
     * Obtiene métricas desde una fecha específica
     * @param since Fecha de inicio
     * @return Lista de métricas desde esa fecha
     */
    List<SystemMetric> getMetricsSince(LocalDateTime since);
    
    /**
     * Obtiene estadísticas promedio para un período
     * @param period Período de tiempo ("1h", "24h", "7d", "1m")
     * @return Lista de métricas promedio del período
     */
    List<SystemMetric> getAverageMetrics(String period);
    
    /**
     * Obtiene estadísticas de uso de recursos del sistema
     * @return Lista de métricas de uso de recursos
     */
    List<SystemMetric> getResourceUsageMetrics();
    
    /**
     * Obtiene estadísticas de rendimiento del sistema
     * @return Lista de métricas de rendimiento
     */
    List<SystemMetric> getPerformanceMetrics();
    
    /**
     * Obtiene estadísticas de disponibilidad del sistema
     * @return Lista de métricas de disponibilidad
     */
    List<SystemMetric> getAvailabilityMetrics();
    
    /**
     * Obtiene estadísticas de seguridad del sistema
     * @return Lista de métricas de seguridad
     */
    List<SystemMetric> getSecurityMetrics();
    
    /**
     * Obtiene estadísticas de capacidad del sistema
     * @return Lista de métricas de capacidad
     */
    List<SystemMetric> getCapacityMetrics();
    
    /**
     * Obtiene estadísticas de eficiencia del sistema
     * @return Lista de métricas de eficiencia
     */
    List<SystemMetric> getEfficiencyMetrics();
    
    /**
     * Obtiene estadísticas de calidad del servicio
     * @return Lista de métricas de calidad del servicio
     */
    List<SystemMetric> getServiceQualityMetrics();
    
    /**
     * Obtiene estadísticas de cumplimiento normativo
     * @return Lista de métricas de cumplimiento normativo
     */
    List<SystemMetric> getComplianceMetrics();
}