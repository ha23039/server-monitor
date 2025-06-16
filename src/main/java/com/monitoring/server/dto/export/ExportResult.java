package com.monitoring.server.dto.export;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 📊 Resultado de operaciones de exportación
 * Encapsula datos, metadatos y estado de exportaciones
 */
public class ExportResult {
    
    public enum Status {
        SUCCESS, ERROR, IN_PROGRESS, CANCELLED
    }
    
    private Status status;
    private byte[] data;
    private String filename;
    private ExportRequest.ExportFormat format;
    private String mimeType;
    private long sizeBytes;
    private LocalDateTime generatedAt;
    private String errorMessage;
    private int recordCount;
    private long processingTimeMs;
    private String downloadUrl;
    private String exportId;
    
    // Constructores privados - usar factory methods
    private ExportResult() {
        this.generatedAt = LocalDateTime.now();
        this.exportId = UUID.randomUUID().toString();
    }
    
    // === FACTORY METHODS ===
    
    public static ExportResult success(byte[] data, String filename, ExportRequest.ExportFormat format) {
        ExportResult result = new ExportResult();
        result.status = Status.SUCCESS;
        result.data = data;
        result.filename = filename;
        result.format = format;
        result.mimeType = format.getMimeType();
        result.sizeBytes = data != null ? data.length : 0;
        return result;
    }
    
    public static ExportResult error(String errorMessage) {
        ExportResult result = new ExportResult();
        result.status = Status.ERROR;
        result.errorMessage = errorMessage;
        return result;
    }
    
    public static ExportResult inProgress() {
        ExportResult result = new ExportResult();
        result.status = Status.IN_PROGRESS;
        return result;
    }
    
    public static ExportResult cancelled() {
        ExportResult result = new ExportResult();
        result.status = Status.CANCELLED;
        return result;
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    public boolean isError() {
        return status == Status.ERROR;
    }
    
    public boolean isInProgress() {
        return status == Status.IN_PROGRESS;
    }
    
    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }
    
    public boolean isCompleted() {
        return status == Status.SUCCESS || status == Status.ERROR || status == Status.CANCELLED;
    }
    
    public String getFormattedSize() {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeBytes / 1024.0);
        } else {
            return String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
        }
    }
    
    public String getFormattedProcessingTime() {
        if (processingTimeMs < 1000) {
            return processingTimeMs + " ms";
        } else if (processingTimeMs < 60000) {
            return String.format("%.1f s", processingTimeMs / 1000.0);
        } else {
            return String.format("%.1f min", processingTimeMs / 60000.0);
        }
    }
    
    // === BUILDER PATTERN ===
    
    public ExportResult withRecordCount(int recordCount) {
        this.recordCount = recordCount;
        return this;
    }
    
    public ExportResult withProcessingTime(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
        return this;
    }
    
    public ExportResult withDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }
    
    // === GETTERS Y SETTERS ===
    
    public Status getStatus() { 
        return status; 
    }
    
    public void setStatus(Status status) { 
        this.status = status; 
    }
    
    public byte[] getData() { 
        return data; 
    }
    
    public void setData(byte[] data) { 
        this.data = data; 
        this.sizeBytes = data != null ? data.length : 0;
    }
    
    public String getFilename() { 
        return filename; 
    }
    
    public void setFilename(String filename) { 
        this.filename = filename; 
    }
    
    public ExportRequest.ExportFormat getFormat() { 
        return format; 
    }
    
    public void setFormat(ExportRequest.ExportFormat format) { 
        this.format = format;
        if (format != null) {
            this.mimeType = format.getMimeType();
        }
    }
    
    public String getMimeType() { 
        return mimeType; 
    }
    
    public void setMimeType(String mimeType) { 
        this.mimeType = mimeType; 
    }
    
    public long getSizeBytes() { 
        return sizeBytes; 
    }
    
    public void setSizeBytes(long sizeBytes) { 
        this.sizeBytes = sizeBytes; 
    }
    
    public LocalDateTime getGeneratedAt() { 
        return generatedAt; 
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) { 
        this.generatedAt = generatedAt; 
    }
    
    public String getErrorMessage() { 
        return errorMessage; 
    }
    
    public void setErrorMessage(String errorMessage) { 
        this.errorMessage = errorMessage; 
    }
    
    public int getRecordCount() { 
        return recordCount; 
    }
    
    public void setRecordCount(int recordCount) { 
        this.recordCount = recordCount; 
    }
    
    public long getProcessingTimeMs() { 
        return processingTimeMs; 
    }
    
    public void setProcessingTimeMs(long processingTimeMs) { 
        this.processingTimeMs = processingTimeMs; 
    }
    
    public String getDownloadUrl() { 
        return downloadUrl; 
    }
    
    public void setDownloadUrl(String downloadUrl) { 
        this.downloadUrl = downloadUrl; 
    }
    
    public String getExportId() { 
        return exportId; 
    }
    
    public void setExportId(String exportId) { 
        this.exportId = exportId; 
    }
    
    @Override
    public String toString() {
        return String.format("ExportResult{id=%s, status=%s, filename=%s, size=%s, recordCount=%d, processingTime=%s}", 
                           exportId, status, filename, getFormattedSize(), recordCount, getFormattedProcessingTime());
    }
}