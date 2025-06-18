package com.monitoring.server.views.components;

import java.time.LocalDateTime;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.monitoring.server.dto.export.ExportRequest;
import com.monitoring.server.dto.export.ExportResult;
import com.monitoring.server.service.interfaces.ExportService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;

/**
 * 🎨 Dialog corregido para exportaciones - CON autenticación adecuada
 * Usa ExportService inyectado en lugar de llamadas directas HTTP
 */
@Component
public class ExportDialogView extends Dialog {

    private static final Logger logger = LoggerFactory.getLogger(ExportDialogView.class);
    
    // ✅ CORREGIDO: Inyección simple sin duplicados
    @Autowired
    private ExportService exportService;
    
    // Componentes principales
    private RadioButtonGroup<String> typeSelector;
    private ComboBox<String> formatSelector;
    private RadioButtonGroup<String> periodSelector;
    private DateTimePicker startDatePicker;
    private DateTimePicker endDatePicker;
    private TextField reportTitleField;
    private Checkbox includeChartsCheckbox;
    private Checkbox includeExecutiveSummaryCheckbox;
    private Checkbox includeDetailedAnalysisCheckbox;
    private ComboBox<String> processFilterSelector;
    
    // Componentes de progreso
    private ProgressBar progressBar;
    private Span progressLabel;
    private Button exportButton;
    private Button cancelButton;
    private VerticalLayout progressSection;
    
    // Estado
    private boolean isExporting = false;
    private long exportStartTime;
    
    public ExportDialogView() {
        initializeDialog();
        createLayout();
        configureEvents();
    }
    
    private void initializeDialog() {
        setHeaderTitle("📊 Export System Data");
        setWidth("700px");
        setHeight("600px");
        setModal(true);
        setDraggable(true);
        setResizable(false);
        
        addClassName("export-dialog");
    }
    
    private void createLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        
        // === SECCIÓN 1: TIPO DE EXPORTACIÓN ===
        mainLayout.add(createTypeSelectionSection());
        
        // === SECCIÓN 2: FORMATO Y CONFIGURACIÓN ===
        mainLayout.add(createFormatConfigSection());
        
        // === SECCIÓN 3: FILTROS DE TIEMPO ===
        mainLayout.add(createTimeFiltersSection());
        
        // === SECCIÓN 4: OPCIONES AVANZADAS ===
        mainLayout.add(createAdvancedOptionsSection());
        
        // === SECCIÓN 5: PROGRESO Y ACCIONES ===
        progressSection = createProgressSection();
        mainLayout.add(progressSection);
        mainLayout.add(createActionButtonsSection());
        
