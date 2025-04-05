package com.monitoring.server.data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monitoring.server.data.entity.ProcessInfo;

/**
 * Repositorio para acceder a la información de procesos en la base de datos
 */
@Repository
public interface ProcessInfoRepository extends JpaRepository<ProcessInfo, Long> {
    
    /**
     * Encuentra procesos entre dos fechas
     */
    List<ProcessInfo> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Encuentra procesos por nombre
     */
    List<ProcessInfo> findByProcessNameContaining(String processName);
    
    /**
     * Encuentra procesos por nombre de usuario
     * Nota: El campo en la entidad es 'username', no 'userName'
     */
    List<ProcessInfo> findByUsername(String username);
    
    /**
     * Encuentra procesos por ID de proceso
     */
    List<ProcessInfo> findByProcessId(String processId);
}