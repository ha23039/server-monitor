package com.monitoring.server.monitoring.collector;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.monitoring.server.data.entity.SystemMetric;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

@Component
public class SystemMetricCollector {

    private static final Logger logger = LoggerFactory.getLogger(SystemMetricCollector.class);
    
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem os;
    private final CentralProcessor processor;

    public SystemMetricCollector() {
        systemInfo = new SystemInfo();
        hardware = systemInfo.getHardware();
        os = systemInfo.getOperatingSystem();
        processor = hardware.getProcessor();
    }

    public SystemMetric collectMetrics() {
        SystemMetric metric = new SystemMetric();
        metric.setTimestamp(LocalDateTime.now());

        try {
            collectCpuMetrics(metric);
            collectMemoryMetrics(metric);
            collectDiskMetrics(metric);
        } catch (Exception e) {
            logger.error("Error collecting system metrics", e);
        }

        return metric;
    }

    private void collectCpuMetrics(SystemMetric metric) {
        try {
            // Usar la API de sistema operativo de OSHI
            double cpuLoad = processor.getProcessorCpuLoad(1000)[0] * 100.0;
            
            metric.setCpuUsage(Math.max(0, Math.min(100, cpuLoad)));
            metric.setCpuAlert(metric.getCpuUsage() > 80); // Umbral por defecto
        } catch (Exception e) {
            logger.error("Error reading CPU metrics", e);
            metric.setCpuUsage(0);
            metric.setCpuAlert(false);
        }
    }

    private void collectMemoryMetrics(SystemMetric metric) {
        try {
            GlobalMemory memory = hardware.getMemory();
            
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            
            double memoryUsage = 100.0 * (totalMemory - availableMemory) / totalMemory;
            
            metric.setMemoryUsage(Math.max(0, Math.min(100, memoryUsage)));
            metric.setMemoryAlert(metric.getMemoryUsage() > 80); // Umbral por defecto
        } catch (Exception e) {
            logger.error("Error reading memory metrics", e);
            metric.setMemoryUsage(0);
            metric.setMemoryAlert(false);
        }
    }

    private void collectDiskMetrics(SystemMetric metric) {
        try {
            FileSystem fileSystem = os.getFileSystem();
            List<OSFileStore> fileStores = fileSystem.getFileStores();
            
            // Calcular uso total de disco
            long totalSpace = 0;
            long usedSpace = 0;
            
            for (OSFileStore store : fileStores) {
                // Verifica el punto de montaje raíz o de sistema para diferentes SO
                if (store.getMount().equals("/") ||  // Linux
                    store.getMount().equals("C:\\") || // Windows
                    store.getMount().startsWith("/System/Volumes/Data")) { // macOS
                    
                    totalSpace += store.getTotalSpace();
                    usedSpace += store.getTotalSpace() - store.getUsableSpace();
                }
            }
            
            double diskUsage = totalSpace > 0 ? (usedSpace * 100.0 / totalSpace) : 0;
            
            metric.setDiskUsage(Math.max(0, Math.min(100, diskUsage)));
            metric.setDiskAlert(metric.getDiskUsage() > 80); // Umbral por defecto
        } catch (Exception e) {
            logger.error("Error reading disk metrics", e);
            metric.setDiskUsage(0);
            metric.setDiskAlert(false);
        }
    }
}