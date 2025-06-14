package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.data.repository.ProcessInfoRepository;
import com.monitoring.server.service.interfaces.ProcessInfoService;

/**
 * Implementación mejorada del servicio de información de procesos
 * Ahora funciona con datos reales capturados por MetricCollectorService
 */
@Service
public class ProcessInfoServiceImpl implements ProcessInfoService {

    @Autowired
    private ProcessInfoRepository processInfoRepository;
    
    @Autowired
    private MetricCollectorService metricCollectorService;

    @Override
    public List<ProcessInfo> getHeavyProcesses(int limit, String sortBy) {
        // Intentar obtener desde BD primero
        List<ProcessInfo> processes = switch (sortBy.toUpperCase()) {
            case "CPU" -> processInfoRepository.findTop10ByOrderByCpuUsageDesc();
            case "MEMORIA", "MEMORY" -> processInfoRepository.findTop10ByOrderByMemoryUsageDesc();
            default -> processInfoRepository.findTop10ByOrderByCpuUsageDesc();
        };
        
        // Si no hay datos recientes en BD, obtener snapshot en vivo
        if (processes.isEmpty()) {
            processes = metricCollectorService.getCurrentProcessesSnapshot(limit);
        }
        
        // Limitar a la cantidad solicitada
        return processes.stream().limit(limit).toList();
    }

    @Override
    public List<ProcessInfo> getProcessHistory(LocalDateTime startTime, LocalDateTime endTime) {
        return processInfoRepository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime);
    }

    @Override
    public ProcessInfo getProcessDetail(String processId) {
        // Buscar el proceso más reciente con ese ID
        List<ProcessInfo> processes = processInfoRepository.findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime.now().minusMinutes(5), 
            LocalDateTime.now()
        );
        
        return processes.stream()
            .filter(p -> processId.equals(p.getProcessId()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Obtiene procesos de un usuario específico
     */
    public List<ProcessInfo> getProcessesByUser(String username) {
        return processInfoRepository.findByUsernameOrderByTimestampDesc(username);
    }
    
    /**
     * Busca procesos por nombre
     */
    public List<ProcessInfo> searchProcessesByName(String processName) {
        return processInfoRepository.findByProcessNameContainingIgnoreCaseOrderByTimestampDesc(processName);
    }
    
    /**
     * Obtiene los nombres únicos de procesos recientes
     */
    public List<String> getUniqueProcessNames() {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        return processInfoRepository.getUniqueProcessNamesSince(since);
    }
}