package com.monitoring.server.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.dto.export.ExportRequest;
import com.opencsv.CSVWriter;

/**
 * 📊 Servicio especializado para exportación CSV
 * Genera CSV optimizados con headers personalizables y formato empresarial
 */
@Service
public class CSVExportService {

    private static final Logger logger = LoggerFactory.getLogger(CSVExportService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 📈 Exportar métricas del sistema a CSV
     */
    public byte[] exportMetrics(List<SystemMetric> metrics, ExportRequest request) throws IOException {
        logger.info("📊 Generando CSV de métricas: {} registros", metrics.size());
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        
        try (CSVWriter csvWriter = new CSVWriter(writer, 
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            
            // Escribir header de información
            writeMetricsHeader(csvWriter, request, metrics.size());
            
            // Header de columnas
            String[] headers = {
                "Timestamp",
                "CPU Usage (%)",
                "Memory Usage (%)", 
                "Disk Usage (%)",
                "CPU Alert",
                "Memory Alert",
                "Disk Alert",
                "Total Alerts",
                "System Status"
            };
            csvWriter.writeNext(headers);
            
            // Escribir datos
            for (SystemMetric metric : metrics) {
                String[] data = {
                    metric.getTimestamp().format(TIMESTAMP_FORMATTER),
                    String.format("%.2f", metric.getCpuUsage()),
                    String.format("%.2f", metric.getMemoryUsage()),
                    String.format("%.2f", metric.getDiskUsage()),
                    metric.isCpuAlert() ? "YES" : "NO",
                    metric.isMemoryAlert() ? "YES" : "NO", 
                    metric.isDiskAlert() ? "YES" : "NO",
                    String.valueOf(countAlerts(metric)),
                    getSystemStatus(metric)
                };
                csvWriter.writeNext(data);
            }
            
            // Escribir footer con estadísticas
            writeMetricsFooter(csvWriter, metrics);
            
        }
        
        logger.info("✅ CSV de métricas generado: {} bytes", outputStream.size());
        return outputStream.toByteArray();
    }
    
    /**
     * ⚙️ Exportar procesos del sistema a CSV
     */
    public byte[] exportProcesses(List<ProcessInfo> processes, ExportRequest request) throws IOException {
        logger.info("🔄 Generando CSV de procesos: {} registros", processes.size());
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            
            // Header de información
            writeProcessesHeader(csvWriter, request, processes.size());
            
            // Header de columnas
            String[] headers = {
                "Timestamp",
                "Process ID",
                "Process Name",
                "Username",
                "Status",
                "CPU Usage (%)",
                "Memory Usage (MB)",
                "Disk Usage (MB)",
                "Priority",
                "Threads",
                "Start Time"
            };
            csvWriter.writeNext(headers);
            
            // Escribir datos
            for (ProcessInfo process : processes) {
                String[] data = {
                    process.getTimestamp().format(TIMESTAMP_FORMATTER),
                    process.getProcessId(),
                    process.getProcessName(),
                    process.getUsername() != null ? process.getUsername() : "N/A",
                    process.getStatus(),
                    String.format("%.2f", process.getCpuUsage()),
                    String.format("%.2f", process.getMemoryUsage()),
                    String.format("%.2f", process.getDiskUsage()),
                    "Normal", // Placeholder - agregar campo en entity si necesario
                    "N/A", // Placeholder - agregar campo en entity si necesario
                    process.getTimestamp().format(TIMESTAMP_FORMATTER) // Placeholder
                };
                csvWriter.writeNext(data);
            }
            
            // Footer con estadísticas
            writeProcessesFooter(csvWriter, processes);
            
        }
        
        logger.info("✅ CSV de procesos generado: {} bytes", outputStream.size());
        return outputStream.toByteArray();
    }
    
    /**
     * 🎨 Exportación personalizada
     */
    public byte[] exportCustom(ExportRequest request) throws IOException {
        logger.info("🎯 Generando CSV personalizado");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
        
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            
            // Header personalizado
            csvWriter.writeNext(new String[]{"=== EXPORTACIÓN PERSONALIZADA ==="});
            csvWriter.writeNext(new String[]{"Generado:", java.time.LocalDateTime.now().format(TIMESTAMP_FORMATTER)});
            csvWriter.writeNext(new String[]{"Configuración:", request.getCustomConfig().toString()});
            csvWriter.writeNext(new String[]{""}); // Línea vacía
            
            // Aquí agregarías la lógica específica según customConfig
            // Por ahora, ejemplo básico
            csvWriter.writeNext(new String[]{"Campo 1", "Campo 2", "Campo 3"});
            csvWriter.writeNext(new String[]{"Valor 1", "Valor 2", "Valor 3"});
            
        }
        
        return outputStream.toByteArray();
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    private void writeMetricsHeader(CSVWriter writer, ExportRequest request, int recordCount) {
        writer.writeNext(new String[]{"=== SERVER MONITOR - EXPORT REPORT ==="});
        writer.writeNext(new String[]{"Report Type:", "System Metrics"});
        writer.writeNext(new String[]{"Generated:", java.time.LocalDateTime.now().format(TIMESTAMP_FORMATTER)});
        writer.writeNext(new String[]{"Period:", request.getPeriod() != null ? request.getPeriod() : "Custom Range"});
        writer.writeNext(new String[]{"Start Date:", request.getStartDate() != null ? request.getStartDate().format(TIMESTAMP_FORMATTER) : "N/A"});
        writer.writeNext(new String[]{"End Date:", request.getEndDate() != null ? request.getEndDate().format(TIMESTAMP_FORMATTER) : "N/A"});
        writer.writeNext(new String[]{"Total Records:", String.valueOf(recordCount)});
        writer.writeNext(new String[]{""}); // Línea vacía
    }
    
    private void writeProcessesHeader(CSVWriter writer, ExportRequest request, int recordCount) {
        writer.writeNext(new String[]{"=== SERVER MONITOR - PROCESSES REPORT ==="});
        writer.writeNext(new String[]{"Report Type:", "System Processes"});
        writer.writeNext(new String[]{"Generated:", java.time.LocalDateTime.now().format(TIMESTAMP_FORMATTER)});
        writer.writeNext(new String[]{"Filter:", request.getProcessFilter() != null ? request.getProcessFilter() : "ALL"});
        writer.writeNext(new String[]{"Total Processes:", String.valueOf(recordCount)});
        writer.writeNext(new String[]{""}); // Línea vacía
    }
    
    private void writeMetricsFooter(CSVWriter writer, List<SystemMetric> metrics) {
        writer.writeNext(new String[]{""}); // Línea vacía
        writer.writeNext(new String[]{"=== STATISTICS ==="});
        
        // Calcular estadísticas
        double avgCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).average().orElse(0.0);
        double avgMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).average().orElse(0.0);
        double avgDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).average().orElse(0.0);
        
        double maxCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).max().orElse(0.0);
        double maxMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).max().orElse(0.0);
        double maxDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).max().orElse(0.0);
        
        long totalAlerts = metrics.stream().mapToLong(this::countAlerts).sum();
        
        writer.writeNext(new String[]{"Average CPU Usage:", String.format("%.2f%%", avgCpu)});
        writer.writeNext(new String[]{"Average Memory Usage:", String.format("%.2f%%", avgMemory)});
        writer.writeNext(new String[]{"Average Disk Usage:", String.format("%.2f%%", avgDisk)});
        writer.writeNext(new String[]{"Peak CPU Usage:", String.format("%.2f%%", maxCpu)});
        writer.writeNext(new String[]{"Peak Memory Usage:", String.format("%.2f%%", maxMemory)});
        writer.writeNext(new String[]{"Peak Disk Usage:", String.format("%.2f%%", maxDisk)});
        writer.writeNext(new String[]{"Total Alerts:", String.valueOf(totalAlerts)});
    }
    
    private void writeProcessesFooter(CSVWriter writer, List<ProcessInfo> processes) {
        writer.writeNext(new String[]{""}); // Línea vacía
        writer.writeNext(new String[]{"=== PROCESS STATISTICS ==="});
        
        // Estadísticas de procesos
        double avgCpu = processes.stream().mapToDouble(ProcessInfo::getCpuUsage).average().orElse(0.0);
        double avgMemory = processes.stream().mapToDouble(ProcessInfo::getMemoryUsage).average().orElse(0.0);
        double totalMemory = processes.stream().mapToDouble(ProcessInfo::getMemoryUsage).sum();
        
        ProcessInfo topCpuProcess = processes.stream()
            .max((p1, p2) -> Double.compare(p1.getCpuUsage(), p2.getCpuUsage()))
            .orElse(null);
            
        ProcessInfo topMemoryProcess = processes.stream()
            .max((p1, p2) -> Double.compare(p1.getMemoryUsage(), p2.getMemoryUsage()))
            .orElse(null);
        
        writer.writeNext(new String[]{"Total Processes:", String.valueOf(processes.size())});
        writer.writeNext(new String[]{"Average CPU per Process:", String.format("%.2f%%", avgCpu)});
        writer.writeNext(new String[]{"Average Memory per Process:", String.format("%.2f MB", avgMemory)});
        writer.writeNext(new String[]{"Total Memory Usage:", String.format("%.2f MB", totalMemory)});
        
        if (topCpuProcess != null) {
            writer.writeNext(new String[]{"Top CPU Process:", 
                topCpuProcess.getProcessName() + " (" + String.format("%.2f%%", topCpuProcess.getCpuUsage()) + ")"});
        }
        
        if (topMemoryProcess != null) {
            writer.writeNext(new String[]{"Top Memory Process:", 
                topMemoryProcess.getProcessName() + " (" + String.format("%.2f MB", topMemoryProcess.getMemoryUsage()) + ")"});
        }
    }
    
    private int countAlerts(SystemMetric metric) {
        int count = 0;
        if (metric.isCpuAlert()) count++;
        if (metric.isMemoryAlert()) count++;
        if (metric.isDiskAlert()) count++;
        return count;
    }
    
    private String getSystemStatus(SystemMetric metric) {
        int alertCount = countAlerts(metric);
        if (alertCount == 0) {
            return "NORMAL";
        } else if (alertCount == 1) {
            return "WARNING";
        } else if (alertCount == 2) {
            return "CRITICAL";
        } else {
            return "EMERGENCY";
        }
    }
}