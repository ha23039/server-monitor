package com.monitoring.server.dto.export;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 📋 Request para operaciones de exportación
 * Define todos los parámetros para exportar datos del sistema
 */
public class ExportRequest {
    
    public enum ExportFormat {
        CSV("csv", "text/csv"),
        PDF("pdf", "application/pdf"),
        EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        JSON("json", "application/json");
        
        private final String extension;
        private final String mimeType;
        
        ExportFormat(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }
        
        public String getExtension() { return extension; }
        public String getMimeType() { return mimeType; }
    }
    
    public enum ExportType {
        METRICS("Métricas del Sistema"),
        PROCESSES("Procesos del Sistema"),
        COMPLETE_REPORT("Reporte Completo"),
        CUSTOM("Exportación Personalizada");
        
        private final String displayName;
        
        ExportType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    private ExportType type;
    private ExportFormat format;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String period; // "1H", "24H", "7D", "1M"
    
    // Filtros específicos
    private Boolean includeAlerts;
    private Boolean includeSystemMetrics;
    private Boolean includeProcessMetrics;
    private String processFilter; // "ALL", "HIGH_CPU", "HIGH_MEMORY", etc.
    private Double cpuThreshold;
    private Double memoryThreshold;
    private Double diskThreshold;
    
    // Configuración de formato
    private Boolean includeCharts;
    private Boolean includeExecutiveSummary;
    private Boolean includeDetailedAnalysis;
    private String reportTitle;
    private String reportDescription;
    
    // Configuración personalizada
    private Map<String, Object> customConfig;
    
    // Constructores
    public ExportRequest() {}
    
    public ExportRequest(ExportType type, ExportFormat format) {
        this.type = type;
        this.format = format;
    }
    
    // === BUILDER PATTERN ===
    
    public static ExportRequest metrics() {
        return new ExportRequest(ExportType.METRICS, ExportFormat.CSV);
    }
    
    public static ExportRequest processes() {
        return new ExportRequest(ExportType.PROCESSES, ExportFormat.CSV);
    }
    
    public static ExportRequest completeReport() {
        return new ExportRequest(ExportType.COMPLETE_REPORT, ExportFormat.PDF);
    }
    
    public ExportRequest format(ExportFormat format) {
        this.format = format;
        return this;
    }
    
    public ExportRequest period(String period) {
        this.period = period;
        return this;
    }
    
    public ExportRequest dateRange(LocalDateTime start, LocalDateTime end) {
        this.startDate = start;
        this.endDate = end;
        return this;
    }
    
    public ExportRequest withCharts() {
        this.includeCharts = true;
        return this;
    }
    
    public ExportRequest withExecutiveSummary() {
        this.includeExecutiveSummary = true;
        return this;
    }
    
    public ExportRequest title(String title) {
        this.reportTitle = title;
        return this;
    }
    
    // === GETTERS Y SETTERS ===
    
    public ExportType getType() { return type; }
    public void setType(ExportType type) { this.type = type; }
    
    public ExportFormat getFormat() { return format; }
    public void setFormat(ExportFormat format) { this.format = format; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    
    public Boolean getIncludeAlerts() { return includeAlerts; }
    public void setIncludeAlerts(Boolean includeAlerts) { this.includeAlerts = includeAlerts; }
    
    public Boolean getIncludeSystemMetrics() { return includeSystemMetrics; }
    public void setIncludeSystemMetrics(Boolean includeSystemMetrics) { this.includeSystemMetrics = includeSystemMetrics; }
    
    public Boolean getIncludeProcessMetrics() { return includeProcessMetrics; }
    public void setIncludeProcessMetrics(Boolean includeProcessMetrics) { this.includeProcessMetrics = includeProcessMetrics; }
    
    public String getProcessFilter() { return processFilter; }
    public void setProcessFilter(String processFilter) { this.processFilter = processFilter; }
    
    public Double getCpuThreshold() { return cpuThreshold; }
    public void setCpuThreshold(Double cpuThreshold) { this.cpuThreshold = cpuThreshold; }
    
    public Double getMemoryThreshold() { return memoryThreshold; }
    public void setMemoryThreshold(Double memoryThreshold) { this.memoryThreshold = memoryThreshold; }
    
    public Double getDiskThreshold() { return diskThreshold; }
    public void setDiskThreshold(Double diskThreshold) { this.diskThreshold = diskThreshold; }
    
    public Boolean getIncludeCharts() { return includeCharts; }
    public void setIncludeCharts(Boolean includeCharts) { this.includeCharts = includeCharts; }
    
    public Boolean getIncludeExecutiveSummary() { return includeExecutiveSummary; }
    public void setIncludeExecutiveSummary(Boolean includeExecutiveSummary) { this.includeExecutiveSummary = includeExecutiveSummary; }
    
    public Boolean getIncludeDetailedAnalysis() { return includeDetailedAnalysis; }
    public void setIncludeDetailedAnalysis(Boolean includeDetailedAnalysis) { this.includeDetailedAnalysis = includeDetailedAnalysis; }
    
    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }
    
    public String getReportDescription() { return reportDescription; }
    public void setReportDescription(String reportDescription) { this.reportDescription = reportDescription; }
    
    public Map<String, Object> getCustomConfig() { return customConfig; }
    public void setCustomConfig(Map<String, Object> customConfig) { this.customConfig = customConfig; }
    
    @Override
    public String toString() {
        return String.format("ExportRequest{type=%s, format=%s, period=%s, startDate=%s, endDate=%s}", 
                           type, format, period, startDate, endDate);
    }
}