package com.monitoring.server.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import com.monitoring.server.data.entity.ProcessInfo;

/**
 * Interfaz para el servicio de información de procesos
 */
public interface ProcessInfoService {
    
    /**
     * Obtiene los procesos más pesados del sistema
     * @param limit Número máximo de procesos a obtener
     * @param sortBy Criterio de ordenación ("CPU", "Memoria", "Disco")
     * @return Lista de procesos ordenados
     */
    List<ProcessInfo> getHeavyProcesses(int limit, String sortBy);
    
    /**
     * Obtiene el historial de procesos para un período determinado
     * @param startTime Fecha y hora de inicio del período
     * @param endTime Fecha y hora de fin del período
     * @return Lista de procesos del período
     */
    List<ProcessInfo> getProcessHistory(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Obtiene los detalles de un proceso específico
     * @param processId ID del proceso
     * @return Objeto ProcessInfo con los detalles del proceso
     */
    ProcessInfo getProcessDetail(String processId);
}