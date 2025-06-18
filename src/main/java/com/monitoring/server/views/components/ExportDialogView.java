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
 * 🎨 ExportDialogView - VERSIÓN FINAL ESTABLE
 * ✅ Sin @VaadinSessionScope para evitar problemas de inicialización
 * ✅ Cada DashboardView crea su propia instancia
 * ✅ Sistema de exportación funcional y estable
 * ✅ Sin problemas de concurrencia
 */
@Component
public class ExportDialogView extends Dialog {

    private static final Logger logger = LoggerFactory.getLogger(ExportDialogView.class);
    
    // ✅ Inyección de servicio
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
    
    // Estado individual
    private boolean isExporting = false;
    private long exportStartTime;
    private String instanceId;
    
    public ExportDialogView() {
        // ID único para esta instancia
        this.instanceId = "instance-" + System.currentTimeMillis() + "-" + hashCode();
        
        logger.info("🔧 Creando ExportDialogView - ID: {}", instanceId);
        
        initializeDialog();
        createLayout();
        configureEvents();
        
        // ✅ CONFIGURACIÓN ESTABLE DE PERSISTENCIA
        getElement().executeJs("""
            // Configurar persistencia básica
            this._persistent = true;
            this._instanceId = $0;
            this._readyToReopen = true;
            
            // Prevenir auto-destrucción
            this.addEventListener('vaadin-overlay-close', (e) => {
                e.preventDefault();
                this.opened = false;
            });
            
            // Configurar para múltiples usos
            this.addEventListener('opened-changed', (e) => {
                if (!e.detail.value) {
                    setTimeout(() => {
                        this._readyToReopen = true;
                        console.log('✅ ExportDialog instancia ' + $0 + ' listo para reabrir');
                    }, 100);
                }
            });
            
            console.log('✅ ExportDialogView inicializado - Instancia: ' + $0);
        """, instanceId);
    }
    
    private void initializeDialog() {
        setHeaderTitle("📊 Export System Data");
        setWidth("700px");
        setHeight("600px");
        setModal(true);
        setDraggable(true);
        setResizable(false);
        
        addClassName("export-dialog");
        addClassName("export-dialog-" + instanceId);
    }
    
    private void createLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        
        // === SECCIONES DEL DIALOG ===
        mainLayout.add(createTypeSelectionSection());
        mainLayout.add(createFormatConfigSection());
        mainLayout.add(createTimeFiltersSection());
        mainLayout.add(createAdvancedOptionsSection());
        
        // === PROGRESO Y ACCIONES ===
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
        
        HorizontalLayout customDateLayout = new HorizontalLayout();
        customDateLayout.setWidthFull();
        customDateLayout.setAlignItems(FlexComponent.Alignment.END);
        customDateLayout.setVisible(false);
        
        startDatePicker = new DateTimePicker("Start Date");
        startDatePicker.setValue(LocalDateTime.now().minusDays(1));
        startDatePicker.setWidth("280px");
        
        endDatePicker = new DateTimePicker("End Date");
        endDatePicker.setValue(LocalDateTime.now());
        endDatePicker.setWidth("280px");
        
        customDateLayout.add(startDatePicker, endDatePicker);
        
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
        typeSelector.addValueChangeListener(event -> {
            boolean showProcessFilter = "Process Data".equals(event.getValue()) || 
                                      "Complete Report".equals(event.getValue());
            processFilterSelector.setVisible(showProcessFilter);
        });
        
