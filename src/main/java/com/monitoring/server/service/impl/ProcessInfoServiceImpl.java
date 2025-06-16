package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ProcessInfoServiceImpl.class);

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
    public List<ProcessInfo> getHighCpuProcesses(int limit, double minCpu) {
        logger.info("🔥 Obteniendo procesos con alto uso de CPU: límite={}, mínimo={}%", limit, minCpu);
        
        try {
            // Obtener procesos actuales del sistema
            List<ProcessInfo> allProcesses = getCurrentProcesses();
            
            // Filtrar por uso de CPU y ordenar
            List<ProcessInfo> highCpuProcesses = allProcesses.stream()
                .filter(process -> process.getCpuUsage() >= minCpu)
                .sorted((p1, p2) -> Double.compare(p2.getCpuUsage(), p1.getCpuUsage()))
                .limit(limit)
                .toList();
            
            logger.info("✅ Encontrados {} procesos con CPU >= {}%", highCpuProcesses.size(), minCpu);
            return highCpuProcesses;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo procesos con alto CPU", e);
            return List.of();
        }
    }

    @Override
    public List<ProcessInfo> getHighMemoryProcesses(int limit, double minMemory) {
        logger.info("💾 Obteniendo procesos con alto uso de memoria: límite={}, mínimo={}MB", limit, minMemory);
        
        try {
            // Obtener procesos actuales del sistema
            List<ProcessInfo> allProcesses = getCurrentProcesses();
            
            // Filtrar por uso de memoria y ordenar
            List<ProcessInfo> highMemoryProcesses = allProcesses.stream()
                .filter(process -> process.getMemoryUsage() >= minMemory)
                .sorted((p1, p2) -> Double.compare(p2.getMemoryUsage(), p1.getMemoryUsage()))
                .limit(limit)
                .toList();
            
            logger.info("✅ Encontrados {} procesos con memoria >= {}MB", highMemoryProcesses.size(), minMemory);
            return highMemoryProcesses;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo procesos con alta memoria", e);
            return List.of();
        }
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
    
    // === MÉTODOS AUXILIARES ===
    
    /**
     * Obtiene los procesos actuales del sistema
     */
    private List<ProcessInfo> getCurrentProcesses() {
        try {
            // Primero intentar obtener de la BD (últimos 5 minutos)
            List<ProcessInfo> recentProcesses = processInfoRepository.findByTimestampBetweenOrderByTimestampDesc(
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now()
            );
            
            if (!recentProcesses.isEmpty()) {
                return recentProcesses;
            }
            
            // Si no hay datos recientes, obtener snapshot en vivo
            return metricCollectorService.getCurrentProcessesSnapshot(100);
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo procesos actuales", e);
            return List.of();
        }
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