package com.monitoring.server.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.dto.export.ExportRequest;

/**
 * 📊 Servicio especializado para exportación Excel
 * Genera archivos Excel con múltiples hojas, formato y gráficos
 */
@Service
public class ExcelExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 📈 Exportar métricas del sistema a Excel
     */
    public byte[] exportMetrics(List<SystemMetric> metrics, ExportRequest request) throws IOException {
        logger.info("📊 Generando Excel de métricas: {} registros", metrics.size());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle alertStyle = createAlertStyle(workbook);
            CellStyle summaryStyle = createSummaryStyle(workbook);
            
            // Hoja 1: Resumen
            createSummarySheet(workbook, metrics, headerStyle, summaryStyle);
            
            // Hoja 2: Datos detallados
            createMetricsDataSheet(workbook, metrics, headerStyle, dataStyle, alertStyle);
            
            // Hoja 3: Estadísticas
            createStatisticsSheet(workbook, metrics, headerStyle, dataStyle);
            
            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            logger.info("✅ Excel de métricas generado: {} bytes", outputStream.size());
            return outputStream.toByteArray();
        }
    }
    
    /**
     * ⚙️ Exportar procesos del sistema a Excel
     */
    public byte[] exportProcesses(List<ProcessInfo> processes, ExportRequest request) throws IOException {
        logger.info("🔄 Generando Excel de procesos: {} registros", processes.size());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle alertStyle = createAlertStyle(workbook);
            CellStyle summaryStyle = createSummaryStyle(workbook);
            
            // Hoja 1: Resumen de procesos
            createProcessSummarySheet(workbook, processes, headerStyle, summaryStyle);
            
            // Hoja 2: Top procesos por CPU
            createTopProcessesSheet(workbook, processes, "CPU", headerStyle, dataStyle, alertStyle);
            
            // Hoja 3: Top procesos por memoria
            createTopProcessesSheet(workbook, processes, "MEMORY", headerStyle, dataStyle, alertStyle);
            
            // Hoja 4: Todos los procesos
            createAllProcessesSheet(workbook, processes, headerStyle, dataStyle);
            
            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            logger.info("✅ Excel de procesos generado: {} bytes", outputStream.size());
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 📊 Exportar reporte completo a Excel
     */
    public byte[] exportCompleteReport(List<SystemMetric> metrics, List<ProcessInfo> processes, ExportRequest request) throws IOException {
        logger.info("📊 Generando Excel de reporte completo");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle alertStyle = createAlertStyle(workbook);
            CellStyle summaryStyle = createSummaryStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            
            // Hoja 1: Dashboard ejecutivo
            createExecutiveDashboard(workbook, metrics, processes, titleStyle, headerStyle, summaryStyle);
            
            // Hoja 2: Métricas del sistema
            createMetricsDataSheet(workbook, metrics, headerStyle, dataStyle, alertStyle);
            
            // Hoja 3: Análisis de procesos
            createProcessSummarySheet(workbook, processes, headerStyle, summaryStyle);
            
            // Hoja 4: Top procesos
            createTopProcessesSheet(workbook, processes, "CPU", headerStyle, dataStyle, alertStyle);
            
            // Hoja 5: Alertas y recomendaciones
            createAlertsSheet(workbook, metrics, headerStyle, dataStyle, alertStyle);
            
            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            logger.info("✅ Excel de reporte completo generado: {} bytes", outputStream.size());
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 🎨 Exportación personalizada
     */
    public byte[] exportCustom(ExportRequest request) throws IOException {
        logger.info("🎯 Generando Excel personalizado");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            Sheet sheet = workbook.createSheet("Custom Export");
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // Header
            Row headerRow = sheet.createRow(0);
            Cell titleCell = headerRow.createCell(0);
            titleCell.setCellValue("Custom Export Configuration");
            titleCell.setCellStyle(headerStyle);
            
            // Configuración
            Row configRow = sheet.createRow(2);
            configRow.createCell(0).setCellValue("Configuration:");
            configRow.createCell(1).setCellValue(request.getCustomConfig() != null ? 
                request.getCustomConfig().toString() : "No custom configuration provided");
            
            // Auto-ajustar columnas
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
        }
    }
    
    // === MÉTODOS PARA CREAR HOJAS ESPECÍFICAS ===
    
    private void createSummarySheet(Workbook workbook, List<SystemMetric> metrics, 
                                   CellStyle headerStyle, CellStyle summaryStyle) {
        Sheet sheet = workbook.createSheet("Summary");
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("🖥️ SERVER MONITOR - SYSTEM METRICS SUMMARY");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        
        rowNum++; // Línea vacía
        
        // Información del reporte
        Row infoRow1 = sheet.createRow(rowNum++);
        infoRow1.createCell(0).setCellValue("Generated:");
        infoRow1.createCell(1).setCellValue(LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        Row infoRow2 = sheet.createRow(rowNum++);
        infoRow2.createCell(0).setCellValue("Total Records:");
        infoRow2.createCell(1).setCellValue(metrics.size());
        
        rowNum++; // Línea vacía
        
        if (!metrics.isEmpty()) {
            // Estadísticas
            double avgCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).average().orElse(0.0);
            double avgMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).average().orElse(0.0);
            double avgDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).average().orElse(0.0);
            
            double maxCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).max().orElse(0.0);
            double maxMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).max().orElse(0.0);
            double maxDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).max().orElse(0.0);
            
            // Headers de estadísticas
            Row statsHeaderRow = sheet.createRow(rowNum++);
            statsHeaderRow.createCell(0).setCellValue("Metric");
            statsHeaderRow.createCell(1).setCellValue("Average");
            statsHeaderRow.createCell(2).setCellValue("Peak");
            statsHeaderRow.createCell(3).setCellValue("Status");
            
            for (Cell cell : statsHeaderRow) {
                cell.setCellStyle(headerStyle);
            }
            
            // Datos de estadísticas
            Row cpuRow = sheet.createRow(rowNum++);
            cpuRow.createCell(0).setCellValue("CPU Usage (%)");
            cpuRow.createCell(1).setCellValue(String.format("%.2f", avgCpu));
            cpuRow.createCell(2).setCellValue(String.format("%.2f", maxCpu));
            cpuRow.createCell(3).setCellValue(getStatusText(avgCpu));
            
            Row memoryRow = sheet.createRow(rowNum++);
            memoryRow.createCell(0).setCellValue("Memory Usage (%)");
            memoryRow.createCell(1).setCellValue(String.format("%.2f", avgMemory));
            memoryRow.createCell(2).setCellValue(String.format("%.2f", maxMemory));
            memoryRow.createCell(3).setCellValue(getStatusText(avgMemory));
            
            Row diskRow = sheet.createRow(rowNum++);
            diskRow.createCell(0).setCellValue("Disk Usage (%)");
            diskRow.createCell(1).setCellValue(String.format("%.2f", avgDisk));
            diskRow.createCell(2).setCellValue(String.format("%.2f", maxDisk));
            diskRow.createCell(3).setCellValue(getStatusText(avgDisk));
            
            // Aplicar estilo a las celdas de datos
            for (int i = rowNum - 3; i < rowNum; i++) {
                Row row = sheet.getRow(i);
                for (Cell cell : row) {
                    cell.setCellStyle(summaryStyle);
                }
            }
        }
        
        // Auto-ajustar columnas
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createMetricsDataSheet(Workbook workbook, List<SystemMetric> metrics, 
                                       CellStyle headerStyle, CellStyle dataStyle, CellStyle alertStyle) {
        Sheet sheet = workbook.createSheet("Metrics Data");
        int rowNum = 0;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Timestamp", "CPU %", "Memory %", "Disk %", "CPU Alert", "Memory Alert", "Disk Alert", "Status"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Datos
        for (SystemMetric metric : metrics) {
            Row dataRow = sheet.createRow(rowNum++);
            
            dataRow.createCell(0).setCellValue(metric.getTimestamp().format(TIMESTAMP_FORMATTER));
            dataRow.createCell(1).setCellValue(metric.getCpuUsage());
            dataRow.createCell(2).setCellValue(metric.getMemoryUsage());
            dataRow.createCell(3).setCellValue(metric.getDiskUsage());
            dataRow.createCell(4).setCellValue(metric.isCpuAlert() ? "YES" : "NO");
            dataRow.createCell(5).setCellValue(metric.isMemoryAlert() ? "YES" : "NO");
            dataRow.createCell(6).setCellValue(metric.isDiskAlert() ? "YES" : "NO");
            dataRow.createCell(7).setCellValue(getSystemStatus(metric));
            
            // Aplicar estilos
            for (int i = 0; i < 8; i++) {
                Cell cell = dataRow.getCell(i);
                if (i >= 4 && i <= 6) { // Columnas de alertas
                    cell.setCellStyle(metric.isCpuAlert() || metric.isMemoryAlert() || metric.isDiskAlert() ? alertStyle : dataStyle);
                } else {
                    cell.setCellStyle(dataStyle);
                }
            }
        }
        
        // Auto-ajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createStatisticsSheet(Workbook workbook, List<SystemMetric> metrics, 
                                      CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("Statistics");
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 DETAILED STATISTICS");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        
        rowNum++; // Línea vacía
        
        if (!metrics.isEmpty()) {
            // Calcular estadísticas avanzadas
            double[] cpuValues = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).toArray();
            double[] memoryValues = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).toArray();
            double[] diskValues = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).toArray();
            
            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Metric");
            headerRow.createCell(1).setCellValue("Value");
            headerRow.createCell(2).setCellValue("Description");
            
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }
            
            // CPU Statistics
            addStatisticRow(sheet, rowNum++, "CPU Average", String.format("%.2f%%", average(cpuValues)), "Average CPU usage over the period", dataStyle);
            addStatisticRow(sheet, rowNum++, "CPU Maximum", String.format("%.2f%%", maximum(cpuValues)), "Peak CPU usage recorded", dataStyle);
            addStatisticRow(sheet, rowNum++, "CPU Minimum", String.format("%.2f%%", minimum(cpuValues)), "Minimum CPU usage recorded", dataStyle);
            addStatisticRow(sheet, rowNum++, "CPU Std Dev", String.format("%.2f", standardDeviation(cpuValues)), "CPU usage variability", dataStyle);
            
            rowNum++; // Línea vacía
            
            // Memory Statistics
            addStatisticRow(sheet, rowNum++, "Memory Average", String.format("%.2f%%", average(memoryValues)), "Average memory usage over the period", dataStyle);
            addStatisticRow(sheet, rowNum++, "Memory Maximum", String.format("%.2f%%", maximum(memoryValues)), "Peak memory usage recorded", dataStyle);
            addStatisticRow(sheet, rowNum++, "Memory Minimum", String.format("%.2f%%", minimum(memoryValues)), "Minimum memory usage recorded", dataStyle);
            addStatisticRow(sheet, rowNum++, "Memory Std Dev", String.format("%.2f", standardDeviation(memoryValues)), "Memory usage variability", dataStyle);
            
            rowNum++; // Línea vacía
            
            // Disk Statistics
            addStatisticRow(sheet, rowNum++, "Disk Average", String.format("%.2f%%", average(diskValues)), "Average disk usage over the period", dataStyle);
            addStatisticRow(sheet, rowNum++, "Disk Maximum", String.format("%.2f%%", maximum(diskValues)), "Peak disk usage recorded", dataStyle);
            addStatisticRow(sheet, rowNum++, "Disk Minimum", String.format("%.2f%%", minimum(diskValues)), "Minimum disk usage recorded", dataStyle);
            addStatisticRow(sheet, rowNum++, "Disk Std Dev", String.format("%.2f", standardDeviation(diskValues)), "Disk usage variability", dataStyle);
            
            rowNum++; // Línea vacía
            
            // Alert Statistics
            long totalAlerts = metrics.stream().mapToLong(this::countAlerts).sum();
            long cpuAlerts = metrics.stream().mapToLong(m -> m.isCpuAlert() ? 1 : 0).sum();
            long memoryAlerts = metrics.stream().mapToLong(m -> m.isMemoryAlert() ? 1 : 0).sum();
            long diskAlerts = metrics.stream().mapToLong(m -> m.isDiskAlert() ? 1 : 0).sum();
            
            addStatisticRow(sheet, rowNum++, "Total Alerts", String.valueOf(totalAlerts), "Total number of alerts generated", dataStyle);
            addStatisticRow(sheet, rowNum++, "CPU Alerts", String.valueOf(cpuAlerts), "Number of CPU threshold breaches", dataStyle);
            addStatisticRow(sheet, rowNum++, "Memory Alerts", String.valueOf(memoryAlerts), "Number of memory threshold breaches", dataStyle);
            addStatisticRow(sheet, rowNum++, "Disk Alerts", String.valueOf(diskAlerts), "Number of disk threshold breaches", dataStyle);
            addStatisticRow(sheet, rowNum++, "Alert Rate", String.format("%.2f%%", (double) totalAlerts / metrics.size() * 100), "Percentage of measurements with alerts", dataStyle);
        }
        
        // Auto-ajustar columnas
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createProcessSummarySheet(Workbook workbook, List<ProcessInfo> processes, 
                                          CellStyle headerStyle, CellStyle summaryStyle) {
        Sheet sheet = workbook.createSheet("Process Summary");
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("⚙️ PROCESS ANALYSIS SUMMARY");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        
        rowNum++; // Línea vacía
        
        if (!processes.isEmpty()) {
            // Estadísticas generales
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
            
            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Metric");
            headerRow.createCell(1).setCellValue("Value");
            headerRow.createCell(2).setCellValue("Details");
            
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            addStatisticRow(sheet, rowNum++, "Total Processes", String.valueOf(processes.size()), "Number of processes analyzed", summaryStyle);
            addStatisticRow(sheet, rowNum++, "Total CPU Usage", String.format("%.2f%%", totalCpu), "Combined CPU usage of all processes", summaryStyle);
            addStatisticRow(sheet, rowNum++, "Total Memory Usage", String.format("%.2f MB", totalMemory), "Combined memory usage of all processes", summaryStyle);
            addStatisticRow(sheet, rowNum++, "Average CPU per Process", String.format("%.2f%%", avgCpu), "Mean CPU usage per process", summaryStyle);
            addStatisticRow(sheet, rowNum++, "Average Memory per Process", String.format("%.2f MB", avgMemory), "Mean memory usage per process", summaryStyle);
            
            if (topCpuProcess != null) {
                addStatisticRow(sheet, rowNum++, "Top CPU Process", topCpuProcess.getProcessName(), 
                    String.format("%.2f%% CPU usage", topCpuProcess.getCpuUsage()), summaryStyle);
            }
            
            if (topMemoryProcess != null) {
                addStatisticRow(sheet, rowNum++, "Top Memory Process", topMemoryProcess.getProcessName(), 
                    String.format("%.2f MB memory usage", topMemoryProcess.getMemoryUsage()), summaryStyle);
            }
        }
        
        // Auto-ajustar columnas
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createTopProcessesSheet(Workbook workbook, List<ProcessInfo> processes, String sortBy, 
                                        CellStyle headerStyle, CellStyle dataStyle, CellStyle alertStyle) {
        String sheetName = "Top " + sortBy + " Processes";
        Sheet sheet = workbook.createSheet(sheetName);
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("🏆 TOP 15 PROCESSES BY " + sortBy);
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        
        rowNum++; // Línea vacía
        
        if (!processes.isEmpty()) {
            // Ordenar procesos
            List<ProcessInfo> sortedProcesses = processes.stream()
                .sorted((p1, p2) -> {
                    if ("CPU".equals(sortBy)) {
                        return Double.compare(p2.getCpuUsage(), p1.getCpuUsage());
                    } else {
                        return Double.compare(p2.getMemoryUsage(), p1.getMemoryUsage());
                    }
                })
                .limit(15)
                .toList();
            
            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Rank", "Process Name", "PID", "User", "CPU %", "Memory MB"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            int rank = 1;
            for (ProcessInfo process : sortedProcesses) {
                Row dataRow = sheet.createRow(rowNum++);
                
                dataRow.createCell(0).setCellValue(rank++);
                dataRow.createCell(1).setCellValue(process.getProcessName());
                dataRow.createCell(2).setCellValue(process.getProcessId());
                dataRow.createCell(3).setCellValue(process.getUsername() != null ? process.getUsername() : "N/A");
                dataRow.createCell(4).setCellValue(process.getCpuUsage());
                dataRow.createCell(5).setCellValue(process.getMemoryUsage());
                
                // Aplicar estilos
                for (int i = 0; i < 6; i++) {
                    Cell cell = dataRow.getCell(i);
                    if ((i == 4 && process.getCpuUsage() > 50) || (i == 5 && process.getMemoryUsage() > 1000)) {
                        cell.setCellStyle(alertStyle);
                    } else {
                        cell.setCellStyle(dataStyle);
                    }
                }
            }
        }
        
        // Auto-ajustar columnas
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createAllProcessesSheet(Workbook workbook, List<ProcessInfo> processes, 
                                        CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("All Processes");
        int rowNum = 0;
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Process Name", "PID", "User", "Status", "CPU %", "Memory MB", "Disk MB"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Datos (limitar para evitar archivos muy grandes)
        List<ProcessInfo> limitedProcesses = processes.size() > 1000 ? 
            processes.subList(0, 1000) : processes;
        
        for (ProcessInfo process : limitedProcesses) {
            Row dataRow = sheet.createRow(rowNum++);
            
            dataRow.createCell(0).setCellValue(process.getProcessName());
            dataRow.createCell(1).setCellValue(process.getProcessId());
            dataRow.createCell(2).setCellValue(process.getUsername() != null ? process.getUsername() : "N/A");
            dataRow.createCell(3).setCellValue(process.getStatus());
            dataRow.createCell(4).setCellValue(process.getCpuUsage());
            dataRow.createCell(5).setCellValue(process.getMemoryUsage());
            dataRow.createCell(6).setCellValue(process.getDiskUsage());
            
            // Aplicar estilo
            for (Cell cell : dataRow) {
                cell.setCellStyle(dataStyle);
            }
        }
        
        // Auto-ajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        if (processes.size() > 1000) {
            Row noteRow = sheet.createRow(rowNum + 2);
            noteRow.createCell(0).setCellValue(String.format("Note: Showing first 1000 of %d total processes", processes.size()));
        }
    }
    
    private void createExecutiveDashboard(Workbook workbook, List<SystemMetric> metrics, List<ProcessInfo> processes,
                                         CellStyle titleStyle, CellStyle headerStyle, CellStyle summaryStyle) {
        Sheet sheet = workbook.createSheet("Executive Dashboard");
        int rowNum = 0;
        
        // Título principal
        Row mainTitleRow = sheet.createRow(rowNum++);
        Cell mainTitleCell = mainTitleRow.createCell(0);
        mainTitleCell.setCellValue("🖥️ SERVER MONITOR - EXECUTIVE DASHBOARD");
        mainTitleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        
        rowNum += 2; // Líneas vacías
        
        // Resumen rápido
        if (!metrics.isEmpty()) {
            double avgCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).average().orElse(0.0);
            double avgMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).average().orElse(0.0);
            double avgDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).average().orElse(0.0);
            long totalAlerts = metrics.stream().mapToLong(this::countAlerts).sum();
            
            // KPIs principales
            Row kpiHeaderRow = sheet.createRow(rowNum++);
            kpiHeaderRow.createCell(0).setCellValue("📊 KEY PERFORMANCE INDICATORS");
            kpiHeaderRow.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));
            
            rowNum++; // Línea vacía
            
            Row kpiLabelsRow = sheet.createRow(rowNum++);
            kpiLabelsRow.createCell(0).setCellValue("Average CPU");
            kpiLabelsRow.createCell(1).setCellValue("Average Memory");
            kpiLabelsRow.createCell(2).setCellValue("Average Disk");
            kpiLabelsRow.createCell(3).setCellValue("Total Alerts");
            
            Row kpiValuesRow = sheet.createRow(rowNum++);
            kpiValuesRow.createCell(0).setCellValue(String.format("%.1f%%", avgCpu));
            kpiValuesRow.createCell(1).setCellValue(String.format("%.1f%%", avgMemory));
            kpiValuesRow.createCell(2).setCellValue(String.format("%.1f%%", avgDisk));
            kpiValuesRow.createCell(3).setCellValue(totalAlerts);
            
            // Aplicar estilos a los KPIs
            for (Cell cell : kpiLabelsRow) {
                cell.setCellStyle(headerStyle);
            }
            for (Cell cell : kpiValuesRow) {
                cell.setCellStyle(summaryStyle);
            }
        }
        
        // Sistema de salud
        rowNum += 2;
        Row healthHeaderRow = sheet.createRow(rowNum++);
        healthHeaderRow.createCell(0).setCellValue("🏥 SYSTEM HEALTH STATUS");
        healthHeaderRow.getCell(0).setCellStyle(headerStyle);
        
        String healthStatus = getOverallSystemHealth(metrics);
        Row healthRow = sheet.createRow(rowNum++);
        healthRow.createCell(0).setCellValue("Overall Status:");
        healthRow.createCell(1).setCellValue(healthStatus);
        
        // Auto-ajustar columnas
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createAlertsSheet(Workbook workbook, List<SystemMetric> metrics, 
                                  CellStyle headerStyle, CellStyle dataStyle, CellStyle alertStyle) {
        Sheet sheet = workbook.createSheet("Alerts Analysis");
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("🚨 ALERTS ANALYSIS");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        
        rowNum++; // Línea vacía
        
        if (!metrics.isEmpty()) {
            // Estadísticas de alertas
            long totalAlerts = metrics.stream().mapToLong(this::countAlerts).sum();
            long cpuAlerts = metrics.stream().mapToLong(m -> m.isCpuAlert() ? 1 : 0).sum();
            long memoryAlerts = metrics.stream().mapToLong(m -> m.isMemoryAlert() ? 1 : 0).sum();
            long diskAlerts = metrics.stream().mapToLong(m -> m.isDiskAlert() ? 1 : 0).sum();
            
            Row alertHeaderRow = sheet.createRow(rowNum++);
            alertHeaderRow.createCell(0).setCellValue("Alert Type");
            alertHeaderRow.createCell(1).setCellValue("Count");
            alertHeaderRow.createCell(2).setCellValue("Percentage");
            alertHeaderRow.createCell(3).setCellValue("Status");
            
            for (Cell cell : alertHeaderRow) {
                cell.setCellStyle(headerStyle);
            }
            
            // Datos de alertas
            addAlertRow(sheet, rowNum++, "Total Alerts", totalAlerts, metrics.size(), dataStyle, alertStyle);
            addAlertRow(sheet, rowNum++, "CPU Alerts", cpuAlerts, metrics.size(), dataStyle, alertStyle);
            addAlertRow(sheet, rowNum++, "Memory Alerts", memoryAlerts, metrics.size(), dataStyle, alertStyle);
            addAlertRow(sheet, rowNum++, "Disk Alerts", diskAlerts, metrics.size(), dataStyle, alertStyle);
            
            rowNum++; // Línea vacía
            
            // Recomendaciones
            Row recomHeaderRow = sheet.createRow(rowNum++);
            recomHeaderRow.createCell(0).setCellValue("💡 RECOMMENDATIONS");
            recomHeaderRow.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));
            
            rowNum++;
            
            // Generar recomendaciones
            String[] recommendations = generateRecommendations(metrics);
            for (String recommendation : recommendations) {
                Row recomRow = sheet.createRow(rowNum++);
                recomRow.createCell(0).setCellValue(recommendation);
                recomRow.getCell(0).setCellStyle(dataStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));
            }
        }
        
        // Auto-ajustar columnas
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createAlertStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createSummaryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private void addStatisticRow(Sheet sheet, int rowNum, String metric, String value, String description, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(metric);
        row.createCell(1).setCellValue(value);
        row.createCell(2).setCellValue(description);
        
        for (Cell cell : row) {
            cell.setCellStyle(style);
        }
    }
    
    private void addAlertRow(Sheet sheet, int rowNum, String alertType, long count, int total, CellStyle dataStyle, CellStyle alertStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(alertType);
        row.createCell(1).setCellValue(count);
        row.createCell(2).setCellValue(String.format("%.2f%%", (double) count / total * 100));
        row.createCell(3).setCellValue(count > 0 ? "ACTIVE" : "OK");
        
        CellStyle styleToUse = count > 0 ? alertStyle : dataStyle;
        for (Cell cell : row) {
            cell.setCellStyle(styleToUse);
        }
    }
    
    private String getStatusText(double value) {
        if (value > 80) return "CRITICAL";
        if (value > 60) return "WARNING";
        return "NORMAL";
    }
    
    private String getSystemStatus(SystemMetric metric) {
        int alertCount = countAlerts(metric);
        if (alertCount == 0) return "NORMAL";
        if (alertCount == 1) return "WARNING";
        if (alertCount == 2) return "CRITICAL";
        return "EMERGENCY";
    }
    
    private String getOverallSystemHealth(List<SystemMetric> metrics) {
        if (metrics.isEmpty()) return "NO DATA";
        
        long totalAlerts = metrics.stream().mapToLong(this::countAlerts).sum();
        double alertPercentage = (double) totalAlerts / metrics.size() * 100;
        
        if (alertPercentage < 5) return "EXCELLENT";
        if (alertPercentage < 15) return "GOOD";
        if (alertPercentage < 30) return "POOR";
        return "CRITICAL";
    }
    
    private String[] generateRecommendations(List<SystemMetric> metrics) {
        if (metrics.isEmpty()) {
            return new String[]{"No metrics data available for analysis."};
        }
        
        double avgCpu = metrics.stream().mapToDouble(SystemMetric::getCpuUsage).average().orElse(0.0);
        double avgMemory = metrics.stream().mapToDouble(SystemMetric::getMemoryUsage).average().orElse(0.0);
        double avgDisk = metrics.stream().mapToDouble(SystemMetric::getDiskUsage).average().orElse(0.0);
        
        java.util.List<String> recommendations = new java.util.ArrayList<>();
        
        if (avgCpu > 80) {
            recommendations.add("• HIGH CPU USAGE: Consider upgrading CPU or optimizing high-usage processes");
        } else if (avgCpu > 60) {
            recommendations.add("• MODERATE CPU USAGE: Monitor CPU-intensive processes and consider load balancing");
        }
        
        if (avgMemory > 85) {
            recommendations.add("• HIGH MEMORY USAGE: Add more RAM or optimize memory-intensive applications");
        } else if (avgMemory > 70) {
            recommendations.add("• MODERATE MEMORY USAGE: Consider memory optimization and monitoring");
        }
        
        if (avgDisk > 90) {
            recommendations.add("• CRITICAL DISK USAGE: Immediate action required - free up disk space or add storage");
        } else if (avgDisk > 80) {
            recommendations.add("• HIGH DISK USAGE: Consider cleanup procedures and storage expansion planning");
        }
        
        long totalAlerts = metrics.stream().mapToLong(this::countAlerts).sum();
        if (totalAlerts > metrics.size() * 0.2) {
            recommendations.add("• ALERT THRESHOLD REVIEW: High alert frequency detected - consider reviewing threshold settings");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("• SYSTEM STATUS: No immediate action required. System is performing within normal parameters");
            recommendations.add("• MAINTENANCE: Continue regular monitoring and maintain current optimization strategies");
        }
        
        return recommendations.toArray(new String[0]);
    }
    
    private int countAlerts(SystemMetric metric) {
        int count = 0;
        if (metric.isCpuAlert()) count++;
        if (metric.isMemoryAlert()) count++;
        if (metric.isDiskAlert()) count++;
        return count;
    }
    
    // Métodos estadísticos
    private double average(double[] values) {
        return java.util.Arrays.stream(values).average().orElse(0.0);
    }
    
    private double maximum(double[] values) {
        return java.util.Arrays.stream(values).max().orElse(0.0);
    }
    
    private double minimum(double[] values) {
        return java.util.Arrays.stream(values).min().orElse(0.0);
    }
    
    private double standardDeviation(double[] values) {
        if (values.length <= 1) return 0.0;
        
        double mean = average(values);
        double variance = java.util.Arrays.stream(values)
            .map(x -> Math.pow(x - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.sqrt(variance);
    }
}