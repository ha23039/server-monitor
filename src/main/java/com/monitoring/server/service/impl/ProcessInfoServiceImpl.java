package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.data.repository.ProcessInfoRepository;
import com.monitoring.server.monitoring.collector.ProcessInfoCollector;
import com.monitoring.server.service.interfaces.ProcessInfoService;

/**
 * Implementación del servicio para obtener información de procesos del sistema
 */
@Service
public class ProcessInfoServiceImpl implements ProcessInfoService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInfoServiceImpl.class);
    
    @Autowired
    private ProcessInfoCollector processInfoCollector;
    
    @Autowired
    private ProcessInfoRepository processInfoRepository;
    
    // Almacenamiento en memoria para acceso rápido
    private List<ProcessInfo> latestProcessInfo = new ArrayList<>();
    
    /**
     * Tarea programada para recolectar información de procesos
     */
    @Scheduled(fixedRateString = "${system.process.collection.interval:30000}")
    public void collectProcessInfo() {
        try {
            List<ProcessInfo> processes = processInfoCollector.collectProcesses();
            
            // Actualizar lista en memoria
            this.latestProcessInfo = processes;
            
            // Guardar en base de datos solo los más pesados para no sobrecargar
            List<ProcessInfo> heavyProcesses = processes.stream()
                    .sorted(Comparator.comparingDouble(ProcessInfo::getCpuUsage).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            
            processInfoRepository.saveAll(heavyProcesses);
            
            logger.debug("Procesos recolectados: {}", heavyProcesses.size());
        } catch (Exception e) {
            logger.error("Error al recolectar información de procesos", e);
        }
    }
    
    @Override
    public List<ProcessInfo> getHeavyProcesses(int limit, String sortBy) {
        // Si no hay datos en memoria, intentar recolectarlos
        if (latestProcessInfo.isEmpty()) {
            collectProcessInfo();
        }
        
        // Ordenar según el criterio solicitado
        Comparator<ProcessInfo> comparator;
        switch (sortBy.toLowerCase()) {
            case "memoria":
                comparator = Comparator.comparingDouble(ProcessInfo::getMemoryUsage).reversed();
                break;
            case "disco":
                comparator = Comparator.comparingDouble(ProcessInfo::getDiskUsage).reversed();
                break;
            case "cpu":
            default:
                comparator = Comparator.comparingDouble(ProcessInfo::getCpuUsage).reversed();
                break;
        }
        
        return latestProcessInfo.stream()
                .sorted(comparator)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProcessInfo> getProcessHistory(LocalDateTime startTime, LocalDateTime endTime) {
        return processInfoRepository.findByTimestampBetween(startTime, endTime);
    }
    
    @Override
    public ProcessInfo getProcessDetail(String processId) {
        return latestProcessInfo.stream()
                .filter(p -> p.getProcessId().equals(processId))
                .findFirst()
                .orElse(null);
    }
}