        formatSelector.addValueChangeListener(event -> {
            String format = event.getValue();
            boolean supportsCharts = "PDF".equals(format) || "EXCEL".equals(format);
            includeChartsCheckbox.setEnabled(supportsCharts);
            
            if (!supportsCharts) {
                includeChartsCheckbox.setValue(false);
            }
        });
    }
    
    // === MÉTODOS PÚBLICOS ===
    
    public void openForMetrics() {
        logger.info("🎯 Abriendo para métricas - Instancia: {}", instanceId);
        typeSelector.setValue("System Metrics");
        formatSelector.setValue("CSV");
        periodSelector.setValue("Last 24 Hours");
        open();
    }
    
    public void openForProcesses() {
        logger.info("🎯 Abriendo para procesos - Instancia: {}", instanceId);
        typeSelector.setValue("Process Data");
        formatSelector.setValue("EXCEL");
        periodSelector.setValue("Last Hour");
        open();
    }
    
    public void openForCompleteReport() {
        logger.info("🎯 Abriendo para reporte completo - Instancia: {}", instanceId);
        typeSelector.setValue("Complete Report");
        formatSelector.setValue("PDF");
        periodSelector.setValue("Last 24 Hours");
        includeChartsCheckbox.setValue(true);
        includeExecutiveSummaryCheckbox.setValue(true);
        open();
    }

    // === ✅ MÉTODO startExport ESTABLE ===
    
    private void startExport() {
        if (isExporting) {
            logger.warn("⚠️ Export ya en progreso para instancia: {}", instanceId);
            return;
        }
        
        try {
            logger.info("🚀 Iniciando export para instancia: {}", instanceId);
            setExportingState(true);
            showProgressSection(true);
            
            String exportUrl = buildExportUrl();
            logger.info("🔗 URL construida para instancia {}: {}", instanceId, exportUrl);
            
            // ✅ JavaScript estable con instanceId único
            UI.getCurrent().getPage().executeJs("""
                console.log('🚀 Iniciando descarga para instancia ' + $1 + ':', $0);
                
                fetch($0, {
                    method: 'GET',
                    credentials: 'include'
                })
                .then(response => {
                    console.log('Response status para instancia ' + $1 + ':', response.status);
                    if (response.ok) {
                        return response.blob();
                    } else {
                        throw new Error('Export failed: ' + response.status);
                    }
                })
                .then(blob => {
                    console.log('✅ Blob recibido para instancia ' + $1 + ':', blob.size, 'bytes');
                    
                    const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-');
                    let extension = '.csv';
                    
                    if ($0.includes('/pdf/')) extension = '.pdf';
                    else if ($0.includes('/excel/')) extension = '.xlsx';
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
                    
                    console.log('✅ Descarga completada para instancia ' + $1 + ':', filename);
                    
                    // Variable única por instancia
                    window['exportCompleted_' + $1] = true;
                    
                    // Resetear UI específica
                    setTimeout(() => {
                        const dialog = document.querySelector('.export-dialog-' + $1);
                        if (dialog) {
                            const exportButton = dialog.querySelector('vaadin-button[theme*="primary"]');
                            if (exportButton) {
                                exportButton.disabled = false;
                                exportButton.textContent = 'Export Data';
                            }
                            
                            const progressSection = dialog.querySelector('.progress-section');
                            if (progressSection) {
                                progressSection.style.display = 'none';
                            }
                            
                            const closeButton = dialog.querySelector('vaadin-button[theme*="tertiary"]');
                            if (closeButton) {
                                closeButton.click();
                                console.log('✅ Modal cerrado para instancia ' + $1);
                            }
                        }
                        
                        // Notificación
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
                    console.error('❌ Error en descarga para instancia ' + $1 + ':', error);
                    alert('Error downloading file: ' + error.message);
                    
                    window['exportCompleted_' + $1] = true;
                    
                    setTimeout(() => {
                        const dialog = document.querySelector('.export-dialog-' + $1);
                        if (dialog) {
                            const exportButton = dialog.querySelector('vaadin-button[theme*="primary"]');
                            if (exportButton) {
                                exportButton.disabled = false;
                                exportButton.textContent = 'Export Data';
                            }
                            
                            const progressSection = dialog.querySelector('.progress-section');
                            if (progressSection) {
                                progressSection.style.display = 'none';
                            }
                            
                            const closeButton = dialog.querySelector('vaadin-button[theme*="tertiary"]');
                            if (closeButton) {
                                closeButton.click();
                            }
                        }
                    }, 500);
                });
                """, exportUrl, instanceId);
            
            // Polling específico por instancia
            checkExportCompletion();
            
        } catch (Exception e) {
            logger.error("❌ Error iniciando exportación para instancia {}: {}", instanceId, e.getMessage(), e);
            handleExportError(e);
        }
    }
    
    private void checkExportCompletion() {
        UI.getCurrent().getPage().executeJs("""
            return window['exportCompleted_' + $0] || false;
            """, instanceId).then(Boolean.class, completed -> {
            if (completed != null && completed) {
                UI.getCurrent().access(() -> {
                    resetExportState();
                });
            } else {
                if (System.currentTimeMillis() - exportStartTime < 30000) {
                    UI.getCurrent().getPage().executeJs("""
                        setTimeout(() => {
                            // Trigger next check
                        }, 500);
                        """).then(result -> checkExportCompletion());
                } else {
                    UI.getCurrent().access(() -> {
                        resetExportState();
                    });
                }
            }
        });
    }
    
    private void resetExportState() {
        setExportingState(false);
        showProgressSection(false);
        
        UI.getCurrent().getPage().executeJs("delete window['exportCompleted_' + $0];", instanceId);
        getElement().executeJs("this._readyToReopen = true;");
        
        logger.info("✅ Estado de exportación reseteado para instancia: {}", instanceId);
    }

    // === MÉTODOS DE UTILIDAD ===
    
    private String buildExportUrl() {
        String baseUrl = "/vaadin-export";
        String type = typeSelector.getValue();
        String format = formatSelector.getValue();
        String period = mapPeriodToString(periodSelector.getValue());
        
        String endpoint = "";
        
        switch (type) {
            case "System Metrics" -> {
                switch (format) {
                    case "CSV" -> endpoint = baseUrl + "/csv/metrics?period=" + period;
                    case "EXCEL" -> endpoint = baseUrl + "/excel/analysis?period=" + period;
                    case "PDF" -> endpoint = baseUrl + "/pdf/complete-report?period=" + period;
                    default -> endpoint = baseUrl + "/csv/metrics?period=" + period;
                }
            }
            case "Process Data" -> {
                String filter = processFilterSelector.getValue();
                switch (format) {
                    case "CSV" -> endpoint = baseUrl + "/csv/processes?filter=" + filter;
                    case "EXCEL" -> endpoint = baseUrl + "/csv/processes?filter=" + filter;
                    case "PDF" -> endpoint = baseUrl + "/pdf/complete-report?period=" + period + "&processFilter=" + filter;
                    default -> endpoint = baseUrl + "/csv/processes?filter=" + filter;
                }
            }
            case "Complete Report" -> {
                switch (format) {
                    case "CSV" -> endpoint = baseUrl + "/csv/metrics?period=" + period;
                    case "EXCEL" -> endpoint = baseUrl + "/excel/analysis?period=" + period;
                    case "PDF" -> endpoint = baseUrl + "/pdf/complete-report?period=" + period;
                    default -> endpoint = baseUrl + "/pdf/complete-report?period=" + period;
                }
            }
            case "Custom Export" -> endpoint = baseUrl + "/csv/metrics?period=" + period;
            default -> endpoint = baseUrl + "/csv/metrics?period=" + period;
        }
        
        StringBuilder urlBuilder = new StringBuilder(endpoint);
        
        if (reportTitleField.getValue() != null && !reportTitleField.getValue().trim().isEmpty()) {
            urlBuilder.append(endpoint.contains("?") ? "&" : "?")
                     .append("reportTitle=").append(encodeURIComponent(reportTitleField.getValue().trim()));
        }
        
        if ("Custom Range".equals(periodSelector.getValue()) && 
            startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            
            urlBuilder.append(endpoint.contains("?") ? "&" : "?")
                     .append("startDate=").append(startDatePicker.getValue().toString())
                     .append("&endDate=").append(endDatePicker.getValue().toString());
        }
        
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
        
        return urlBuilder.toString();
    }

    private String encodeURIComponent(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }
    
    private ExportRequest buildExportRequest() {
        ExportRequest request = new ExportRequest();
        
        request.setType(mapTypeToEnum(typeSelector.getValue()));
        request.setFormat(ExportRequest.ExportFormat.valueOf(formatSelector.getValue()));
        
        if ("Custom Range".equals(periodSelector.getValue())) {
            request.setStartDate(startDatePicker.getValue());
            request.setEndDate(endDatePicker.getValue());
        } else {
            request.setPeriod(mapPeriodToString(periodSelector.getValue()));
        }
        
        request.setIncludeCharts(includeChartsCheckbox.getValue());
        request.setIncludeExecutiveSummary(includeExecutiveSummaryCheckbox.getValue());
        request.setIncludeDetailedAnalysis(includeDetailedAnalysisCheckbox.getValue());
        request.setReportTitle(reportTitleField.getValue());
        
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
            logger.info("✅ Export exitoso para instancia {}: {}", instanceId, result.getFilename());
            
            triggerDownload(result);
            
            Notification.show(
                "✅ Export completed: " + result.getFilename() + " (" + result.getFormattedSize() + ")",
                3000, 
                Notification.Position.TOP_END
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            close();
        } else {
            logger.error("❌ Error en exportación para instancia {}: {}", instanceId, result.getErrorMessage());
            Notification.show(
                "❌ Export failed: " + result.getErrorMessage(),
                5000, 
                Notification.Position.TOP_END
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void triggerDownload(ExportResult result) {
        logger.info("🔽 Iniciando descarga para instancia {}: {} - {}", instanceId, result.getFilename(), result.getFormattedSize());
        
        UI.getCurrent().getPage().executeJs("""
            console.log('🔽 Iniciando descarga para instancia ' + $2 + ':', $1);
            try {
                const byteArray = new Uint8Array($0);
                const blob = new Blob([byteArray], { type: $3 });
                const url = URL.createObjectURL(blob);
                
                const link = document.createElement('a');
                link.href = url;
                link.download = $1;
                link.style.display = 'none';
                
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                URL.revokeObjectURL(url);
                
                console.log('✅ Descarga completada para instancia ' + $2);
            } catch (error) {
                console.error('❌ Error en descarga para instancia ' + $2 + ':', error);
                alert('Error downloading file: ' + error.message);
            }
            """, 
            result.getData(), 
            result.getFilename(), 
            instanceId,
            result.getMimeType()
        );
    }
    
    private void handleExportError(Throwable error) {
        setExportingState(false);
        showProgressSection(false);
        
        logger.error("❌ Error en exportación para instancia {}: {}", instanceId, error.getMessage(), error);
        
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
    
    // ✅ OVERRIDE del método close estable
    @Override
    public void close() {
        logger.info("🔒 Cerrando dialog para instancia: {}", instanceId);
        super.close();
        
        UI.getCurrent().access(() -> {
            setExportingState(false);
            showProgressSection(false);
            
            getElement().executeJs("this._readyToReopen = true;");
            
            logger.info("✅ Dialog cerrado y reseteado para instancia: {}", instanceId);
        });
    }
    
    // ✅ Método para obtener información de la instancia
    public String getInstanceInfo() {
        return "Instance: " + instanceId + ", Ready: " + !isExporting;
    }
}