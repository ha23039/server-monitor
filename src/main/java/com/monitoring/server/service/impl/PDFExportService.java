package com.monitoring.server.service.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.dto.export.ExportRequest;

/**
 * 📄 Servicio especializado para exportación PDF
 * Genera reportes profesionales con gráficos, tablas y análisis
 */
@Service
public class PDFExportService {

    private static final Logger logger = LoggerFactory.getLogger(PDFExportService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Colores corporativos
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(52, 73, 94);   // Azul oscuro
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(149, 165, 166); // Gris
    private static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(39, 174, 96);   // Verde
    private static final DeviceRgb WARNING_COLOR = new DeviceRgb(243, 156, 18);  // Naranja
    private static final DeviceRgb DANGER_COLOR = new DeviceRgb(231, 76, 60);    // Rojo
    
    /**
     * 📊 Exportar métricas del sistema a PDF
     */
    public byte[] exportMetricsReport(List<SystemMetric> metrics, ExportRequest request) throws IOException {
        logger.info("📄 Generando reporte PDF de métricas: {} registros", metrics.size());
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // Header del documento
            addDocumentHeader(document, "System Metrics Report", request);
            
            // Resumen ejecutivo
            addExecutiveSummary(document, metrics);
            
            // Gráficos de tendencias
            if (request.getIncludeCharts() != null && request.getIncludeCharts()) {
                addMetricsCharts(document, metrics);
            }
            
            // Tabla de métricas detallada
            addMetricsTable(document, metrics);
            
            // Análisis de alertas
            addAlertsAnalysis(document, metrics);
            
            // Footer
            addDocumentFooter(document);
            
        }
        
        logger.info("✅ Reporte PDF de métricas generado: {} bytes", outputStream.size());
        return outputStream.toByteArray();
    }
    
    /**
     * ⚙️ Exportar procesos del sistema a PDF
     */
    public byte[] exportProcessesReport(List<ProcessInfo> processes, ExportRequest request) throws IOException {
        logger.info("🔄 Generando reporte PDF de procesos: {} registros", processes.size());
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // Header del documento
            addDocumentHeader(document, "System Processes Report", request);
            
            // Resumen de procesos
            addProcessesSummary(document, processes);
            
            // Top 10 procesos por CPU
            addTopProcessesTable(document, processes, "CPU");
            
            // Top 10 procesos por memoria
            addTopProcessesTable(document, processes, "MEMORY");
            
            // Tabla completa de procesos
            addProcessesTable(document, processes);
            
            // Footer
            addDocumentFooter(document);
            
        }
        
