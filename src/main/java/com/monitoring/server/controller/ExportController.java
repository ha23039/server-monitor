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
import com.monitoring.server.service.interfaces.ExportService;

/**
 * 🚀 REST Controller para operaciones de exportación - VERSIÓN CORREGIDA
 * Endpoints simplificados y funcionales para descargar reportes
 */
@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);
    
    @Autowired
    private ExportService exportService;
    
    /**
     * 📊 Endpoint principal para exportar métricas del sistema
     * GET /api/export/metrics?format=CSV&period=24H
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator', 'ROLE_viewer')")
    public CompletableFuture<ResponseEntity<byte[]>> exportMetrics(
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false, defaultValue = "24H") String period,
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
            
            // Ejecutar exportación
            return exportService.exportSystemMetrics(request)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        logger.info("✅ Exportación exitosa: {}", result.getFilename());
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en exportación: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(("Export error: " + result.getErrorMessage()).getBytes());
                    }
                })
                .exceptionally(throwable -> {
                    logger.error("❌ Error crítico en exportación", throwable);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(("Export failed: " + throwable.getMessage()).getBytes());
                });
                
        } catch (Exception e) {
            logger.error("❌ Error procesando solicitud de exportación", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Invalid request: " + e.getMessage()).getBytes())
            );
        }
    }
    
    /**
     * ⚙️ Exportar procesos del sistema
     * GET /api/export/processes?format=CSV&filter=HIGH_CPU
     */
    @GetMapping("/processes")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator')")
    public CompletableFuture<ResponseEntity<byte[]>> exportProcesses(
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false, defaultValue = "ALL") String filter) {
        
        logger.info("🔄 Solicitud de exportación de procesos: formato={}, filtro={}", format, filter);
        
        try {
            ExportRequest request = ExportRequest.processes()
                .format(ExportRequest.ExportFormat.valueOf(format.toUpperCase()));
            
            request.setProcessFilter(filter);
            
            return exportService.exportProcessData(request)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        logger.info("✅ Exportación de procesos exitosa: {}", result.getFilename());
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en exportación de procesos: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(("Process export error: " + result.getErrorMessage()).getBytes());
                    }
                });
                
        } catch (Exception e) {
            logger.error("❌ Error procesando exportación de procesos", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
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
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false, defaultValue = "24H") String period,
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
                        logger.info("✅ Reporte completo exitoso: {}", result.getFilename());
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en reporte completo: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(("Complete report error: " + result.getErrorMessage()).getBytes());
                    }
                });
                
        } catch (Exception e) {
            logger.error("❌ Error procesando reporte completo", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
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
                        logger.info("✅ Exportación personalizada exitosa: {}", result.getFilename());
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en exportación personalizada: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(("Custom export error: " + result.getErrorMessage()).getBytes());
                    }
                });
                
        } catch (Exception e) {
            logger.error("❌ Error procesando exportación personalizada", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Invalid request: " + e.getMessage()).getBytes())
            );
        }
    }
    
    // === ENDPOINTS SIMPLIFICADOS PARA EL DASHBOARD ===

    /**
     * 📊 CSV Metrics endpoint directo
     */
    @GetMapping("/csv/metrics")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator', 'ROLE_viewer')")
    public CompletableFuture<ResponseEntity<byte[]>> exportMetricsCSV(
            @RequestParam(required = false, defaultValue = "24H") String period) {
        
        logger.info("🔍 Exportación CSV de métricas directa - período: {}", period);
        
        ExportRequest request = ExportRequest.metrics()
            .format(ExportRequest.ExportFormat.CSV)
            .period(period);
        
        return exportService.exportSystemMetrics(request)
            .thenApply(result -> {
                if (result.isSuccess()) {
                    return createDownloadResponse(result);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(("CSV export failed: " + result.getErrorMessage()).getBytes());
                }
            });
    }

    /**
     * 📊 PDF Complete report endpoint directo
     */
    @GetMapping("/pdf/complete-report")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator')")
    public CompletableFuture<ResponseEntity<byte[]>> exportCompleteReportPDF(
            @RequestParam(required = false, defaultValue = "24H") String period) {
        
        logger.info("📊 Exportación PDF reporte completo directo - período: {}", period);
        
        ExportRequest request = ExportRequest.completeReport()
            .format(ExportRequest.ExportFormat.PDF)
            .period(period)
            .withCharts()
            .withExecutiveSummary();
        
        return exportService.exportCompleteReport(request)
            .thenApply(result -> {
                if (result.isSuccess()) {
                    return createDownloadResponse(result);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(("PDF export failed: " + result.getErrorMessage()).getBytes());
                }
            });
    }

    /**
     * 📊 Excel Analysis endpoint directo
     */
    @GetMapping("/excel/analysis")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator')")
    public CompletableFuture<ResponseEntity<byte[]>> exportAnalysisExcel(
            @RequestParam(required = false, defaultValue = "24H") String period) {
        
        logger.info("📈 Exportación Excel análisis directo - período: {}", period);
        
        ExportRequest request = ExportRequest.metrics()
            .format(ExportRequest.ExportFormat.EXCEL)
            .period(period);
        
        return exportService.exportSystemMetrics(request)
            .thenApply(result -> {
                if (result.isSuccess()) {
                    return createDownloadResponse(result);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(("Excel export failed: " + result.getErrorMessage()).getBytes());
                }
            });
    }

    /**
     * 📊 CSV Processes endpoint directo
     */
    @GetMapping("/csv/processes")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator')")
    public CompletableFuture<ResponseEntity<byte[]>> exportProcessesCSV(
            @RequestParam(required = false, defaultValue = "ALL") String filter) {
        
        logger.info("⚙️ Exportación CSV de procesos directo - filtro: {}", filter);
        
        ExportRequest request = ExportRequest.processes()
            .format(ExportRequest.ExportFormat.CSV);
        request.setProcessFilter(filter);
        
        return exportService.exportProcessData(request)
            .thenApply(result -> {
                if (result.isSuccess()) {
                    return createDownloadResponse(result);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(("Processes CSV export failed: " + result.getErrorMessage()).getBytes());
                }
            });
    }
    
    /**
     * 🔍 Health check del servicio de exportación
     */
    @GetMapping("/health")
    public ResponseEntity<String> exportHealthCheck() {
        logger.info("🔍 Health check del servicio de exportación");
        
        if (exportService.isExportServiceAvailable()) {
            return ResponseEntity.ok("Export service is available and ready");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Export service is not available");
        }
    }
    
    // === MÉTODO DE UTILIDAD ===
    
    private ResponseEntity<byte[]> createDownloadResponse(ExportResult result) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.getMimeType()));
        headers.setContentDispositionFormData("attachment", result.getFilename());
        headers.add("X-Export-Size", result.getFormattedSize());
        headers.add("X-Export-Records", String.valueOf(result.getRecordCount()));
        headers.add("X-Export-Generated", result.getGeneratedAt().toString());
        headers.add("Access-Control-Expose-Headers", "X-Export-Size,X-Export-Records,X-Export-Generated");
        
        logger.info("✅ Descarga preparada: {} - {} - {} registros", 
                   result.getFilename(), result.getFormattedSize(), result.getRecordCount());
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(result.getData());
    }
}