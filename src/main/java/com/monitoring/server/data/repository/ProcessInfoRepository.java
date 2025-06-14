// ProcessInfoRepository.java
package com.monitoring.server.data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.monitoring.server.data.entity.ProcessInfo;

@Repository
public interface ProcessInfoRepository extends JpaRepository<ProcessInfo, Long> {
    
    /**
     * Encuentra procesos en un rango de fechas específico
     */
    List<ProcessInfo> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Encuentra los procesos más pesados por CPU
     */
    List<ProcessInfo> findTop10ByOrderByCpuUsageDesc();
    
    /**
     * Encuentra los procesos más pesados por memoria
     */
    List<ProcessInfo> findTop10ByOrderByMemoryUsageDesc();
    
    /**
     * Encuentra procesos de un usuario específico
     */
    List<ProcessInfo> findByUsernameOrderByTimestampDesc(String username);
    
    /**
     * Encuentra instancias de un proceso específico
     */
    List<ProcessInfo> findByProcessNameContainingIgnoreCaseOrderByTimestampDesc(String processName);
    
    /**
     * Elimina procesos anteriores a una fecha específica (para limpieza)
     */
    @Modifying
    @Query("DELETE FROM ProcessInfo pi WHERE pi.timestamp < :cutoffDate")
    int deleteByTimestampBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Obtiene los procesos únicos más recientes
     */
    @Query("SELECT DISTINCT pi.processName FROM ProcessInfo pi WHERE pi.timestamp >= :since")
    List<String> getUniqueProcessNamesSince(@Param("since") LocalDateTime since);
}