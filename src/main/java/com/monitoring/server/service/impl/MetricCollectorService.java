package com.monitoring.server.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.data.repository.ProcessInfoRepository;
import com.monitoring.server.data.repository.SystemMetricRepository;
import com.monitoring.server.service.interfaces.AlertConfigService;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

/**
 * Servicio para captura automática de métricas del sistema usando OSHI
 * Ejecuta tareas programadas con CRON para recolectar datos en tiempo real
 */
@Service
public class MetricCollectorService {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollectorService.class);
    
    @Autowired
    private SystemMetricRepository systemMetricRepository;
    
    @Autowired
    private ProcessInfoRepository processInfoRepository;
    
    @Autowired
    private AlertConfigService alertConfigService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // OSHI components para obtener métricas del sistema
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem operatingSystem;
    private final CentralProcessor processor;
    private final GlobalMemory memory;
    
    // Para cálculo de CPU usage
    private long[] prevTicks;
    
    public MetricCollectorService() {
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.operatingSystem = systemInfo.getOperatingSystem();
        this.processor = hardware.getProcessor();
        this.memory = hardware.getMemory();
        this.prevTicks = processor.getSystemCpuLoadTicks();
        
        logger.info("🚀 MetricCollectorService inicializado con OSHI");
        logger.info("💻 Sistema: {} {}", operatingSystem.getFamily(), operatingSystem.getVersionInfo());
        logger.info("🔧 CPU: {} cores", processor.getLogicalProcessorCount());
        logger.info("💾 RAM Total: {} GB", memory.getTotal() / (1024 * 1024 * 1024));
    }
    
    /**
     * CRON Job principal: Ejecuta cada 5 segundos
     * Captura métricas del sistema y las almacena en BD
     */
    @Scheduled(fixedRate = 5000) // Cada 5 segundos
    @Transactional
    public void collectSystemMetrics() {
        try {
            SystemMetric metric = gatherCurrentSystemMetrics();
            
            // Guardar en base de datos
            systemMetricRepository.save(metric);
            
            // Enviar por WebSocket para actualización en tiempo real
            messagingTemplate.convertAndSend("/topic/metrics", metric);
            
            logger.debug("📊 Métricas recolectadas: CPU={}%, RAM={}%, Disk={}%", 
                        metric.getCpuUsage(), metric.getMemoryUsage(), metric.getDiskUsage());
            
        } catch (Exception e) {
            logger.error("❌ Error recolectando métricas del sistema", e);
        }
    }
    
    /**
     * CRON Job para procesos pesados: Ejecuta cada 10 segundos
     * Captura los procesos que más recursos consumen
     */
    @Scheduled(fixedRate = 10000) // Cada 10 segundos
    @Transactional
    public void collectHeavyProcesses() {
        try {
            List<ProcessInfo> heavyProcesses = gatherHeavyProcesses(10);
            
            // Guardar procesos en BD
            processInfoRepository.saveAll(heavyProcesses);
            
            // Enviar por WebSocket
            messagingTemplate.convertAndSend("/topic/processes", heavyProcesses);
            
            logger.debug("🔄 {} procesos pesados recolectados", heavyProcesses.size());
            
        } catch (Exception e) {
            logger.error("❌ Error recolectando procesos pesados", e);
        }
    }
    
    /**
     * CRON Job de limpieza: Ejecuta cada hora
     * Limpia datos antiguos para evitar que la BD crezca infinitamente
     */
    @Scheduled(cron = "0 0 */1 * * *") // Cada hora
    @Transactional
    public void cleanupOldData() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // Mantener 30 días
            
            int deletedMetrics = systemMetricRepository.deleteByTimestampBefore(cutoffDate);
            int deletedProcesses = processInfoRepository.deleteByTimestampBefore(cutoffDate);
            
            logger.info("🧹 Limpieza completada: {} métricas y {} procesos eliminados", 
                       deletedMetrics, deletedProcesses);
            
        } catch (Exception e) {
            logger.error("❌ Error en limpieza de datos antiguos", e);
        }
    }
    
    /**
     * Recolecta métricas actuales del sistema usando OSHI
     */
    private SystemMetric gatherCurrentSystemMetrics() {
        // CPU Usage
        double cpuUsage = getCpuUsage();
        
        // Memory Usage
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        double memoryUsage = ((totalMemory - availableMemory) * 100.0) / totalMemory;
        
        // Disk Usage (simplificado - primer sistema de archivos)
        double diskUsage = getDiskUsage();
        
        // Crear métrica
        SystemMetric metric = new SystemMetric(cpuUsage, memoryUsage, diskUsage);
        
        // Evaluar alertas usando configuración actual
        evaluateAlerts(metric);
        
        return metric;
    }
    
    /**
     * Calcula el uso actual de CPU
     */
    private double getCpuUsage() {
        try {
            long[] currentTicks = processor.getSystemCpuLoadTicks();
            double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100.0;
            prevTicks = currentTicks;
            
            // Validar que el valor esté en rango válido
            if (cpuUsage < 0) cpuUsage = 0;
            if (cpuUsage > 100) cpuUsage = 100;
            
            return cpuUsage;
        } catch (Exception e) {
            logger.warn("⚠️ Error calculando CPU usage, usando valor por defecto", e);
            return 0.0;
        }
    }
    
    /**
     * Calcula el uso actual de disco
     */
    private double getDiskUsage() {
        try {
            var fileStores = operatingSystem.getFileSystem().getFileStores();
            
            if (!fileStores.isEmpty()) {
                var primaryStore = fileStores.get(0);
                long totalSpace = primaryStore.getTotalSpace();
                long usableSpace = primaryStore.getUsableSpace();
                
                if (totalSpace > 0) {
                    return ((totalSpace - usableSpace) * 100.0) / totalSpace;
                }
            }
            
            return 0.0;
        } catch (Exception e) {
            logger.warn("⚠️ Error calculando Disk usage, usando valor por defecto", e);
            return 0.0;
        }
    }
    
    /**
     * Recolecta los procesos que más recursos consumen
     */
    private List<ProcessInfo> gatherHeavyProcesses(int limit) {
        List<ProcessInfo> processInfos = new ArrayList<>();
        
        try {
            List<OSProcess> processes = operatingSystem.getProcesses(
                OperatingSystem.ProcessFiltering.ALL_PROCESSES, 
                OperatingSystem.ProcessSorting.CPU_DESC, 
                limit
            );
            
            for (OSProcess process : processes) {
                ProcessInfo processInfo = new ProcessInfo();
                processInfo.setProcessId(String.valueOf(process.getProcessID()));
                processInfo.setProcessName(process.getName());
                processInfo.setUsername(process.getUser());
                processInfo.setStatus(process.getState().name());
                processInfo.setCpuUsage(process.getProcessCpuLoadCumulative() * 100.0);
                
                // Calcular memoria como porcentaje del total del sistema
                long processMemory = process.getResidentSetSize();
                double memoryPercentage = (processMemory * 100.0) / memory.getTotal();
                processInfo.setMemoryUsage(memoryPercentage);
                
                // Disk I/O (simplificado)
                processInfo.setDiskUsage(process.getBytesRead() + process.getBytesWritten());
                
                processInfo.setTimestamp(LocalDateTime.now());
                processInfos.add(processInfo);
            }
            
        } catch (Exception e) {
            logger.error("❌ Error recolectando procesos pesados", e);
        }
        
        return processInfos;
    }
    
    /**
     * Evalúa si las métricas actuales generan alertas
     */
    private void evaluateAlerts(SystemMetric metric) {
        try {
            var config = alertConfigService.getCurrentConfig();
            
            boolean cpuAlert = metric.getCpuUsage() > config.getCpuThreshold();
            boolean memoryAlert = metric.getMemoryUsage() > config.getMemoryThreshold();
            boolean diskAlert = metric.getDiskUsage() > config.getDiskThreshold();
            
            metric.setCpuAlert(cpuAlert);
            metric.setMemoryAlert(memoryAlert);
            metric.setDiskAlert(diskAlert);
            
            // Si hay alertas, enviar notificación especial por WebSocket
            if (cpuAlert || memoryAlert || diskAlert) {
                messagingTemplate.convertAndSend("/topic/alerts", metric);
                logger.warn("🚨 ALERTA: CPU={}% (>{}), RAM={}% (>{}), Disk={}% (>{})", 
                           metric.getCpuUsage(), config.getCpuThreshold(),
                           metric.getMemoryUsage(), config.getMemoryThreshold(),
                           metric.getDiskUsage(), config.getDiskThreshold());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error evaluando alertas", e);
        }
    }
    
    /**
     * Método público para obtener métricas instantáneas (sin guardar en BD)
     */
    public SystemMetric getCurrentMetricsSnapshot() {
        return gatherCurrentSystemMetrics();
    }
    
    /**
     * Método público para obtener procesos actuales (sin guardar en BD)
     */
    public List<ProcessInfo> getCurrentProcessesSnapshot(int limit) {
        return gatherHeavyProcesses(limit);
    }
}