        logger.info("✅ Reporte PDF de procesos generado: {} bytes", outputStream.size());
        return outputStream.toByteArray();
    }
    
    /**
     * 📈 Exportar reporte completo del sistema
     */
    public byte[] exportCompleteReport(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) throws IOException {
        logger.info("📊 Generando reporte PDF completo");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // Header del documento
            addDocumentHeader(document, "Complete System Report", request);
            
            // Índice
            addTableOfContents(document);
            
            // 1. Resumen ejecutivo
            document.add(new Paragraph("1. Executive Summary")
                .setFontSize(18)
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(20));
            addExecutiveSummary(document, metrics);
            
            // 2. Métricas del sistema
            document.add(new Paragraph("2. System Metrics Analysis")
                .setFontSize(18)
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(20));
            
            if (request.getIncludeCharts() != null && request.getIncludeCharts()) {
                addMetricsCharts(document, metrics);
            }
            addMetricsTable(document, metrics);
            
            // 3. Análisis de procesos
            document.add(new Paragraph("3. Process Analysis")
                .setFontSize(18)
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(20));
            addProcessesSummary(document, processes);
            addTopProcessesTable(document, processes, "CPU");
            
            // 4. Alertas y recomendaciones
            document.add(new Paragraph("4. Alerts & Recommendations")
                .setFontSize(18)
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(20));
            addAlertsAnalysis(document, metrics);
            addRecommendations(document, metrics, processes);
            
            // Footer
            addDocumentFooter(document);
            
        }
        
        logger.info("✅ Reporte PDF completo generado: {} bytes", outputStream.size());
        return outputStream.toByteArray();
    }
    
    /**
     * 🎨 Exportación personalizada
     */
    public byte[] exportCustom(ExportRequest request) throws IOException {
        logger.info("🎯 Generando PDF personalizado");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            addDocumentHeader(document, "Custom Report", request);
            
            document.add(new Paragraph("This is a custom PDF export. Configure your specific requirements in the ExportRequest.customConfig parameter.")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(100));
            
            addDocumentFooter(document);
            
        }
        
        return outputStream.toByteArray();
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    private void addDocumentHeader(Document document, String title, ExportRequest request) {
        // Logo y título principal
        Paragraph titleParagraph = new Paragraph("🖥️ SERVER MONITOR")
            .setFontSize(24)
            .setFontColor(PRIMARY_COLOR)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5);
        document.add(titleParagraph);
        
        // Subtítulo
        Paragraph subtitleParagraph = new Paragraph(title)
            .setFontSize(18)
            .setFontColor(SECONDARY_COLOR)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20);
        document.add(subtitleParagraph);
        
        // Información del reporte
        Table infoTable = new Table(2);
        infoTable.setWidth(UnitValue.createPercentValue(100));
        
        addInfoRow(infoTable, "Generated:", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        addInfoRow(infoTable, "Period:", request.getPeriod() != null ? request.getPeriod() : "Custom Range");
        if (request.getStartDate() != null) {
            addInfoRow(infoTable, "Start Date:", request.getStartDate().format(TIMESTAMP_FORMATTER));
        }
        if (request.getEndDate() != null) {
            addInfoRow(infoTable, "End Date:", request.getEndDate().format(TIMESTAMP_FORMATTER));
        }
        addInfoRow(infoTable, "Report Type:", request.getType().getDisplayName());
        
        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addExecutiveSummary(Document document, List<SystemMetric> metrics) {
        document.add(new Paragraph("Executive Summary")
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(10)
            .setMarginBottom(10));
        
        if (metrics.isEmpty()) {
            document.add(new Paragraph("No metrics data available for the selected period.")
                .setFontSize(12)
                .setFontColor(SECONDARY_COLOR));
            return;
        }
        
        // Calcular estadísticas
        double avgCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).average().orElse(0.0);
        double avgMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).average().orElse(0.0);
        double avgDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).average().orElse(0.0);
        
        double maxCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).max().orElse(0.0);
        double maxMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).max().orElse(0.0);
        double maxDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).max().orElse(0.0);
        
        long totalAlerts = metrics.stream().mapToLong(this::countAlerts).sum();
        
        // Tabla de resumen
        Table summaryTable = new Table(3);
        summaryTable.setWidth(UnitValue.createPercentValue(100));
        
        // Headers
        summaryTable.addHeaderCell(createHeaderCell("Metric"));
        summaryTable.addHeaderCell(createHeaderCell("Average"));
        summaryTable.addHeaderCell(createHeaderCell("Peak"));
        
        // Datos
        summaryTable.addCell(createDataCell("CPU Usage"));
        summaryTable.addCell(createDataCell(String.format("%.2f%%", avgCpu), getColorByValue(avgCpu)));
        summaryTable.addCell(createDataCell(String.format("%.2f%%", maxCpu), getColorByValue(maxCpu)));
        
        summaryTable.addCell(createDataCell("Memory Usage"));
        summaryTable.addCell(createDataCell(String.format("%.2f%%", avgMemory), getColorByValue(avgMemory)));
        summaryTable.addCell(createDataCell(String.format("%.2f%%", maxMemory), getColorByValue(maxMemory)));
        
        summaryTable.addCell(createDataCell("Disk Usage"));
        summaryTable.addCell(createDataCell(String.format("%.2f%%", avgDisk), getColorByValue(avgDisk)));
        summaryTable.addCell(createDataCell(String.format("%.2f%%", maxDisk), getColorByValue(maxDisk)));
        
        document.add(summaryTable);
        
        // Resumen de alertas
        document.add(new Paragraph(String.format("Total Alerts Generated: %d", totalAlerts))
            .setFontSize(12)
            .setFontColor(totalAlerts > 0 ? DANGER_COLOR : SUCCESS_COLOR)
            .setMarginTop(10));
        
        document.add(new Paragraph("\n"));
    }
    
    private void addMetricsCharts(Document document, List<SystemMetric> metrics) {
        try {
            document.add(new Paragraph("System Metrics Trends")
                .setFontSize(16)
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(10)
                .setMarginBottom(10));
            
            // Crear dataset para el gráfico
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            
            TimeSeries cpuSeries = new TimeSeries("CPU Usage");
            TimeSeries memorySeries = new TimeSeries("Memory Usage");
            TimeSeries diskSeries = new TimeSeries("Disk Usage");
            
            for (SystemMetric metric : metrics) {
                Minute minute = new Minute(
                    metric.getTimestamp().getMinute(),
                    metric.getTimestamp().getHour(),
                    metric.getTimestamp().getDayOfMonth(),
                    metric.getTimestamp().getMonthValue(),
                    metric.getTimestamp().getYear()
                );
                
                cpuSeries.add(minute, metric.getCpuUsage());
                memorySeries.add(minute, metric.getMemoryUsage());
                diskSeries.add(minute, metric.getDiskUsage());
            }
            
            dataset.addSeries(cpuSeries);
            dataset.addSeries(memorySeries);
            dataset.addSeries(diskSeries);
            
            // Crear gráfico
            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "System Performance Metrics",
                "Time",
                "Usage (%)",
                dataset,
                true, true, false
            );
            
            // Personalizar gráfico
            XYPlot plot = chart.getXYPlot();
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesPaint(0, new Color(231, 76, 60));   // CPU - Rojo
            renderer.setSeriesPaint(1, new Color(52, 152, 219));  // Memory - Azul
            renderer.setSeriesPaint(2, new Color(39, 174, 96));   // Disk - Verde
            plot.setRenderer(renderer);
            
            // Convertir a imagen
            BufferedImage chartImage = chart.createBufferedImage(500, 300);
            ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "PNG", chartStream);
            
            // Agregar al PDF
            Image pdfImage = new Image(ImageDataFactory.create(chartStream.toByteArray()));
            pdfImage.setWidth(UnitValue.createPercentValue(80));
            pdfImage.setTextAlignment(TextAlignment.CENTER);
            document.add(pdfImage);
            
        } catch (Exception e) {
            logger.error("Error generando gráfico: ", e);
            document.add(new Paragraph("Error generating chart: " + e.getMessage())
                .setFontColor(DANGER_COLOR));
        }
        
        document.add(new Paragraph("\n"));
    }
    
    private void addMetricsTable(Document document, List<SystemMetric> metrics) {
        document.add(new Paragraph("Detailed Metrics")
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(10)
            .setMarginBottom(10));
        
        if (metrics.isEmpty()) {
            document.add(new Paragraph("No metrics data available.")
                .setFontSize(12)
                .setFontColor(SECONDARY_COLOR));
            return;
        }
        
        // Limitar a las últimas 20 entradas para evitar PDFs muy largos
        List<SystemMetric> limitedMetrics = metrics.size() > 20 ? 
            metrics.subList(Math.max(0, metrics.size() - 20), metrics.size()) : metrics;
        
        Table table = new Table(8);
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Headers
        table.addHeaderCell(createHeaderCell("Timestamp"));
        table.addHeaderCell(createHeaderCell("CPU %"));
        table.addHeaderCell(createHeaderCell("Memory %"));
        table.addHeaderCell(createHeaderCell("Disk %"));
        table.addHeaderCell(createHeaderCell("CPU Alert"));
        table.addHeaderCell(createHeaderCell("Mem Alert"));
        table.addHeaderCell(createHeaderCell("Disk Alert"));
        table.addHeaderCell(createHeaderCell("Status"));
        
        // Datos
        for (SystemMetric metric : limitedMetrics) {
            table.addCell(createDataCell(metric.getTimestamp().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))));
            table.addCell(createDataCell(String.format("%.1f", metric.getCpuUsage()), getColorByValue(metric.getCpuUsage())));
            table.addCell(createDataCell(String.format("%.1f", metric.getMemoryUsage()), getColorByValue(metric.getMemoryUsage())));
            table.addCell(createDataCell(String.format("%.1f", metric.getDiskUsage()), getColorByValue(metric.getDiskUsage())));
            table.addCell(createDataCell(metric.isCpuAlert() ? "YES" : "NO", metric.isCpuAlert() ? DANGER_COLOR : SUCCESS_COLOR));
            table.addCell(createDataCell(metric.isMemoryAlert() ? "YES" : "NO", metric.isMemoryAlert() ? DANGER_COLOR : SUCCESS_COLOR));
            table.addCell(createDataCell(metric.isDiskAlert() ? "YES" : "NO", metric.isDiskAlert() ? DANGER_COLOR : SUCCESS_COLOR));
            table.addCell(createDataCell(getSystemStatus(metric), getStatusColor(metric)));
        }
        
        document.add(table);
        
        if (metrics.size() > 20) {
            document.add(new Paragraph(String.format("Showing last 20 entries of %d total records", metrics.size()))
                .setFontSize(10)
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(5));
        }
        
        document.add(new Paragraph("\n"));
    }
    
    private void addProcessesSummary(Document document, List<ProcessInfo> processes) {
        document.add(new Paragraph("Process Summary")
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(10)
            .setMarginBottom(10));
        
        if (processes.isEmpty()) {
            document.add(new Paragraph("No process data available.")
                .setFontSize(12)
                .setFontColor(SECONDARY_COLOR));
            return;
        }
        
        // Estadísticas
        double totalCpu = processes.stream().mapToDouble(ProcessInfo::getCpuUsage).sum();
        double totalMemory = processes.stream().mapToDouble(ProcessInfo::getMemoryUsage).sum();
        double avgCpu = processes.stream().mapToDouble(ProcessInfo::getCpuUsage).average().orElse(0.0);
        double avgMemory = processes.stream().mapToDouble(ProcessInfo::getMemoryUsage).average().orElse(0.0);
        
        ProcessInfo topCpuProcess = processes.stream()
            .max((p1, p2) -> Double.compare(p1.getCpuUsage(), p2.getCpuUsage()))
            .orElse(null);
        ProcessInfo topMemoryProcess = processes.stream()
            .max((p1, p2) -> Double.compare(p1.getMemoryUsage(), p2.getMemoryUsage()))
            .orElse(null);
        
        Table summaryTable = new Table(2);
        summaryTable.setWidth(UnitValue.createPercentValue(100));
        
        addInfoRow(summaryTable, "Total Processes:", String.valueOf(processes.size()));
        addInfoRow(summaryTable, "Total CPU Usage:", String.format("%.2f%%", totalCpu));
        addInfoRow(summaryTable, "Total Memory Usage:", String.format("%.2f MB", totalMemory));
        addInfoRow(summaryTable, "Average CPU per Process:", String.format("%.2f%%", avgCpu));
        addInfoRow(summaryTable, "Average Memory per Process:", String.format("%.2f MB", avgMemory));
        
        if (topCpuProcess != null) {
            addInfoRow(summaryTable, "Highest CPU Process:", 
                topCpuProcess.getProcessName() + " (" + String.format("%.2f%%", topCpuProcess.getCpuUsage()) + ")");
        }
        
        if (topMemoryProcess != null) {
            addInfoRow(summaryTable, "Highest Memory Process:", 
                topMemoryProcess.getProcessName() + " (" + String.format("%.2f MB", topMemoryProcess.getMemoryUsage()) + ")");
        }
        
        document.add(summaryTable);
        document.add(new Paragraph("\n"));
    }
    
    private void addTopProcessesTable(Document document, List<ProcessInfo> processes, String sortBy) {
        String title = "sortBy".equals("CPU") ? "Top 10 Processes by CPU Usage" : "Top 10 Processes by Memory Usage";
        
        document.add(new Paragraph(title)
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(10)
            .setMarginBottom(10));
        
        if (processes.isEmpty()) {
            return;
        }
        
        // Ordenar y limitar a top 10
        List<ProcessInfo> sortedProcesses = processes.stream()
            .sorted((p1, p2) -> {
                if ("CPU".equals(sortBy)) {
                    return Double.compare(p2.getCpuUsage(), p1.getCpuUsage());
                } else {
                    return Double.compare(p2.getMemoryUsage(), p1.getMemoryUsage());
                }
            })
            .limit(10)
            .toList();
        
        Table table = new Table(5);
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Headers
        table.addHeaderCell(createHeaderCell("Rank"));
        table.addHeaderCell(createHeaderCell("Process Name"));
        table.addHeaderCell(createHeaderCell("PID"));
        table.addHeaderCell(createHeaderCell("CPU %"));
        table.addHeaderCell(createHeaderCell("Memory MB"));
        
        // Datos
        int rank = 1;
        for (ProcessInfo process : sortedProcesses) {
            table.addCell(createDataCell(String.valueOf(rank++)));
            table.addCell(createDataCell(process.getProcessName()));
            table.addCell(createDataCell(process.getProcessId()));
            table.addCell(createDataCell(String.format("%.2f", process.getCpuUsage()), 
                process.getCpuUsage() > 50 ? DANGER_COLOR : (process.getCpuUsage() > 25 ? WARNING_COLOR : SUCCESS_COLOR)));
            table.addCell(createDataCell(String.format("%.2f", process.getMemoryUsage()), 
                process.getMemoryUsage() > 1000 ? DANGER_COLOR : (process.getMemoryUsage() > 500 ? WARNING_COLOR : SUCCESS_COLOR)));
        }
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }
    
    private void addProcessesTable(Document document, List<ProcessInfo> processes) {
        document.add(new Paragraph("All Processes")
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(10)
            .setMarginBottom(10));
        
        if (processes.isEmpty()) {
            return;
        }
        
        // Limitar para evitar PDFs muy largos
        List<ProcessInfo> limitedProcesses = processes.size() > 30 ? 
            processes.subList(0, 30) : processes;
        
        Table table = new Table(6);
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Headers
        table.addHeaderCell(createHeaderCell("Process Name"));
        table.addHeaderCell(createHeaderCell("PID"));
        table.addHeaderCell(createHeaderCell("User"));
        table.addHeaderCell(createHeaderCell("Status"));
        table.addHeaderCell(createHeaderCell("CPU %"));
        table.addHeaderCell(createHeaderCell("Memory MB"));
        
        // Datos
        for (ProcessInfo process : limitedProcesses) {
            table.addCell(createDataCell(process.getProcessName()));
            table.addCell(createDataCell(process.getProcessId()));
            table.addCell(createDataCell(process.getUsername() != null ? process.getUsername() : "N/A"));
            table.addCell(createDataCell(process.getStatus()));
            table.addCell(createDataCell(String.format("%.2f", process.getCpuUsage())));
            table.addCell(createDataCell(String.format("%.2f", process.getMemoryUsage())));
        }
        
        document.add(table);
        
        if (processes.size() > 30) {
            document.add(new Paragraph(String.format("Showing first 30 entries of %d total processes", processes.size()))
                .setFontSize(10)
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(5));
        }
        
        document.add(new Paragraph("\n"));
    }
    
    private void addAlertsAnalysis(Document document, List<SystemMetric> metrics) {
        document.add(new Paragraph("Alerts Analysis")
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(10)
            .setMarginBottom(10));
        
        if (metrics.isEmpty()) {
            return;
        }
        
        long cpuAlerts = metrics.stream().mapToLong(m -> m.isCpuAlert() ? 1 : 0).sum();
        long memoryAlerts = metrics.stream().mapToLong(m -> m.isMemoryAlert() ? 1 : 0).sum();
        long diskAlerts = metrics.stream().mapToLong(m -> m.isDiskAlert() ? 1 : 0).sum();
        long totalAlerts = cpuAlerts + memoryAlerts + diskAlerts;
        
        Table alertsTable = new Table(2);
        alertsTable.setWidth(UnitValue.createPercentValue(100));
        
        addInfoRow(alertsTable, "Total Alerts:", String.valueOf(totalAlerts));
        addInfoRow(alertsTable, "CPU Alerts:", String.valueOf(cpuAlerts));
        addInfoRow(alertsTable, "Memory Alerts:", String.valueOf(memoryAlerts));
        addInfoRow(alertsTable, "Disk Alerts:", String.valueOf(diskAlerts));
        
        double alertPercentage = (double) totalAlerts / metrics.size() * 100;
        addInfoRow(alertsTable, "Alert Frequency:", String.format("%.2f%% of measurements", alertPercentage));
        
        document.add(alertsTable);
        
        // Evaluación del estado
        String systemHealth;
        DeviceRgb healthColor;
        
        if (alertPercentage < 5) {
            systemHealth = "EXCELLENT - System performing optimally";
            healthColor = SUCCESS_COLOR;
        } else if (alertPercentage < 15) {
            systemHealth = "GOOD - Minor performance issues detected";
            healthColor = WARNING_COLOR;
        } else if (alertPercentage < 30) {
            systemHealth = "POOR - Significant performance problems";
            healthColor = DANGER_COLOR;
        } else {
            systemHealth = "CRITICAL - Immediate attention required";
            healthColor = DANGER_COLOR;
        }
        
        document.add(new Paragraph("System Health Status: " + systemHealth)
            .setFontSize(12)
            .setFontColor(healthColor)
            .setMarginTop(10));
        
        document.add(new Paragraph("\n"));
    }
    
    private void addRecommendations(Document document, List<SystemMetric> metrics, List<ProcessInfo> processes) {
        document.add(new Paragraph("Recommendations")
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(10)
            .setMarginBottom(10));
        
        StringBuilder recommendations = new StringBuilder();
        
        // Análisis de CPU
        double avgCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).average().orElse(0.0);
        if (avgCpu > 80) {
            recommendations.append("• HIGH CPU USAGE: Consider upgrading CPU or optimizing high-usage processes.\n");
        } else if (avgCpu > 60) {
            recommendations.append("• MODERATE CPU USAGE: Monitor CPU-intensive processes and consider load balancing.\n");
        }
        
        // Análisis de memoria
        double avgMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).average().orElse(0.0);
        if (avgMemory > 85) {
            recommendations.append("• HIGH MEMORY USAGE: Add more RAM or optimize memory-intensive applications.\n");
        } else if (avgMemory > 70) {
            recommendations.append("• MODERATE MEMORY USAGE: Consider memory optimization and monitoring.\n");
        }
        
        // Análisis de disco
        double avgDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).average().orElse(0.0);
        if (avgDisk > 90) {
            recommendations.append("• CRITICAL DISK USAGE: Immediate action required - free up disk space or add storage.\n");
        } else if (avgDisk > 80) {
            recommendations.append("• HIGH DISK USAGE: Consider cleanup procedures and storage expansion planning.\n");
        }
        
        // Análisis de procesos
        if (!processes.isEmpty()) {
            ProcessInfo topCpuProcess = processes.stream()
                .max((p1, p2) -> Double.compare(p1.getCpuUsage(), p2.getCpuUsage()))
                .orElse(null);
            
            if (topCpuProcess != null && topCpuProcess.getCpuUsage() > 50) {
                recommendations.append(String.format("• PROCESS OPTIMIZATION: %s is using %.2f%% CPU - investigate optimization opportunities.\n", 
                    topCpuProcess.getProcessName(), topCpuProcess.getCpuUsage()));
            }
        }
        
        // Recomendaciones de alertas
        long totalAlerts = metrics.stream().mapToLong(this::countAlerts).sum();
        if (totalAlerts > metrics.size() * 0.2) {
            recommendations.append("• ALERT THRESHOLD REVIEW: High alert frequency detected - consider reviewing threshold settings.\n");
        }
        
        if (recommendations.length() == 0) {
            recommendations.append("• SYSTEM STATUS: No immediate action required. System is performing within normal parameters.\n");
            recommendations.append("• MAINTENANCE: Continue regular monitoring and maintain current optimization strategies.\n");
        }
        
        document.add(new Paragraph(recommendations.toString())
            .setFontSize(11)
            .setMarginTop(10));
        
        document.add(new Paragraph("\n"));
    }
    
    private void addTableOfContents(Document document) {
        document.add(new Paragraph("Table of Contents")
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(10)
            .setMarginBottom(10));
        
        document.add(new Paragraph("1. Executive Summary\n" +
                                 "2. System Metrics Analysis\n" +
                                 "3. Process Analysis\n" +
                                 "4. Alerts & Recommendations")
            .setFontSize(12)
            .setMarginLeft(20));
        
        document.add(new Paragraph("\n"));
    }
    
    private void addDocumentFooter(Document document) {
        document.add(new Paragraph("---")
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(20));
        
        document.add(new Paragraph("Generated by Server Monitor Enterprise System")
            .setFontSize(10)
            .setFontColor(SECONDARY_COLOR)
            .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("© 2025 Server Monitor - Confidential Report")
            .setFontSize(8)
            .setFontColor(SECONDARY_COLOR)
            .setTextAlignment(TextAlignment.CENTER));
    }
    
    // === MÉTODOS DE UTILIDAD PARA TABLAS ===
    
    private Cell createHeaderCell(String text) {
        return new Cell()
            .add(new Paragraph(text))
            .setBackgroundColor(PRIMARY_COLOR)
            .setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(Border.NO_BORDER)
            .setPadding(8);
    }
    
    private Cell createDataCell(String text) {
        return createDataCell(text, new DeviceRgb(0, 0, 0));
    }
    
    private Cell createDataCell(String text, DeviceRgb color) {
        return new Cell()
            .add(new Paragraph(text))
            .setFontColor(color)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(Border.NO_BORDER)
            .setPadding(5);
    }
    
    private void addInfoRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)).setBorder(Border.NO_BORDER).setPadding(3));
        table.addCell(new Cell().add(new Paragraph(value)).setBorder(Border.NO_BORDER).setPadding(3));
    }
    
    private DeviceRgb getColorByValue(double value) {
        if (value > 80) return DANGER_COLOR;
        if (value > 60) return WARNING_COLOR;
        return SUCCESS_COLOR;
    }
    
    private DeviceRgb getStatusColor(SystemMetric metric) {
        int alerts = countAlerts(metric);
        if (alerts >= 3) return DANGER_COLOR;
        if (alerts >= 2) return DANGER_COLOR;
        if (alerts == 1) return WARNING_COLOR;
        return SUCCESS_COLOR;
    }
    
    private String getSystemStatus(SystemMetric metric) {
        int alertCount = countAlerts(metric);
        if (alertCount == 0) return "NORMAL";
        if (alertCount == 1) return "WARNING";
        if (alertCount == 2) return "CRITICAL";
        return "EMERGENCY";
    }
    
    private int countAlerts(SystemMetric metric) {
        int count = 0;
        if (metric.isCpuAlert()) count++;
        if (metric.isMemoryAlert()) count++;
        if (metric.isDiskAlert()) count++;
        return count;
    }
}