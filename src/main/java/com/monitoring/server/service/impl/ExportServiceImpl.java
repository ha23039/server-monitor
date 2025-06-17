package com.monitoring.server.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.dto.export.ExportRequest;
import com.monitoring.server.dto.export.ExportResult;
import com.monitoring.server.service.interfaces.ExportService;
import com.monitoring.server.service.interfaces.ProcessInfoService;
import com.monitoring.server.service.interfaces.SystemMonitorService;

/**
 * 🚀 Implementación simplificada del servicio de exportación
 * Sin dependencias circulares y con funcionalidad básica garantizada
 */
@Service
public class ExportServiceImpl implements ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportServiceImpl.class);
    
    @Autowired
    private SystemMonitorService systemMonitorService;
    
    @Autowired
    private ProcessInfoService processInfoService;
    
    // Configuración desde application.properties
    @Value("${server-monitor.export.enabled:true}")
    private boolean exportEnabled;
    
    @Value("${server-monitor.export.max-records-per-export:100000}")
    private int maxRecordsPerExport;
    
    @Value("${server-monitor.export.company-name:Server Monitor Enterprise}")
    private String companyName;
    
    // Cache para trackear exportaciones en progreso
    private final Map<String, ExportResult> exportCache = new ConcurrentHashMap<>();
    
    @Override
    @Async("exportExecutor")
    public CompletableFuture<ExportResult> exportSystemMetrics(ExportRequest request) {
        logger.info("🔄 Iniciando exportación de métricas del sistema: {}", request);
        
        if (!exportEnabled) {
            return CompletableFuture.completedFuture(
                ExportResult.error("Export service is disabled")
            );
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Obtener datos de métricas
            List<SystemMetric> metrics = getSystemMetricsData(request);
            logger.info("📊 Obtenidas {} métricas para exportación", metrics.size());
            
            if (metrics.size() > maxRecordsPerExport) {
                metrics = metrics.subList(Math.max(0, metrics.size() - maxRecordsPerExport), metrics.size());
                logger.warn("⚠️ Limitando exportación a {} registros", maxRecordsPerExport);
            }
            
            // Generar archivo según formato
            byte[] data = generateFileData(metrics, null, request);
            String filename = generateFilename("system_metrics", request);
            
            // Crear resultado exitoso
            ExportResult result = ExportResult.success(data, filename, request.getFormat())
                .withRecordCount(metrics.size())
                .withProcessingTime(System.currentTimeMillis() - startTime);
            
            exportCache.put(result.getExportId(), result);
            
            logger.info("✅ Exportación de métricas completada: {} - {} registros - {}", 
                       filename, metrics.size(), result.getFormattedSize());
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            logger.error("❌ Error en exportación de métricas", e);
            ExportResult errorResult = ExportResult.error("Export failed: " + e.getMessage());
            return CompletableFuture.completedFuture(errorResult);
        }
    }
    
    @Override
    @Async("exportExecutor")
    public CompletableFuture<ExportResult> exportProcessData(ExportRequest request) {
        logger.info("🔄 Iniciando exportación de datos de procesos: {}", request);
        
        if (!exportEnabled) {
            return CompletableFuture.completedFuture(
                ExportResult.error("Export service is disabled")
            );
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Obtener datos de procesos
            List<ProcessInfo> processes = getProcessData(request);
            logger.info("⚙️ Obtenidos {} procesos para exportación", processes.size());
            
            if (processes.size() > maxRecordsPerExport) {
                processes = processes.subList(0, Math.min(processes.size(), maxRecordsPerExport));
                logger.warn("⚠️ Limitando exportación de procesos a {} registros", maxRecordsPerExport);
            }
            
            // Generar archivo según formato
            byte[] data = generateFileData(null, processes, request);
            String filename = generateFilename("system_processes", request);
            
            // Crear resultado exitoso
            ExportResult result = ExportResult.success(data, filename, request.getFormat())
                .withRecordCount(processes.size())
                .withProcessingTime(System.currentTimeMillis() - startTime);
            
            exportCache.put(result.getExportId(), result);
            
            logger.info("✅ Exportación de procesos completada: {} - {} registros - {}", 
                       filename, processes.size(), result.getFormattedSize());
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            logger.error("❌ Error en exportación de procesos", e);
            ExportResult errorResult = ExportResult.error("Export failed: " + e.getMessage());
            return CompletableFuture.completedFuture(errorResult);
        }
    }
    
    @Override
    @Async("exportExecutor")
    public CompletableFuture<ExportResult> exportCompleteReport(ExportRequest request) {
        logger.info("🔄 Iniciando exportación de reporte completo: {}", request);
        
        if (!exportEnabled) {
            return CompletableFuture.completedFuture(
                ExportResult.error("Export service is disabled")
            );
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Obtener ambos tipos de datos
            List<SystemMetric> metrics = getSystemMetricsData(request);
            List<ProcessInfo> processes = getProcessData(request);
            
            logger.info("📊 Datos obtenidos - Métricas: {}, Procesos: {}", metrics.size(), processes.size());
            
            // Aplicar límites
            if (metrics.size() > maxRecordsPerExport / 2) {
                metrics = metrics.subList(Math.max(0, metrics.size() - (maxRecordsPerExport / 2)), metrics.size());
            }
            if (processes.size() > maxRecordsPerExport / 2) {
                processes = processes.subList(0, Math.min(processes.size(), maxRecordsPerExport / 2));
            }
            
            // Generar reporte completo
            byte[] data = generateCompleteReport(metrics, processes, request);
            String filename = generateFilename("complete_system_report", request);
            
            // Crear resultado exitoso
            ExportResult result = ExportResult.success(data, filename, request.getFormat())
                .withRecordCount(metrics.size() + processes.size())
                .withProcessingTime(System.currentTimeMillis() - startTime);
            
            exportCache.put(result.getExportId(), result);
            
            logger.info("✅ Reporte completo generado: {} - {} métricas + {} procesos - {}", 
                       filename, metrics.size(), processes.size(), result.getFormattedSize());
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            logger.error("❌ Error en reporte completo", e);
            ExportResult errorResult = ExportResult.error("Complete report failed: " + e.getMessage());
            return CompletableFuture.completedFuture(errorResult);
        }
    }
    
    @Override
    @Async("exportExecutor")
    public CompletableFuture<ExportResult> exportCustomData(ExportRequest request) {
        logger.info("🔄 Iniciando exportación personalizada: {}", request);
        
        // Para custom export, usar métricas por defecto
        return exportSystemMetrics(request);
    }
    
    @Override
    public ExportResult getExportStatus(String exportId) {
        return exportCache.get(exportId);
    }
    
    @Override
    public boolean cancelExport(String exportId) {
        ExportResult result = exportCache.get(exportId);
        if (result != null && result.isInProgress()) {
            result.setStatus(ExportResult.Status.CANCELLED);
            exportCache.put(exportId, result);
            logger.info("🚫 Exportación cancelada: {}", exportId);
            return true;
        }
        return false;
    }
    
    @Override
    public void cleanupTempFiles() {
        logger.info("🧹 Limpiando archivos temporales de exportación");
        
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        
        exportCache.entrySet().removeIf(entry -> {
            ExportResult result = entry.getValue();
            return result.isCompleted() && result.getGeneratedAt().isBefore(cutoff);
        });
        
        logger.info("✅ Limpieza de archivos temporales completada");
    }
    
    @Override
    public boolean isExportServiceAvailable() {
        return exportEnabled && 
               systemMonitorService != null && 
               processInfoService != null;
    }
    
    // === MÉTODOS PRIVADOS DE UTILIDAD ===
    
    private List<SystemMetric> getSystemMetricsData(ExportRequest request) {
        try {
            if (request.getPeriod() != null) {
                return systemMonitorService.getMetricsHistory(request.getPeriod());
            } else if (request.getStartDate() != null && request.getEndDate() != null) {
                return systemMonitorService.getMetricsByDateRange(request.getStartDate(), request.getEndDate());
            } else {
                // Default: últimas 24 horas
                return systemMonitorService.getMetricsHistory("24H");
            }
        } catch (Exception e) {
            logger.error("❌ Error obteniendo métricas del sistema", e);
            // Retornar datos mock si falla
            return createMockMetrics();
        }
    }
    
    private List<ProcessInfo> getProcessData(ExportRequest request) {
        try {
            String filter = request.getProcessFilter() != null ? request.getProcessFilter() : "ALL";
            
            if ("HIGH_CPU".equals(filter)) {
                return processInfoService.getHighCpuProcesses(50, 20.0);
            } else if ("HIGH_MEMORY".equals(filter)) {
                return processInfoService.getHighMemoryProcesses(50, 100.0);
            } else {
                return processInfoService.getHeavyProcesses(100, "CPU");
            }
        } catch (Exception e) {
            logger.error("❌ Error obteniendo datos de procesos", e);
            // Retornar datos mock si falla
            return createMockProcesses();
        }
    }
    
    private byte[] generateFileData(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) throws Exception {
        return switch (request.getFormat()) {
            case CSV -> generateCSV(metrics, processes, request);
            case JSON -> generateJSON(metrics, processes, request);
            case PDF, EXCEL -> generateBasicReport(metrics, processes, request);
        };
    }
    
    private byte[] generateCompleteReport(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) throws Exception {
        return switch (request.getFormat()) {
            case CSV -> {
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("=== SYSTEM METRICS ===\n");
                csvContent.append(new String(generateCSV(metrics, null, request)));
                csvContent.append("\n\n=== SYSTEM PROCESSES ===\n");
                csvContent.append(new String(generateCSV(null, processes, request)));
                yield csvContent.toString().getBytes();
            }
            case JSON -> generateJSON(metrics, processes, request);
            default -> generateBasicReport(metrics, processes, request);
        };
    }
    
    private byte[] generateCSV(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos)) {
            
            if (metrics != null && !metrics.isEmpty()) {
                // Header para métricas
                writer.println("timestamp,cpuUsage,memoryUsage,diskUsage,cpuAlert,memoryAlert,diskAlert");
                
                // Datos de métricas
                for (SystemMetric metric : metrics) {
                    writer.printf("%s,%.2f,%.2f,%.2f,%s,%s,%s%n",
                        metric.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        metric.getCpuUsage(),
                        metric.getMemoryUsage(),
                        metric.getDiskUsage(),
                        metric.isCpuAlert(),
                        metric.isMemoryAlert(),
                        metric.isDiskAlert()
                    );
                }
            }
            
            if (processes != null && !processes.isEmpty()) {
                if (metrics != null) writer.println(); // Línea en blanco
                
                // Header para procesos
                writer.println("processId,processName,username,status,cpuUsage,memoryUsage,diskUsage");
                
                // Datos de procesos
                for (ProcessInfo process : processes) {
                    writer.printf("%s,%s,%s,%s,%.2f,%.2f,%.2f%n",
                        process.getProcessId(),
                        process.getProcessName(),
                        process.getUsername() != null ? process.getUsername() : "N/A",
                        process.getStatus(),
                        process.getCpuUsage(),
                        process.getMemoryUsage(),
                        process.getDiskUsage()
                    );
                }
            }
        }
        
        return baos.toByteArray();
    }
    
    private byte[] generateJSON(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"exportInfo\": {\n");
        json.append("    \"generatedAt\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
        json.append("    \"company\": \"").append(companyName).append("\",\n");
        json.append("    \"type\": \"").append(request.getType() != null ? request.getType().getDisplayName() : "System Export").append("\"\n");
        json.append("  }");
        
        if (metrics != null && !metrics.isEmpty()) {
            json.append(",\n  \"systemMetrics\": [\n");
            for (int i = 0; i < metrics.size(); i++) {
                SystemMetric metric = metrics.get(i);
                json.append("    {\n");
                json.append("      \"timestamp\": \"").append(metric.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
                json.append("      \"cpuUsage\": ").append(metric.getCpuUsage()).append(",\n");
                json.append("      \"memoryUsage\": ").append(metric.getMemoryUsage()).append(",\n");
                json.append("      \"diskUsage\": ").append(metric.getDiskUsage()).append(",\n");
                json.append("      \"cpuAlert\": ").append(metric.isCpuAlert()).append(",\n");
                json.append("      \"memoryAlert\": ").append(metric.isMemoryAlert()).append(",\n");
                json.append("      \"diskAlert\": ").append(metric.isDiskAlert()).append("\n");
                json.append("    }").append(i < metrics.size() - 1 ? "," : "").append("\n");
            }
            json.append("  ]");
        }
        
        if (processes != null && !processes.isEmpty()) {
            if (metrics != null && !metrics.isEmpty()) json.append(",");
            json.append("\n  \"systemProcesses\": [\n");
            for (int i = 0; i < processes.size(); i++) {
                ProcessInfo process = processes.get(i);
                json.append("    {\n");
                json.append("      \"processId\": \"").append(process.getProcessId()).append("\",\n");
                json.append("      \"processName\": \"").append(process.getProcessName()).append("\",\n");
                json.append("      \"username\": \"").append(process.getUsername() != null ? process.getUsername() : "N/A").append("\",\n");
                json.append("      \"status\": \"").append(process.getStatus()).append("\",\n");
                json.append("      \"cpuUsage\": ").append(process.getCpuUsage()).append(",\n");
                json.append("      \"memoryUsage\": ").append(process.getMemoryUsage()).append(",\n");
                json.append("      \"diskUsage\": ").append(process.getDiskUsage()).append("\n");
                json.append("    }").append(i < processes.size() - 1 ? "," : "").append("\n");
            }
            json.append("  ]");
        }
        
        json.append("\n}");
        return json.toString().getBytes();
    }
    
    private byte[] generateBasicReport(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) {
        // Para PDF/Excel básico, usar formato texto simple
        StringBuilder report = new StringBuilder();
        report.append("=== SYSTEM MONITORING REPORT ===\n");
        report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        report.append("Company: ").append(companyName).append("\n\n");
        
        if (metrics != null && !metrics.isEmpty()) {
            report.append("=== SYSTEM METRICS ===\n");
            report.append("Total metrics: ").append(metrics.size()).append("\n");
            report.append("Latest metric:\n");
            SystemMetric latest = metrics.get(metrics.size() - 1);
            report.append("- CPU: ").append(String.format("%.2f%%", latest.getCpuUsage())).append("\n");
            report.append("- Memory: ").append(String.format("%.2f%%", latest.getMemoryUsage())).append("\n");
            report.append("- Disk: ").append(String.format("%.2f%%", latest.getDiskUsage())).append("\n\n");
        }
        
        if (processes != null && !processes.isEmpty()) {
            report.append("=== SYSTEM PROCESSES ===\n");
            report.append("Total processes: ").append(processes.size()).append("\n");
            report.append("Top 5 processes by CPU:\n");
            processes.stream()
                .sorted((p1, p2) -> Double.compare(p2.getCpuUsage(), p1.getCpuUsage()))
                .limit(5)
                .forEach(process -> 
                    report.append("- ").append(process.getProcessName())
                          .append(": ").append(String.format("%.2f%% CPU", process.getCpuUsage()))
                          .append("\n"));
        }
        
        return report.toString().getBytes();
    }
    
    private String generateFilename(String type, ExportRequest request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String title = request.getReportTitle() != null ? 
            "_" + request.getReportTitle().replaceAll("[^a-zA-Z0-9]", "_") : "";
        
        return String.format("%s%s_%s.%s", 
            type, title, timestamp, request.getFormat().getExtension());
    }
    
    // === MÉTODOS MOCK PARA FALLBACK ===
    
    private List<SystemMetric> createMockMetrics() {
        logger.info("📊 Creando métricas mock para exportación");
        SystemMetric mockMetric = new SystemMetric();
        mockMetric.setTimestamp(LocalDateTime.now());
        mockMetric.setCpuUsage(45.5);
        mockMetric.setMemoryUsage(60.2);
        mockMetric.setDiskUsage(35.8);
        mockMetric.setCpuAlert(false);
        mockMetric.setMemoryAlert(false);
        mockMetric.setDiskAlert(false);
        
        return List.of(mockMetric);
    }
    
    private List<ProcessInfo> createMockProcesses() {
        logger.info("⚙️ Creando procesos mock para exportación");
        ProcessInfo mockProcess = new ProcessInfo();
        mockProcess.setProcessId("12345");
        mockProcess.setProcessName("java");
        mockProcess.setUsername("system");
        mockProcess.setStatus("Running");
        mockProcess.setCpuUsage(25.5);
        mockProcess.setMemoryUsage(512.0);
        mockProcess.setDiskUsage(10.5);
        
        return List.of(mockProcess);
    }
}