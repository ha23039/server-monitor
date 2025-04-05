package com.monitoring.server.data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monitoring.server.data.entity.SystemMetric;

/**
 * Repositorio para acceder a las métricas del sistema en la base de datos
 */
@Repository
public interface MetricRepository extends JpaRepository<SystemMetric, Long> {
    
    /**
     * Encuentra métricas por nombre
     */
    List<SystemMetric> findByMetricName(String metricName);
    
    /**
     * Encuentra métricas entre dos fechas
     */
    List<SystemMetric> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Encuentra métricas posteriores a una fecha
     */
    List<SystemMetric> findByTimestampAfter(LocalDateTime startTime);
    
    /**
     * Encuentra métricas con alertas
     */
    List<SystemMetric> findByCpuAlertTrueOrMemoryAlertTrueOrDiskAlertTrue();
    
    /**
     * Encuentra métricas con paginación
     */
    Page<SystemMetric> findAll(Pageable pageable);
    
    /**
     * Elimina métricas anteriores a una fecha
     */
    int deleteByTimestampBefore(LocalDateTime date);
}