        add(mainLayout);
    }
    
    private VerticalLayout createTypeSelectionSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        
        H4 title = new H4("🎯 Export Type");
        title.getStyle().set("margin", "0 0 10px 0").set("color", "#2c3e50");
        
        typeSelector = new RadioButtonGroup<>();
        typeSelector.setLabel("Select what to export:");
        typeSelector.setItems("System Metrics", "Process Data", "Complete Report", "Custom Export");
        typeSelector.setValue("System Metrics");
        typeSelector.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        
        section.add(title, typeSelector);
        return section;
    }
    
    private VerticalLayout createFormatConfigSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        
        H4 title = new H4("📄 Format & Configuration");
        title.getStyle().set("margin", "0 0 10px 0").set("color", "#2c3e50");
        
        // Layout horizontal para formato y título
        HorizontalLayout formatRow = new HorizontalLayout();
        formatRow.setWidthFull();
        formatRow.setAlignItems(FlexComponent.Alignment.END);
        
        formatSelector = new ComboBox<>("Export Format");
        formatSelector.setItems("CSV", "PDF", "EXCEL");
        formatSelector.setValue("CSV");
        formatSelector.setWidth("200px");
        formatSelector.setItemLabelGenerator(format -> {
            return switch (format) {
                case "CSV" -> "📊 CSV - Data Analysis";
                case "PDF" -> "📄 PDF - Professional Report";
                case "EXCEL" -> "📈 Excel - Advanced Spreadsheet";
                default -> format;
            };
        });
        
        reportTitleField = new TextField("Report Title (optional)");
        reportTitleField.setPlaceholder("e.g., Monthly Performance Report");
        reportTitleField.setWidth("300px");
        
        formatRow.add(formatSelector, reportTitleField);
        
        section.add(title, formatRow);
        return section;
    }
    
    private VerticalLayout createTimeFiltersSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        
        H4 title = new H4("📅 Time Range");
        title.getStyle().set("margin", "0 0 10px 0").set("color", "#2c3e50");
        
        periodSelector = new RadioButtonGroup<>();
        periodSelector.setLabel("Quick periods:");
        periodSelector.setItems("Last Hour", "Last 6 Hours", "Last 24 Hours", "Last 7 Days", "Last 30 Days", "Custom Range");
        periodSelector.setValue("Last 24 Hours");
        periodSelector.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        
        // Campos de fecha personalizada
        HorizontalLayout customDateLayout = new HorizontalLayout();
        customDateLayout.setWidthFull();
        customDateLayout.setAlignItems(FlexComponent.Alignment.END);
        customDateLayout.setVisible(false); // Inicialmente oculto
        
        startDatePicker = new DateTimePicker("Start Date");
        startDatePicker.setValue(LocalDateTime.now().minusDays(1));
        startDatePicker.setWidth("280px");
        
        endDatePicker = new DateTimePicker("End Date");
        endDatePicker.setValue(LocalDateTime.now());
        endDatePicker.setWidth("280px");
        
        customDateLayout.add(startDatePicker, endDatePicker);
        
        // Mostrar/ocultar campos personalizados
        periodSelector.addValueChangeListener(event -> {
            boolean isCustom = "Custom Range".equals(event.getValue());
            customDateLayout.setVisible(isCustom);
        });
        
        section.add(title, periodSelector, customDateLayout);
        return section;
    }
    
    private VerticalLayout createAdvancedOptionsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        
        H4 title = new H4("⚙️ Advanced Options");
        title.getStyle().set("margin", "0 0 10px 0").set("color", "#2c3e50");
        
        // Opciones de contenido
        VerticalLayout contentOptions = new VerticalLayout();
        contentOptions.setPadding(false);
        contentOptions.setSpacing(false);
        
        includeChartsCheckbox = new Checkbox("Include Charts & Visualizations");
        includeChartsCheckbox.setValue(true);
        includeChartsCheckbox.getStyle().set("margin-bottom", "5px");
        
        includeExecutiveSummaryCheckbox = new Checkbox("Include Executive Summary");
        includeExecutiveSummaryCheckbox.setValue(true);
        includeExecutiveSummaryCheckbox.getStyle().set("margin-bottom", "5px");
        
        includeDetailedAnalysisCheckbox = new Checkbox("Include Detailed Analysis");
        includeDetailedAnalysisCheckbox.setValue(false);
        includeDetailedAnalysisCheckbox.getStyle().set("margin-bottom", "5px");
        
        // Filtro de procesos (solo visible cuando se selecciona Process Data)
        processFilterSelector = new ComboBox<>("Process Filter");
        processFilterSelector.setItems("ALL", "HIGH_CPU", "HIGH_MEMORY", "SYSTEM", "USER");
        processFilterSelector.setValue("ALL");
        processFilterSelector.setVisible(false);
        processFilterSelector.setWidth("100%");
        
        contentOptions.add(includeChartsCheckbox, includeExecutiveSummaryCheckbox, includeDetailedAnalysisCheckbox, processFilterSelector);
        section.add(title, contentOptions);
        
        return section;
    }
    
    private VerticalLayout createProgressSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        section.setVisible(false);
        section.addClassName("progress-section");
        
        H4 title = new H4("📊 Export Progress");
        title.getStyle().set("margin", "0 0 10px 0").set("color", "#2c3e50");
        
        progressBar = new ProgressBar();
        progressBar.setWidth("100%");
        progressBar.setIndeterminate(true);
        
        progressLabel = new Span("Preparing export...");
        progressLabel.getStyle()
            .set("color", "#666")
            .set("font-size", "0.9rem")
            .set("text-align", "center");
        
        section.add(title, progressBar, progressLabel);
        return section;
    }
    
    private HorizontalLayout createActionButtonsSection() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidthFull();
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonsLayout.setSpacing(true);
        
        cancelButton = new Button("Cancel", VaadinIcon.CLOSE.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> close());
        
        exportButton = new Button("Export Data", VaadinIcon.DOWNLOAD.create());
        exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        exportButton.addClickListener(e -> startExport());
        
        buttonsLayout.add(cancelButton, exportButton);
        return buttonsLayout;
    }
    
    private void configureEvents() {
        // Mostrar/ocultar filtro de procesos según el tipo seleccionado
        typeSelector.addValueChangeListener(event -> {
            boolean showProcessFilter = "Process Data".equals(event.getValue()) || 
                                      "Complete Report".equals(event.getValue());
            processFilterSelector.setVisible(showProcessFilter);
        });
        
        // Habilitar/deshabilitar opciones según el formato
        formatSelector.addValueChangeListener(event -> {
            String format = event.getValue();
            boolean supportsCharts = "PDF".equals(format) || "EXCEL".equals(format);
            includeChartsCheckbox.setEnabled(supportsCharts);
            
            if (!supportsCharts) {
                includeChartsCheckbox.setValue(false);
            }
        });
    }
    
    // === MÉTODOS PÚBLICOS PARA EL DASHBOARD ===
    
    public void openForMetrics() {
        typeSelector.setValue("System Metrics");
        formatSelector.setValue("CSV");
        periodSelector.setValue("Last 24 Hours");
        open();
    }
    
    public void openForProcesses() {
        typeSelector.setValue("Process Data");
        formatSelector.setValue("EXCEL");
        periodSelector.setValue("Last Hour");
        open();
    }
    
    public void openForCompleteReport() {
        typeSelector.setValue("Complete Report");
        formatSelector.setValue("PDF");
        periodSelector.setValue("Last 24 Hours");
        includeChartsCheckbox.setValue(true);
        includeExecutiveSummaryCheckbox.setValue(true);
        open();
    }



