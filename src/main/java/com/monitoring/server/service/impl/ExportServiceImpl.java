package com.monitoring.server.service.impl;

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
import com.monitoring.server.service.interfaces.ExportService;  // ← CAMBIO AQUÍ: package correcto
import com.monitoring.server.service.interfaces.ProcessInfoService;
import com.monitoring.server.service.interfaces.SystemMonitorService;

/**
 * 🚀 Implementación principal del servicio de exportación
 * Coordina todos los tipos de exportación y maneja el estado
 */
@Service
public class ExportServiceImpl implements ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportServiceImpl.class);
    
    @Autowired
    private SystemMonitorService systemMonitorService;
    
    @Autowired
    private ProcessInfoService processInfoService;
    
    @Autowired
    private CSVExportService csvExportService;
    
    @Autowired
    private PDFExportService pdfExportService;
    
    @Autowired
    private ExcelExportService excelExportService;
    
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
        ExportResult inProgressResult = ExportResult.inProgress();
        exportCache.put(inProgressResult.getExportId(), inProgressResult);
        
        try {
            // Obtener datos de métricas
            List<SystemMetric> metrics = getSystemMetricsData(request);
            
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
            exportCache.put(errorResult.getExportId(), errorResult);
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
        ExportResult inProgressResult = ExportResult.inProgress();
        exportCache.put(inProgressResult.getExportId(), inProgressResult);
        
        try {
            // Obtener datos de procesos
            List<ProcessInfo> processes = getProcessData(request);
            
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
            exportCache.put(errorResult.getExportId(), errorResult);
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
        ExportResult inProgressResult = ExportResult.inProgress();
        exportCache.put(inProgressResult.getExportId(), inProgressResult);
        
        try {
            // Obtener ambos tipos de datos
            List<SystemMetric> metrics = getSystemMetricsData(request);
            List<ProcessInfo> processes = getProcessData(request);
            
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
            exportCache.put(errorResult.getExportId(), errorResult);
            return CompletableFuture.completedFuture(errorResult);
        }
    }
    
    @Override
    @Async("exportExecutor")
    public CompletableFuture<ExportResult> exportCustomData(ExportRequest request) {
        logger.info("🔄 Iniciando exportación personalizada: {}", request);
        
        if (!exportEnabled) {
            return CompletableFuture.completedFuture(
                ExportResult.error("Export service is disabled")
            );
        }
        
        long startTime = System.currentTimeMillis();
        ExportResult inProgressResult = ExportResult.inProgress();
        exportCache.put(inProgressResult.getExportId(), inProgressResult);
        
        try {
            // Generar exportación personalizada
            byte[] data = generateCustomExport(request);
            String filename = generateFilename("custom_export", request);
            
            // Crear resultado exitoso
            ExportResult result = ExportResult.success(data, filename, request.getFormat())
                .withRecordCount(1) // Placeholder
                .withProcessingTime(System.currentTimeMillis() - startTime);
            
            exportCache.put(result.getExportId(), result);
            
            logger.info("✅ Exportación personalizada completada: {} - {}", 
                       filename, result.getFormattedSize());
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            logger.error("❌ Error en exportación personalizada", e);
            ExportResult errorResult = ExportResult.error("Custom export failed: " + e.getMessage());
            exportCache.put(errorResult.getExportId(), errorResult);
            return CompletableFuture.completedFuture(errorResult);
        }
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
        
        // Remover exportaciones completadas más antigas que 1 hora
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
               csvExportService != null && 
               pdfExportService != null && 
               excelExportService != null &&
               systemMonitorService != null &&
               processInfoService != null;
    }
    
    // === MÉTODOS PRIVADOS DE UTILIDAD ===
    
    private List<SystemMetric> getSystemMetricsData(ExportRequest request) {
        if (request.getPeriod() != null) {
            return systemMonitorService.getMetricsHistory(request.getPeriod());
        } else if (request.getStartDate() != null && request.getEndDate() != null) {
            return systemMonitorService.getMetricsByDateRange(request.getStartDate(), request.getEndDate());
        } else {
            // Default: últimas 24 horas
            return systemMonitorService.getMetricsHistory("24H");
        }
    }
    
    private List<ProcessInfo> getProcessData(ExportRequest request) {
        String filter = request.getProcessFilter() != null ? request.getProcessFilter() : "ALL";
        
        // Obtener procesos según filtro
        if ("HIGH_CPU".equals(filter)) {
            return processInfoService.getHighCpuProcesses(50, 20.0); // Top 50 con >20% CPU
        } else if ("HIGH_MEMORY".equals(filter)) {
            return processInfoService.getHighMemoryProcesses(50, 100.0); // Top 50 con >100MB RAM
        } else {
            return processInfoService.getHeavyProcesses(100, "CPU"); // Top 100 por CPU
        }
    }
    
    private byte[] generateFileData(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) throws Exception {
        return switch (request.getFormat()) {
            case CSV -> {
                if (metrics != null) {
                    yield csvExportService.exportMetrics(metrics, request);
                } else {
                    yield csvExportService.exportProcesses(processes, request);
                }
            }
            case PDF -> {
                if (metrics != null) {
                    yield pdfExportService.exportMetricsReport(metrics, request);
                } else {
                    yield pdfExportService.exportProcessesReport(processes, request);
                }
            }
            case EXCEL -> {
                if (metrics != null) {
                    yield excelExportService.exportMetrics(metrics, request);
                } else {
                    yield excelExportService.exportProcesses(processes, request);
                }
            }
            case JSON -> generateJsonExport(metrics, processes, request);
        };
    }
    
    private byte[] generateCompleteReport(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) throws Exception {
        return switch (request.getFormat()) {
            case PDF -> pdfExportService.exportCompleteReport(metrics, processes, request);
            case EXCEL -> excelExportService.exportCompleteReport(metrics, processes, request);
            case CSV -> {
                // Para CSV completo, combinar ambos tipos de datos
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("=== SYSTEM METRICS ===\n");
                csvContent.append(new String(csvExportService.exportMetrics(metrics, request)));
                csvContent.append("\n\n=== SYSTEM PROCESSES ===\n");
                csvContent.append(new String(csvExportService.exportProcesses(processes, request)));
                yield csvContent.toString().getBytes();
            }
            case JSON -> generateJsonCompleteReport(metrics, processes, request);
        };
    }
    
    private byte[] generateCustomExport(ExportRequest request) throws Exception {
        return switch (request.getFormat()) {
            case CSV -> csvExportService.exportCustom(request);
            case PDF -> pdfExportService.exportCustom(request);
            case EXCEL -> excelExportService.exportCustom(request);
            case JSON -> generateJsonCustom(request);
        };
    }
    
    private byte[] generateJsonExport(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) {
        // Implementación básica de JSON - puedes expandir según necesidades
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"exportInfo\": {\n");
        json.append("    \"generatedAt\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
        json.append("    \"company\": \"").append(companyName).append("\",\n");
        json.append("    \"type\": \"").append(request.getType().getDisplayName()).append("\"\n");
        json.append("  },\n");
        
        if (metrics != null) {
            json.append("  \"systemMetrics\": [\n");
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
            json.append("  ]\n");
        }
        
        if (processes != null) {
            if (metrics != null) json.append(",\n");
            json.append("  \"systemProcesses\": [\n");
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
            json.append("  ]\n");
        }
        
        json.append("}");
        return json.toString().getBytes();
    }
    
    private byte[] generateJsonCompleteReport(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) {
        return generateJsonExport(metrics, processes, request);
    }
    
    private byte[] generateJsonCustom(ExportRequest request) {
        String json = String.format("""
            {
              "exportInfo": {
                "type": "custom",
                "generatedAt": "%s",
                "company": "%s",
                "customConfig": %s
              },
              "data": {
                "message": "Custom export configuration",
                "status": "success"
              }
            }
            """, 
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            companyName,
            request.getCustomConfig() != null ? request.getCustomConfig().toString() : "null"
        );
        return json.getBytes();
    }
    
    private String generateFilename(String type, ExportRequest request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String title = request.getReportTitle() != null ? 
            "_" + request.getReportTitle().replaceAll("[^a-zA-Z0-9]", "_") : "";
        
        return String.format("%s%s_%s.%s", 
            type, title, timestamp, request.getFormat().getExtension());
    }
}