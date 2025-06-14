// SystemMetricRepository.java
package com.monitoring.server.data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.monitoring.server.data.entity.SystemMetric;

@Repository
public interface SystemMetricRepository extends JpaRepository<SystemMetric, Long> {
    
    /**
     * Encuentra métricas en un rango de fechas específico
     */
    List<SystemMetric> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Encuentra las últimas N métricas
     */
    List<SystemMetric> findTop100ByOrderByTimestampDesc();
    
    /**
     * Encuentra métricas que tienen alertas activas
     */
    @Query("SELECT sm FROM SystemMetric sm WHERE sm.cpuAlert = true OR sm.memoryAlert = true OR sm.diskAlert = true ORDER BY sm.timestamp DESC")
    List<SystemMetric> findMetricsWithAlerts();
    
    /**
     * Encuentra métricas de las últimas N horas
     */
    @Query("SELECT sm FROM SystemMetric sm WHERE sm.timestamp >= :since ORDER BY sm.timestamp DESC")
    List<SystemMetric> findMetricsSince(@Param("since") LocalDateTime since);
    
    /**
     * Elimina métricas anteriores a una fecha específica (para limpieza)
     */
    @Modifying
    @Query("DELETE FROM SystemMetric sm WHERE sm.timestamp < :cutoffDate")
    int deleteByTimestampBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Cuenta métricas en un período específico
     */
    @Query("SELECT COUNT(sm) FROM SystemMetric sm WHERE sm.timestamp BETWEEN :start AND :end")
    long countMetricsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Obtiene estadísticas promedio para un período
     */
    @Query("SELECT AVG(sm.cpuUsage), AVG(sm.memoryUsage), AVG(sm.diskUsage) FROM SystemMetric sm WHERE sm.timestamp BETWEEN :start AND :end")
    Object[] getAverageMetricsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
