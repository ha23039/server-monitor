package com.monitoring.server.service.interfaces;

import java.util.concurrent.CompletableFuture;

import com.monitoring.server.dto.export.ExportRequest;
import com.monitoring.server.dto.export.ExportResult;

/**
 * 🚀 Interfaz del servicio principal de exportación
 * Define las operaciones de exportación disponibles
 */
public interface ExportService {
    
    /**
     * 📊 Exportar métricas del sistema
     */
    CompletableFuture<ExportResult> exportSystemMetrics(ExportRequest request);
    
    /**
     * ⚙️ Exportar datos de procesos
     */
    CompletableFuture<ExportResult> exportProcessData(ExportRequest request);
    
    /**
     * 📈 Exportar reporte completo del sistema
     */
    CompletableFuture<ExportResult> exportCompleteReport(ExportRequest request);
    
    /**
     * 🎨 Exportación personalizada
     */
    CompletableFuture<ExportResult> exportCustomData(ExportRequest request);
    
    /**
     * 📋 Obtener estado de una exportación en progreso
     */
    ExportResult getExportStatus(String exportId);
    
    /**
     * ❌ Cancelar una exportación en progreso
     */
    boolean cancelExport(String exportId);
    
    /**
     * 🧹 Limpiar archivos temporales de exportación
     */
    void cleanupTempFiles();
    
    /**
     * ✅ Verificar si el servicio de exportación está disponible
     */
    boolean isExportServiceAvailable();
}