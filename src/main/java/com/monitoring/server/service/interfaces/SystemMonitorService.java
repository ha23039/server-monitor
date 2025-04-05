package com.monitoring.server.service.interfaces;

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
     * Obtiene las métricas que han generado alertas
     * @return Lista de métricas con alertas
     */
    List<SystemMetric> getMetricsWithAlerts();
}