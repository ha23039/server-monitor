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
     * 🔥 NUEVO - Obtener procesos con alto uso de CPU
     * @param limit Número máximo de procesos a retornar
     * @param minCpu Porcentaje mínimo de CPU requerido
     * @return Lista de procesos que exceden el umbral de CPU
     */
    List<ProcessInfo> getHighCpuProcesses(int limit, double minCpu);

    /**
     * 💾 NUEVO - Obtener procesos con alto uso de memoria
     * @param limit Número máximo de procesos a retornar
     * @param minMemory Cantidad mínima de memoria en MB requerida
     * @return Lista de procesos que exceden el umbral de memoria
     */
    List<ProcessInfo> getHighMemoryProcesses(int limit, double minMemory);
    
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