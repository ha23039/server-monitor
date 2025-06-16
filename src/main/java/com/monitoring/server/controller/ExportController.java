package com.monitoring.server.controller;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monitoring.server.dto.export.ExportRequest;
import com.monitoring.server.dto.export.ExportResult;
import com.monitoring.server.service.interfaces.ExportService;  // ← IMPORT CORREGIDO

/**
 * 🚀 REST Controller para operaciones de exportación
 * Proporciona endpoints para descargar reportes en múltiples formatos
 */
@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);
    
    @Autowired
    private ExportService exportService;
    
    /**
     * 📊 Exportar métricas del sistema
     * GET /api/export/metrics?format=PDF&period=24H
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator', 'ROLE_viewer')")
    public CompletableFuture<ResponseEntity<byte[]>> exportMetrics(
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "false") boolean includeCharts,
            @RequestParam(defaultValue = "false") boolean includeExecutiveSummary) {
        
        logger.info("🔍 Solicitud de exportación de métricas: formato={}, período={}", format, period);
        
        try {
            // Crear request de exportación
            ExportRequest request = ExportRequest.metrics()
                .format(ExportRequest.ExportFormat.valueOf(format.toUpperCase()))
                .period(period)
                .dateRange(startDate, endDate);
            
            if (includeCharts) {
                request.withCharts();
            }
            
            if (includeExecutiveSummary) {
                request.withExecutiveSummary();
            }
            
            // Ejecutar exportación asíncrona
            return exportService.exportSystemMetrics(request)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en exportación: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(result.getErrorMessage().getBytes());
                    }
                })
                .exceptionally(throwable -> {
                    logger.error("❌ Error crítico en exportación", throwable);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Export failed: internal server error".getBytes());
                });
                
        } catch (Exception e) {
            logger.error("❌ Error procesando solicitud de exportación", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Invalid request: " + e.getMessage()).getBytes())
            );
        }
    }
    
    /**
     * ⚙️ Exportar procesos del sistema
     * GET /api/export/processes?format=EXCEL&filter=HIGH_CPU
     */
    @GetMapping("/processes")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator')")
    public CompletableFuture<ResponseEntity<byte[]>> exportProcesses(
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        logger.info("🔄 Solicitud de exportación de procesos: formato={}, filtro={}", format, filter);
        
        try {
            ExportRequest request = ExportRequest.processes()
                .format(ExportRequest.ExportFormat.valueOf(format.toUpperCase()))
                .dateRange(startDate, endDate);
            
            request.setProcessFilter(filter);
            
            return exportService.exportProcessData(request)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en exportación de procesos: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(result.getErrorMessage().getBytes());
                    }
                });
                
        } catch (Exception e) {
            logger.error("❌ Error procesando exportación de procesos", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Invalid request: " + e.getMessage()).getBytes())
            );
        }
    }
    
    /**
     * 📈 Exportar reporte completo del sistema
     * GET /api/export/complete-report?format=PDF
     */
    @GetMapping("/complete-report")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator')")
    public CompletableFuture<ResponseEntity<byte[]>> exportCompleteReport(
            @RequestParam(defaultValue = "PDF") String format,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "true") boolean includeCharts,
            @RequestParam(defaultValue = "true") boolean includeExecutiveSummary,
            @RequestParam(defaultValue = "true") boolean includeDetailedAnalysis,
            @RequestParam(required = false) String reportTitle) {
        
        logger.info("📊 Solicitud de reporte completo: formato={}", format);
        
        try {
            ExportRequest request = ExportRequest.completeReport()
                .format(ExportRequest.ExportFormat.valueOf(format.toUpperCase()))
                .period(period)
                .dateRange(startDate, endDate);
            
            if (includeCharts) {
                request.withCharts();
            }
            
            if (includeExecutiveSummary) {
                request.withExecutiveSummary();
            }
            
            request.setIncludeDetailedAnalysis(includeDetailedAnalysis);
            
            if (reportTitle != null && !reportTitle.trim().isEmpty()) {
                request.title(reportTitle);
            }
            
            return exportService.exportCompleteReport(request)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en reporte completo: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(result.getErrorMessage().getBytes());
                    }
                });
                
        } catch (Exception e) {
            logger.error("❌ Error procesando reporte completo", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Invalid request: " + e.getMessage()).getBytes())
            );
        }
    }
    
    /**
     * 🎨 Exportación personalizada (POST con configuración avanzada)
     * POST /api/export/custom
     */
    @PostMapping("/custom")
    @PreAuthorize("hasAuthority('ROLE_admin')")
    public CompletableFuture<ResponseEntity<byte[]>> exportCustomData(@RequestBody ExportRequest request) {
        
        logger.info("🎯 Solicitud de exportación personalizada: {}", request);
        
        try {
            // Validación básica
            if (request.getFormat() == null) {
                request.setFormat(ExportRequest.ExportFormat.CSV);
            }
            
            if (request.getType() == null) {
                request.setType(ExportRequest.ExportType.CUSTOM);
            }
            
            return exportService.exportCustomData(request)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en exportación personalizada: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(result.getErrorMessage().getBytes());
                    }
                });
                
        } catch (Exception e) {
            logger.error("❌ Error procesando exportación personalizada", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Invalid request: " + e.getMessage()).getBytes())
            );
        }
    }
    
    /**
     * 📋 Obtener formatos de exportación disponibles
     * GET /api/export/formats
     */
    @GetMapping("/formats")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator', 'ROLE_viewer')")
    public ResponseEntity<ExportFormatsResponse> getAvailableFormats() {
        
        logger.info("📋 Solicitud de formatos disponibles");
        
        ExportFormatsResponse response = new ExportFormatsResponse();
        
        // Formatos disponibles
        for (ExportRequest.ExportFormat format : ExportRequest.ExportFormat.values()) {
            FormatInfo formatInfo = new FormatInfo();
            formatInfo.name = format.name();
            formatInfo.displayName = format.name().toUpperCase();
            formatInfo.extension = format.getExtension();
            formatInfo.mimeType = format.getMimeType();
            formatInfo.supportsCharts = format == ExportRequest.ExportFormat.PDF || format == ExportRequest.ExportFormat.EXCEL;
            formatInfo.description = getFormatDescription(format);
            
            response.formats.add(formatInfo);
        }
        
        // Tipos de exportación disponibles
        for (ExportRequest.ExportType type : ExportRequest.ExportType.values()) {
            ExportTypeInfo typeInfo = new ExportTypeInfo();
            typeInfo.name = type.name();
            typeInfo.displayName = type.getDisplayName();
            typeInfo.description = getTypeDescription(type);
            
            response.types.add(typeInfo);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 📊 Obtener configuración de exportación disponible
     * GET /api/export/config
     */
    @GetMapping("/config")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator', 'ROLE_viewer')")
    public ResponseEntity<ExportConfigResponse> getExportConfiguration() {
        
        logger.info("⚙️ Solicitud de configuración de exportación");
        
        ExportConfigResponse response = new ExportConfigResponse();
        response.maxFileSizeMb = 50;
        response.maxRecordsPerExport = 100000;
        response.defaultFormat = "CSV";
        response.defaultPeriod = "24H";
        response.supportsAsync = true;
        response.supportsCustomFilters = true;
        response.supportedPeriods = new String[]{"1H", "6H", "12H", "24H", "7D", "30D", "90D"};
        response.supportedProcessFilters = new String[]{"ALL", "HIGH_CPU", "HIGH_MEMORY", "HIGH_DISK", "ALERTS_ONLY"};
        
        return ResponseEntity.ok(response);
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    private ResponseEntity<byte[]> createDownloadResponse(ExportResult result) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.getMimeType()));
        headers.setContentDispositionFormData("attachment", result.getFilename());
        headers.add("X-Export-Size", result.getFormattedSize());
        headers.add("X-Export-Records", String.valueOf(result.getRecordCount()));
        headers.add("X-Export-Generated", result.getGeneratedAt().toString());
        
        logger.info("✅ Descarga preparada: {} - {}", result.getFilename(), result.getFormattedSize());
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(result.getData());
    }
    
    private String getFormatDescription(ExportRequest.ExportFormat format) {
        return switch (format) {
            case CSV -> "Comma-separated values file, excellent for data analysis";
            case PDF -> "Professional report with charts and formatting";
            case EXCEL -> "Multi-sheet Excel workbook with advanced features";
            case JSON -> "Structured data format for API integration";
        };
    }
    
    private String getTypeDescription(ExportRequest.ExportType type) {
        return switch (type) {
            case METRICS -> "System performance metrics over time";
            case PROCESSES -> "Running processes and resource usage";
            case COMPLETE_REPORT -> "Comprehensive system analysis report";
            case CUSTOM -> "User-defined export configuration";
        };
    }
    
    // === CLASES DE RESPUESTA ===
    
    public static class ExportFormatsResponse {
        public java.util.List<FormatInfo> formats = new java.util.ArrayList<>();
        public java.util.List<ExportTypeInfo> types = new java.util.ArrayList<>();
    }
    
    public static class FormatInfo {
        public String name;
        public String displayName;
        public String extension;
        public String mimeType;
        public boolean supportsCharts;
        public String description;
    }
    
    public static class ExportTypeInfo {
        public String name;
        public String displayName;
        public String description;
    }
    
    public static class ExportConfigResponse {
        public int maxFileSizeMb;
        public int maxRecordsPerExport;
        public String defaultFormat;
        public String defaultPeriod;
        public boolean supportsAsync;
        public boolean supportsCustomFilters;
        public String[] supportedPeriods;
        public String[] supportedProcessFilters;
    }
}