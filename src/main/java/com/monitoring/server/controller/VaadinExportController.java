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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monitoring.server.dto.export.ExportRequest;
import com.monitoring.server.dto.export.ExportResult;
import com.monitoring.server.service.interfaces.ExportService;

/**
 * 🎨 Controller de exportación para sesiones WEB de Vaadin
 * NO requiere JWT - usa la autenticación web estándar
 */
@RestController
@RequestMapping("/vaadin-export") // ✅ DIFERENTE PATH - no empieza con /api/
public class VaadinExportController {

    private static final Logger logger = LoggerFactory.getLogger(VaadinExportController.class);
    
    @Autowired
    private ExportService exportService;
    
    /**
     * 📊 CSV Metrics - Compatible con sesión Vaadin
     */
    @GetMapping("/csv/metrics")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator', 'ROLE_viewer')")
    public CompletableFuture<ResponseEntity<byte[]>> exportMetricsCSV(
            @RequestParam(required = false, defaultValue = "24H") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        logger.info("🔍 Vaadin CSV export - período: {}", period);
        
        try {
            ExportRequest request = ExportRequest.metrics()
                .format(ExportRequest.ExportFormat.CSV)
                .period(period)
                .dateRange(startDate, endDate);
            
            return exportService.exportSystemMetrics(request)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        logger.info("✅ CSV export exitoso: {}", result.getFilename());
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en CSV export: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(("CSV export failed: " + result.getErrorMessage()).getBytes());
                    }
                });
        } catch (Exception e) {
            logger.error("❌ Error en CSV export", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("CSV export error: " + e.getMessage()).getBytes())
            );
        }
    }

    /**
     * 📊 PDF Complete Report - Compatible con sesión Vaadin
     */
    @GetMapping("/pdf/complete-report")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator')")
    public CompletableFuture<ResponseEntity<byte[]>> exportCompleteReportPDF(
            @RequestParam(required = false, defaultValue = "24H") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "true") boolean includeCharts,
            @RequestParam(defaultValue = "true") boolean includeExecutiveSummary,
            @RequestParam(required = false) String reportTitle) {
        
        logger.info("📊 Vaadin PDF export - período: {}", period);
        
        try {
            ExportRequest request = ExportRequest.completeReport()
                .format(ExportRequest.ExportFormat.PDF)
                .period(period)
                .dateRange(startDate, endDate);
            
            if (includeCharts) {
                request.withCharts();
            }
            
            if (includeExecutiveSummary) {
                request.withExecutiveSummary();
            }
            
            if (reportTitle != null && !reportTitle.trim().isEmpty()) {
                request.title(reportTitle);
            }
            
            return exportService.exportCompleteReport(request)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        logger.info("✅ PDF export exitoso: {}", result.getFilename());
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en PDF export: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(("PDF export failed: " + result.getErrorMessage()).getBytes());
                    }
                });
        } catch (Exception e) {
            logger.error("❌ Error en PDF export", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("PDF export error: " + e.getMessage()).getBytes())
            );
        }
    }

    /**
     * 📊 Excel Analysis - Compatible con sesión Vaadin
     */
    @GetMapping("/excel/analysis")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator')")
    public CompletableFuture<ResponseEntity<byte[]>> exportAnalysisExcel(
            @RequestParam(required = false, defaultValue = "24H") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        logger.info("📈 Vaadin Excel export - período: {}", period);
        
        try {
            ExportRequest request = ExportRequest.metrics()
                .format(ExportRequest.ExportFormat.EXCEL)
                .period(period)
                .dateRange(startDate, endDate);
            
            return exportService.exportSystemMetrics(request)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        logger.info("✅ Excel export exitoso: {}", result.getFilename());
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en Excel export: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(("Excel export failed: " + result.getErrorMessage()).getBytes());
                    }
                });
        } catch (Exception e) {
            logger.error("❌ Error en Excel export", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Excel export error: " + e.getMessage()).getBytes())
            );
        }
    }

    /**
     * 📊 CSV Processes - Compatible con sesión Vaadin
     */
    @GetMapping("/csv/processes")
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_operator')")
    public CompletableFuture<ResponseEntity<byte[]>> exportProcessesCSV(
            @RequestParam(required = false, defaultValue = "ALL") String filter) {
        
        logger.info("⚙️ Vaadin processes CSV export - filtro: {}", filter);
        
        try {
            ExportRequest request = ExportRequest.processes()
                .format(ExportRequest.ExportFormat.CSV);
            request.setProcessFilter(filter);
            
            return exportService.exportProcessData(request)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        logger.info("✅ Processes CSV export exitoso: {}", result.getFilename());
                        return createDownloadResponse(result);
                    } else {
                        logger.error("❌ Error en processes CSV export: {}", result.getErrorMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(("Processes CSV export failed: " + result.getErrorMessage()).getBytes());
                    }
                });
        } catch (Exception e) {
            logger.error("❌ Error en processes CSV export", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Processes CSV export error: " + e.getMessage()).getBytes())
            );
        }
    }
    
    /**
     * 🔍 Health check específico para Vaadin
     */
    @GetMapping("/health")
    public ResponseEntity<String> vaadinExportHealthCheck() {
        logger.info("🔍 Vaadin export health check");
        
        if (exportService != null && exportService.isExportServiceAvailable()) {
            return ResponseEntity.ok("Vaadin export service is available and ready");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Vaadin export service is not available");
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
        
        logger.info("✅ Vaadin descarga preparada: {} - {} - {} registros", 
                   result.getFilename(), result.getFormattedSize(), result.getRecordCount());
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(result.getData());
    }
}