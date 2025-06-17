package com.monitoring.server.service.interfaces;

import java.util.concurrent.CompletableFuture;
import com.monitoring.server.dto.export.ExportRequest;
import com.monitoring.server.dto.export.ExportResult;

/**
 * 📊 Interface para el servicio de exportación
 * Define operaciones para exportar datos del sistema en múltiples formatos
 */
public interface ExportService {
    
    /**
     * Exportar métricas del sistema
     */
    CompletableFuture<ExportResult> exportSystemMetrics(ExportRequest request);
    
    /**
     * Exportar datos de procesos
     */
    CompletableFuture<ExportResult> exportProcessData(ExportRequest request);
    
    /**
     * Exportar reporte completo del sistema
     */
    CompletableFuture<ExportResult> exportCompleteReport(ExportRequest request);
    
    /**
     * Exportar datos personalizados
     */
    CompletableFuture<ExportResult> exportCustomData(ExportRequest request);
    
    /**
     * Obtener estado de exportación por ID
     */
    ExportResult getExportStatus(String exportId);
    
    /**
     * Cancelar exportación en progreso
     */
    boolean cancelExport(String exportId);
    
    /**
     * Limpiar archivos temporales
     */
    void cleanupTempFiles();
    
    /**
     * Verificar disponibilidad del servicio
     */
    boolean isExportServiceAvailable();
}