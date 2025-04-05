package com.monitoring.server.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import com.monitoring.server.data.entity.SystemMetric;

/**
 * Interfaz para el servicio de gestión de métricas del sistema
 */
public interface MetricService {
    
    /**
     * Guarda una nueva métrica en la base de datos
     * @param metric Métrica a guardar
     * @return Métrica guardada
     */
    SystemMetric save(SystemMetric metric);
    
    /**
     * Busca una métrica por su ID
     * @param id ID de la métrica
     * @return Métrica encontrada o null si no existe
     */
    SystemMetric findById(Long id);
    
    /**
     * Obtiene todas las métricas almacenadas
     * @return Lista de métricas
     */
    List<SystemMetric> findAll();
    
    /**
     * Busca métricas por nombre
     * @param metricName Nombre de la métrica
     * @return Lista de métricas con ese nombre
     */
    List<SystemMetric> findByMetricName(String metricName);
    
    /**
     * Busca métricas en un rango de tiempo
     * @param start Fecha de inicio
     * @param end Fecha de fin
     * @return Lista de métricas en ese rango
     */
    List<SystemMetric> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Elimina todas las métricas anteriores a una fecha
     * @param date Fecha límite
     * @return Número de métricas eliminadas
     */
    int deleteOlderThan(LocalDateTime date);
    
    /**
     * Obtiene las métricas actuales del sistema (CPU, memoria, disco)
     * @return Objeto con las métricas actuales
     */
    SystemMetric getCurrentMetrics();
    
    /**
     * Obtiene las métricas más recientes
     * @return Lista de métricas recientes
     */
    List<SystemMetric> getLatestMetrics();
    
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