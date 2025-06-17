package com.monitoring.server.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.dto.export.ExportRequest;

/**
 * 📄 Servicio PDF básico SIN dependencias externas
 * Genera contenido HTML que se convierte a PDF en el navegador
 */
@Service
public class PDFExportService {

    private static final Logger logger = LoggerFactory.getLogger(PDFExportService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 📊 Exportar métricas del sistema a PDF (HTML)
     */
    public byte[] exportMetricsReport(List<SystemMetric> metrics, ExportRequest request) throws IOException {
        logger.info("📄 Generando reporte PDF de métricas: {} registros", metrics.size());
        
        StringBuilder htmlContent = new StringBuilder();
        
        // HTML completo con estilos CSS para PDF
        htmlContent.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>System Metrics Report</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        margin: 20px; 
                        color: #333;
                        background: white;
                    }
                    .header { 
                        text-align: center; 
                        border-bottom: 3px solid #4F46E5; 
                        padding-bottom: 20px; 
                        margin-bottom: 30px; 
                    }
                    .title { 
                        color: #4F46E5; 
                        font-size: 28px; 
                        margin: 0; 
                        font-weight: bold; 
                    }
                    .subtitle { 
                        color: #666; 
                        font-size: 16px; 
                        margin: 10px 0; 
                    }
                    .summary { 
                        background: #f8f9fa; 
                        padding: 20px; 
                        border-radius: 8px; 
                        margin-bottom: 30px; 
                        border-left: 5px solid #4F46E5; 
                    }
                    .summary h3 { 
                        color: #4F46E5; 
                        margin-top: 0; 
                    }
                    table { 
                        width: 100%; 
                        border-collapse: collapse; 
                        margin-bottom: 30px; 
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1); 
                    }
                    th { 
                        background: #4F46E5; 
                        color: white; 
                        padding: 12px; 
                        text-align: center; 
                        font-weight: bold; 
                    }
                    td { 
                        padding: 10px; 
                        text-align: center; 
                        border-bottom: 1px solid #ddd; 
                    }
                    tr:nth-child(even) { 
                        background: #f8f9fa; 
                    }
                    .status-normal { background: #d4edda; color: #155724; font-weight: bold; }
                    .status-warning { background: #fff3cd; color: #856404; font-weight: bold; }
                    .status-critical { background: #f8d7da; color: #721c24; font-weight: bold; }
                    .metric-high { color: #dc3545; font-weight: bold; }
                    .metric-medium { color: #fd7e14; font-weight: bold; }
                    .metric-normal { color: #28a745; font-weight: bold; }
                    .footer { 
                        text-align: center; 
                        color: #666; 
                        font-size: 12px; 
                        border-top: 1px solid #ddd; 
                        padding-top: 20px; 
                        margin-top: 50px; 
                    }
                    .info-grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 20px;
                        margin-bottom: 30px;
                    }
                    .info-box {
                        background: #e3f2fd;
                        padding: 15px;
                        border-radius: 8px;
                        border-left: 4px solid #2196f3;
                    }
                    .info-box h4 {
                        margin: 0 0 10px 0;
                        color: #1976d2;
                    }
                    @media print {
                        body { margin: 0; }
                        .header { page-break-after: avoid; }
                        table { page-break-inside: avoid; }
                    }
                </style>
            </head>
            <body>
            """);
        
        // Header del reporte
        htmlContent.append("""
            <div class="header">
                <h1 class="title">🖥️ SERVER MONITOR</h1>
                <p class="subtitle">System Metrics Report</p>
                <p>Generated: %s</p>
                <p>Period: %s | Records: %d</p>
            </div>
            """.formatted(
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                request.getPeriod() != null ? request.getPeriod() : "Custom Range",
                metrics.size()
            ));
        
        if (!metrics.isEmpty()) {
            // Resumen ejecutivo
            double avgCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).average().orElse(0.0);
            double avgMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).average().orElse(0.0);
            double avgDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).average().orElse(0.0);
            
            double maxCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).max().orElse(0.0);
            double maxMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).max().orElse(0.0);
            double maxDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).max().orElse(0.0);
            
            long totalAlerts = metrics.stream().mapToLong(this::countAlerts).sum();
            
            htmlContent.append("""
                <div class="summary">
                    <h3>📊 Executive Summary</h3>
                    <div class="info-grid">
                        <div class="info-box">
                            <h4>Average Usage</h4>
                            <p>CPU: %.2f%% | Memory: %.2f%% | Disk: %.2f%%</p>
                        </div>
                        <div class="info-box">
                            <h4>Peak Usage</h4>
                            <p>CPU: %.2f%% | Memory: %.2f%% | Disk: %.2f%%</p>
                        </div>
                        <div class="info-box">
                            <h4>System Health</h4>
                            <p>Total Alerts: %d | Status: %s</p>
                        </div>
                        <div class="info-box">
                            <h4>Data Period</h4>
                            <p>From: %s<br>To: %s</p>
                        </div>
                    </div>
                </div>
                """.formatted(
                    avgCpu, avgMemory, avgDisk,
                    maxCpu, maxMemory, maxDisk,
                    totalAlerts, getSystemHealthStatus(metrics),
                    metrics.get(0).getTimestamp().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                    metrics.get(metrics.size() - 1).getTimestamp().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
                ));
            
            // Tabla de métricas detalladas (últimas 20)
            List<SystemMetric> recentMetrics = metrics.size() > 20 ? 
                metrics.subList(metrics.size() - 20, metrics.size()) : metrics;
            
            htmlContent.append("""
                <h3 style="color: #4F46E5; border-bottom: 2px solid #4F46E5; padding-bottom: 10px;">
                    📈 Recent Metrics (Last %d entries)
                </h3>
                <table>
                    <thead>
                        <tr>
                            <th>Timestamp</th>
                            <th>CPU %%</th>
                            <th>Memory %%</th>
                            <th>Disk %%</th>
                            <th>Alerts</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                """.formatted(recentMetrics.size()));
            
            for (SystemMetric metric : recentMetrics) {
                String status = getSystemStatus(metric);
                String statusClass = switch (status) {
                    case "NORMAL" -> "status-normal";
                    case "WARNING" -> "status-warning";
                    default -> "status-critical";
                };
                
                htmlContent.append("""
                    <tr>
                        <td>%s</td>
                        <td class="%s">%.1f</td>
                        <td class="%s">%.1f</td>
                        <td class="%s">%.1f</td>
                        <td>%s</td>
                        <td class="%s">%s</td>
                    </tr>
                    """.formatted(
                        metric.getTimestamp().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                        getMetricClass(metric.getCpuUsage()), metric.getCpuUsage(),
                        getMetricClass(metric.getMemoryUsage()), metric.getMemoryUsage(),
                        getMetricClass(metric.getDiskUsage()), metric.getDiskUsage(),
                        getAlertsText(metric),
                        statusClass, status
                    ));
            }
            
            htmlContent.append("</tbody></table>");
            
            // Análisis de alertas
            if (totalAlerts > 0) {
                long cpuAlerts = metrics.stream().mapToLong(m -> m.isCpuAlert() ? 1 : 0).sum();
                long memoryAlerts = metrics.stream().mapToLong(m -> m.isMemoryAlert() ? 1 : 0).sum();
                long diskAlerts = metrics.stream().mapToLong(m -> m.isDiskAlert() ? 1 : 0).sum();
                
                htmlContent.append("""
                    <h3 style="color: #dc3545; border-bottom: 2px solid #dc3545; padding-bottom: 10px;">
                        🚨 Alerts Analysis
                    </h3>
                    <div class="summary" style="border-left-color: #dc3545; background: #fff5f5;">
                        <p><strong>Total Alerts:</strong> %d (%.2f%% of measurements)</p>
                        <p><strong>CPU Alerts:</strong> %d | <strong>Memory Alerts:</strong> %d | <strong>Disk Alerts:</strong> %d</p>
                        <p><strong>Recommendation:</strong> %s</p>
                    </div>
                    """.formatted(
                        totalAlerts, (double) totalAlerts / metrics.size() * 100,
                        cpuAlerts, memoryAlerts, diskAlerts,
                        getRecommendation(avgCpu, avgMemory, avgDisk, totalAlerts, metrics.size())
                    ));
            }
        } else {
            htmlContent.append("""
                <div class="summary">
                    <h3>⚠️ No Data Available</h3>
                    <p>No metrics data found for the selected period.</p>
                </div>
                """);
        }
        
        // Footer
        htmlContent.append("""
            <div class="footer">
                <p>Generated by Server Monitor Enterprise System</p>
                <p>© 2025 Server Monitor - Confidential Report</p>
                <p><strong>Note:</strong> This HTML report can be saved as PDF using your browser's print function.</p>
            </div>
            </body>
            </html>
            """);
        
        byte[] result = htmlContent.toString().getBytes("UTF-8");
        logger.info("✅ Reporte HTML generado: {} bytes", result.length);
        return result;
    }
    
    /**
     * ⚙️ Exportar procesos del sistema a PDF (HTML)
     */
    public byte[] exportProcessesReport(List<ProcessInfo> processes, ExportRequest request) throws IOException {
        logger.info("🔄 Generando reporte PDF de procesos: {} registros", processes.size());
        
        // Similar estructura pero para procesos
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        htmlContent.append("<title>System Processes Report</title>");
        htmlContent.append("<style>/* Mismos estilos CSS */</style></head><body>");
        
        htmlContent.append("<h1>🖥️ System Processes Report</h1>");
        htmlContent.append("<p>Generated: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("</p>");
        htmlContent.append("<p>Total Processes: ").append(processes.size()).append("</p>");
        
        // Tabla básica de procesos
        htmlContent.append("<table border='1'>");
        htmlContent.append("<tr><th>Process</th><th>PID</th><th>CPU %</th><th>Memory MB</th><th>Status</th></tr>");
        
        for (ProcessInfo process : processes.stream().limit(50).toList()) {
            htmlContent.append("<tr>")
                .append("<td>").append(process.getProcessName()).append("</td>")
                .append("<td>").append(process.getProcessId()).append("</td>")
                .append("<td>").append(String.format("%.2f", process.getCpuUsage())).append("</td>")
                .append("<td>").append(String.format("%.2f", process.getMemoryUsage())).append("</td>")
                .append("<td>").append(process.getStatus()).append("</td>")
                .append("</tr>");
        }
        
        htmlContent.append("</table></body></html>");
        
        return htmlContent.toString().getBytes("UTF-8");
    }
    
    /**
     * 📈 Exportar reporte completo del sistema
     */
    public byte[] exportCompleteReport(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) throws IOException {
        logger.info("📊 Generando reporte PDF completo");
        
        // Combinar métricas y procesos
        byte[] metricsReport = exportMetricsReport(metrics, request);
        // Por simplicidad, retornamos solo el reporte de métricas
        // En una implementación completa, combinarías ambos
        
        return metricsReport;
    }
    
    /**
     * 🎨 Exportación personalizada
     */
    public byte[] exportCustom(ExportRequest request) throws IOException {
        logger.info("🎯 Generando PDF personalizado");
        
        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Custom Report</title></head>
            <body style="font-family: Arial; padding: 20px;">
                <h1>🎨 Custom Export Report</h1>
                <p>Generated: %s</p>
                <p>Configuration: %s</p>
                <p>This is a custom PDF export. Configure your specific requirements.</p>
            </body>
            </html>
            """, 
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            request.getCustomConfig() != null ? request.getCustomConfig().toString() : "Default"
        );
        
        return html.getBytes("UTF-8");
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    private int countAlerts(SystemMetric metric) {
        int count = 0;
        if (metric.isCpuAlert()) count++;
        if (metric.isMemoryAlert()) count++;
        if (metric.isDiskAlert()) count++;
        return count;
    }
    
    private String getSystemStatus(SystemMetric metric) {
        int alertCount = countAlerts(metric);
        if (alertCount == 0) return "NORMAL";
        if (alertCount == 1) return "WARNING";
        return "CRITICAL";
    }
    
    private String getSystemHealthStatus(List<SystemMetric> metrics) {
        long totalAlerts = metrics.stream().mapToLong(this::countAlerts).sum();
        double alertPercentage = (double) totalAlerts / metrics.size() * 100;
        
        if (alertPercentage < 5) return "EXCELLENT";
        if (alertPercentage < 15) return "GOOD";
        if (alertPercentage < 30) return "POOR";
        return "CRITICAL";
    }
    
    private String getMetricClass(double value) {
        if (value > 80) return "metric-high";
        if (value > 60) return "metric-medium";
        return "metric-normal";
    }
    
    private String getAlertsText(SystemMetric metric) {
        int count = countAlerts(metric);
        if (count == 0) return "None";
        
        StringBuilder alerts = new StringBuilder();
        if (metric.isCpuAlert()) alerts.append("CPU ");
        if (metric.isMemoryAlert()) alerts.append("MEM ");
        if (metric.isDiskAlert()) alerts.append("DISK ");
        
        return alerts.toString().trim();
    }
    
    private String getRecommendation(double avgCpu, double avgMemory, double avgDisk, long totalAlerts, int totalMeasurements) {
        if (avgCpu > 80) return "HIGH CPU: Consider upgrading CPU or optimizing processes";
        if (avgMemory > 85) return "HIGH MEMORY: Add more RAM or optimize applications";
        if (avgDisk > 90) return "CRITICAL DISK: Immediate cleanup or storage expansion required";
        if ((double) totalAlerts / totalMeasurements > 0.2) return "Review alert thresholds - high frequency detected";
        return "System performing within normal parameters";
    }
}