// === ✅ MÉTODO startExport CON RESET DE ESTADO COMPLETO ===
    
    private void startExport() {
        if (isExporting) {
            return;
        }
        
        try {
            setExportingState(true);
            showProgressSection(true);
            
            // Construir URL del endpoint según configuración
            String exportUrl = buildExportUrl();
            
            logger.info("🚀 Iniciando descarga desde: {}", exportUrl);
            
            // ✅ SOLUCIÓN COMPLETA: JavaScript maneja TODO + Reset de estado Java
            UI.getCurrent().getPage().executeJs("""
                console.log('🚀 Abriendo descarga:', $0);
                
                fetch($0, {
                    method: 'GET',
                    credentials: 'include'
                })
                .then(response => {
                    console.log('Response status:', response.status);
                    if (response.ok) {
                        return response.blob();
                    } else {
                        throw new Error('Export failed: ' + response.status);
                    }
                })
                .then(blob => {
                    console.log('✅ Blob recibido:', blob.size, 'bytes');
                    
                    // ✅ CORREGIDO: Detectar extensión desde URL
                    const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-');
                    let extension = '.csv'; // Default
                    
                    if ($0.includes('/pdf/')) extension = '.pdf';
                    else if ($0.includes('/excel/')) extension = '.xlsx';
                    else if ($0.includes('format=JSON')) extension = '.json';
                    else if ($0.includes('format=PDF')) extension = '.pdf';
                    else if ($0.includes('format=EXCEL')) extension = '.xlsx';
                    
                    const filename = 'export_' + timestamp + extension;
                    
                    // Crear descarga
                    const url = URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = filename;
                    a.style.display = 'none';
                    document.body.appendChild(a);
                    a.click();
                    document.body.removeChild(a);
                    URL.revokeObjectURL(url);
                    
                    console.log('✅ Descarga completada:', filename);
                    
                    // ✅ CRÍTICO: Marcar como completado para que Java resetee estado
                    window.exportCompleted = true;
                    
                    // ✅ CRÍTICO: Resetear UI y cerrar modal
                    setTimeout(() => {
                        // Buscar el dialog y resetear su estado
                        const dialog = document.querySelector('vaadin-dialog-overlay');
                        if (dialog) {
                            // Resetear botón de exportación
                            const exportButton = dialog.querySelector('vaadin-button[theme*="primary"]');
                            if (exportButton) {
                                exportButton.disabled = false;
                                exportButton.textContent = 'Export Data';
                                const icon = exportButton.querySelector('vaadin-icon');
                                if (icon) {
                                    icon.setAttribute('icon', 'vaadin:download');
                                }
                            }
                            
                            // Ocultar barra de progreso
                            const progressSection = dialog.querySelector('.progress-section');
                            if (progressSection) {
                                progressSection.style.display = 'none';
                            }
                            
                            // Cerrar modal
                            const closeButton = dialog.querySelector('vaadin-button[theme*="tertiary"]');
                            if (closeButton) {
                                closeButton.click();
                                console.log('✅ Modal cerrado y estado reseteado');
                            }
                        }
                        
                        // Mostrar notificación
                        const notification = document.createElement('vaadin-notification');
                        notification.setAttribute('position', 'top-end');
                        notification.setAttribute('theme', 'success');
                        notification.setAttribute('duration', '3000');
                        notification.textContent = '✅ Export completed! File: ' + filename;
                        document.body.appendChild(notification);
                        notification.open = true;
                        
                        setTimeout(() => {
                            if (notification.parentNode) {
                                notification.parentNode.removeChild(notification);
                            }
                        }, 3500);
                    }, 1000);
                })
                .catch(error => {
                    console.error('❌ Error en descarga:', error);
                    alert('Error downloading file: ' + error.message);
                    
                    // ✅ CRÍTICO: Marcar como error para reseteo
                    window.exportCompleted = true;
                    
                    // Resetear y cerrar modal también en caso de error
                    setTimeout(() => {
                        const dialog = document.querySelector('vaadin-dialog-overlay');
                        if (dialog) {
                            // Resetear botón
                            const exportButton = dialog.querySelector('vaadin-button[theme*="primary"]');
                            if (exportButton) {
                                exportButton.disabled = false;
                                exportButton.textContent = 'Export Data';
                            }
                            
                            // Ocultar progreso
                            const progressSection = dialog.querySelector('.progress-section');
                            if (progressSection) {
                                progressSection.style.display = 'none';
                            }
                            
                            // Cerrar
                            const closeButton = dialog.querySelector('vaadin-button[theme*="tertiary"]');
                            if (closeButton) {
                                closeButton.click();
                            }
                        }
                    }, 500);
                });
                """, exportUrl);
            
            // ✅ SOLUCIÓN: Polling para verificar cuando JavaScript termine y resetear estado Java
            UI.getCurrent().getPage().executeJs("""
                return window.exportCompleted || false;
                """).then(Boolean.class, completed -> {
                if (completed != null && completed) {
                    // ✅ JavaScript terminó, resetear estado Java
                    UI.getCurrent().access(() -> {
                        resetExportState();
                    });
                } else {
                    // ✅ Seguir verificando cada 500ms hasta que termine
                    checkExportCompletion();
                }
            });
            
        } catch (Exception e) {
            logger.error("❌ Error iniciando exportación", e);
            handleExportError(e);
        }
    }
    
    // ✅ MÉTODO AUXILIAR: Verificar cuando JavaScript termine
    private void checkExportCompletion() {
        UI.getCurrent().getPage().executeJs("""
            return window.exportCompleted || false;
            """).then(Boolean.class, completed -> {
            if (completed != null && completed) {
                UI.getCurrent().access(() -> {
                    resetExportState();
                });
            } else {
                // Continuar verificando si no ha pasado mucho tiempo
                if (System.currentTimeMillis() - exportStartTime < 30000) { // 30 segundos max
                    UI.getCurrent().getPage().executeJs("""
                        setTimeout(() => {
                            // Trigger next check
                        }, 500);
                        """).then(result -> checkExportCompletion());
                } else {
                    // Timeout - resetear por seguridad
                    UI.getCurrent().access(() -> {
                        resetExportState();
                    });
                }
            }
        });
    }
    
    // ✅ MÉTODO AUXILIAR: Resetear estado Java completamente
    private void resetExportState() {
        setExportingState(false);
        showProgressSection(false);
        
        // Limpiar variable de JavaScript
        UI.getCurrent().getPage().executeJs("delete window.exportCompleted;");
        
        logger.info("✅ Estado de exportación reseteado completamente");
    }
    /**
     * Builds the export URL based on the current dialog selections.
     */

    // ✅ MÉTODO PARA CONSTRUIR URLS CORREGIDO - LÓGICA COMPLETA
    private String buildExportUrl() {
        String baseUrl = "/vaadin-export";
        String type = typeSelector.getValue();
        String format = formatSelector.getValue();
        String period = mapPeriodToString(periodSelector.getValue());
        
        logger.info("🔗 Construyendo URL - Type: {}, Format: {}, Period: {}", type, format, period);
        
        // ✅ CORREGIDO: Lógica clara y sin errores
        String endpoint = "";
        
        switch (type) {
            case "System Metrics" -> {
                switch (format) {
                    case "CSV" -> endpoint = baseUrl + "/csv/metrics?period=" + period;
                    case "EXCEL" -> endpoint = baseUrl + "/excel/analysis?period=" + period;
                    case "PDF" -> endpoint = baseUrl + "/pdf/complete-report?period=" + period; // ✅ PDF para métricas
                    case "JSON" -> endpoint = baseUrl + "/csv/metrics?period=" + period; // Fallback
                    default -> endpoint = baseUrl + "/csv/metrics?period=" + period;
                }
            }
            case "Process Data" -> {
                String filter = processFilterSelector.getValue();
                switch (format) {
                    case "CSV" -> endpoint = baseUrl + "/csv/processes?filter=" + filter;
                    case "EXCEL" -> endpoint = baseUrl + "/csv/processes?filter=" + filter; // No hay Excel específico para procesos
                    case "PDF" -> endpoint = baseUrl + "/pdf/complete-report?period=" + period + "&processFilter=" + filter;
                    case "JSON" -> endpoint = baseUrl + "/csv/processes?filter=" + filter; // Fallback
                    default -> endpoint = baseUrl + "/csv/processes?filter=" + filter;
                }
            }
            case "Complete Report" -> {
                switch (format) {
                    case "CSV" -> endpoint = baseUrl + "/csv/metrics?period=" + period;
                    case "EXCEL" -> endpoint = baseUrl + "/excel/analysis?period=" + period;
                    case "PDF" -> endpoint = baseUrl + "/pdf/complete-report?period=" + period;
                    case "JSON" -> endpoint = baseUrl + "/csv/metrics?period=" + period; // Fallback
                    default -> endpoint = baseUrl + "/pdf/complete-report?period=" + period;
                }
            }
            case "Custom Export" -> endpoint = baseUrl + "/csv/metrics?period=" + period;
            default -> endpoint = baseUrl + "/csv/metrics?period=" + period;
        }
        
        // Agregar parámetros adicionales
        StringBuilder urlBuilder = new StringBuilder(endpoint);
        
        // Agregar título del reporte si está especificado
        if (reportTitleField.getValue() != null && !reportTitleField.getValue().trim().isEmpty()) {
            urlBuilder.append(endpoint.contains("?") ? "&" : "?")
                     .append("reportTitle=").append(encodeURIComponent(reportTitleField.getValue().trim()));
        }
        
        // Agregar parámetros de fechas personalizadas si están seleccionadas
        if ("Custom Range".equals(periodSelector.getValue()) && 
            startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            
            urlBuilder.append(endpoint.contains("?") ? "&" : "?")
                     .append("startDate=").append(startDatePicker.getValue().toString())
                     .append("&endDate=").append(endDatePicker.getValue().toString());
        }
        
        // Agregar parámetros de opciones avanzadas SOLO para PDF
        if (format.equals("PDF")) {
            if (includeChartsCheckbox.getValue()) {
                urlBuilder.append(urlBuilder.toString().contains("?") ? "&" : "?")
                         .append("includeCharts=true");
            }
            
            if (includeExecutiveSummaryCheckbox.getValue()) {
                urlBuilder.append(urlBuilder.toString().contains("?") ? "&" : "?")
                         .append("includeExecutiveSummary=true");
            }
            
            if (includeDetailedAnalysisCheckbox.getValue()) {
                urlBuilder.append(urlBuilder.toString().contains("?") ? "&" : "?")
                         .append("includeDetailedAnalysis=true");
            }
        }
        
        String finalUrl = urlBuilder.toString();
        logger.info("🔗 URL final construida: {}", finalUrl);
        return finalUrl;
    }

    /**
     * Encodes a string for use in a URL query parameter.
     */
    private String encodeURIComponent(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }
    
    private ExportRequest buildExportRequest() {
        ExportRequest request = new ExportRequest();
        
        // Tipo y formato
        request.setType(mapTypeToEnum(typeSelector.getValue()));
        request.setFormat(ExportRequest.ExportFormat.valueOf(formatSelector.getValue()));
        
        // Fechas
        if ("Custom Range".equals(periodSelector.getValue())) {
            request.setStartDate(startDatePicker.getValue());
            request.setEndDate(endDatePicker.getValue());
        } else {
            request.setPeriod(mapPeriodToString(periodSelector.getValue()));
        }
        
        // Opciones
        request.setIncludeCharts(includeChartsCheckbox.getValue());
        request.setIncludeExecutiveSummary(includeExecutiveSummaryCheckbox.getValue());
        request.setIncludeDetailedAnalysis(includeDetailedAnalysisCheckbox.getValue());
        request.setReportTitle(reportTitleField.getValue());
        
        // Filtro de procesos
        if (processFilterSelector.isVisible()) {
            request.setProcessFilter(processFilterSelector.getValue());
        }
        
        return request;
    }
    
    private ExportRequest.ExportType mapTypeToEnum(String type) {
        return switch (type) {
            case "System Metrics" -> ExportRequest.ExportType.METRICS;
            case "Process Data" -> ExportRequest.ExportType.PROCESSES;
            case "Complete Report" -> ExportRequest.ExportType.COMPLETE_REPORT;
            case "Custom Export" -> ExportRequest.ExportType.CUSTOM;
            default -> ExportRequest.ExportType.METRICS;
        };
    }
    
    private String mapPeriodToString(String period) {
        return switch (period) {
            case "Last Hour" -> "1H";
            case "Last 6 Hours" -> "6H";
            case "Last 24 Hours" -> "24H";
            case "Last 7 Days" -> "7D";
            case "Last 30 Days" -> "30D";
            default -> "24H";
        };
    }
    
    private void handleExportResult(ExportResult result) {
        setExportingState(false);
        showProgressSection(false);
        
        if (result.isSuccess()) {
            logger.info("✅ Export exitoso: {}", result.getFilename());
            
            // ✅ CORREGIDO: Crear blob en el navegador y descargar
            triggerDownload(result);
            
            Notification.show(
                "✅ Export completed: " + result.getFilename() + " (" + result.getFormattedSize() + ")",
                3000, 
                Notification.Position.TOP_END
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            close();
        } else {
            logger.error("❌ Error en exportación: {}", result.getErrorMessage());
            Notification.show(
                "❌ Export failed: " + result.getErrorMessage(),
                5000, 
                Notification.Position.TOP_END
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void triggerDownload(ExportResult result) {
        logger.info("🔽 Iniciando descarga: {} - {}", result.getFilename(), result.getFormattedSize());
        
        // ✅ CORREGIDO: Usar JavaScript para crear blob y descargar
        UI.getCurrent().getPage().executeJs("""
            console.log('🔽 Iniciando descarga:', $1);
            try {
                // Convertir datos a Uint8Array
                const byteArray = new Uint8Array($0);
                
                // Crear blob con tipo MIME correcto
                const blob = new Blob([byteArray], { type: $2 });
                
                // Crear URL temporal
                const url = URL.createObjectURL(blob);
                
                // Crear enlace de descarga
                const link = document.createElement('a');
                link.href = url;
                link.download = $1;
                link.style.display = 'none';
                
                // Ejecutar descarga
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                
                // Limpiar URL temporal
                URL.revokeObjectURL(url);
                
                console.log('✅ Descarga completada exitosamente');
            } catch (error) {
                console.error('❌ Error en descarga:', error);
                alert('Error downloading file: ' + error.message);
            }
            """, 
            result.getData(), 
            result.getFilename(), 
            result.getMimeType()
        );
    }
    
    private void handleExportError(Throwable error) {
        setExportingState(false);
        showProgressSection(false);
        
        logger.error("❌ Error en exportación", error);
        
        Notification.show(
            "❌ Export error: " + error.getMessage(),
            5000, 
            Notification.Position.TOP_END
        ).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
    
    private void setExportingState(boolean exporting) {
        this.isExporting = exporting;
        if (exporting) {
        this.exportStartTime = System.currentTimeMillis();
    }
        exportButton.setEnabled(!exporting);
        exportButton.setText(exporting ? "Exporting..." : "Export Data");
        
        if (exporting) {
            exportButton.setIcon(VaadinIcon.CLOCK.create());
            progressLabel.setText("Processing export...");
        } else {
            exportButton.setIcon(VaadinIcon.DOWNLOAD.create());
        }
    }
    
    private void showProgressSection(boolean show) {
        if (progressSection != null) {
            progressSection.setVisible(show);
        }
    }
}