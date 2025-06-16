package com.monitoring.server.views.components;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

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
 * 🎨 Dialog avanzado para configurar y ejecutar exportaciones
 * Incluye filtros, vista previa y descarga automática
 */
@Component
public class ExportDialogView extends Dialog {

    @Autowired
    public void setExportService(ExportService exportService) {
        this.exportService = exportService;
        System.out.println("✅ ExportService inyectado en ExportDialogView");
    }

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
    
    // Estado
    private boolean isExporting = false;
    private CompletableFuture<ExportResult> currentExport;
    
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
        
        // Agregar clase CSS para estilización
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
        mainLayout.add(createProgressSection());
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
        formatSelector.setItems("CSV", "PDF", "EXCEL", "JSON");
        formatSelector.setValue("CSV");
        formatSelector.setWidth("200px");
        formatSelector.setItemLabelGenerator(format -> {
            return switch (format) {
                case "CSV" -> "📊 CSV - Data Analysis";
                case "PDF" -> "📄 PDF - Professional Report";
                case "EXCEL" -> "📈 Excel - Advanced Spreadsheet";
                case "JSON" -> "🔧 JSON - API Integration";
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
    
    // === MÉTODOS DE EXPORTACIÓN ===
    
    private void startExport() {
        if (isExporting) {
            return;
        }
        
        try {
            ExportRequest request = buildExportRequest();
            
            setExportingState(true);
            showProgressSection(true);
            
            // Ejecutar exportación según el tipo
            currentExport = switch (typeSelector.getValue()) {
                case "System Metrics" -> exportService.exportSystemMetrics(request);
                case "Process Data" -> exportService.exportProcessData(request);
                case "Complete Report" -> exportService.exportCompleteReport(request);
                case "Custom Export" -> exportService.exportCustomData(request);
                default -> exportService.exportSystemMetrics(request);
            };
            
            // Manejar resultado
            currentExport.thenAccept(result -> {
                UI.getCurrent().access(() -> {
                    handleExportResult(result);
                });
            }).exceptionally(throwable -> {
                UI.getCurrent().access(() -> {
                    handleExportError(throwable);
                });
                return null;
            });
            
        } catch (Exception e) {
            handleExportError(e);
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
            // Crear descarga
            triggerDownload(result);
            
            Notification.show(
                String.format("✅ Export completed! File: %s (%s)", 
                    result.getFilename(), result.getFormattedSize()),
                3000, 
                Notification.Position.TOP_END
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            close();
        } else {
            Notification.show(
                "❌ Export failed: " + result.getErrorMessage(),
                5000, 
                Notification.Position.TOP_END
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void handleExportError(Throwable error) {
        setExportingState(false);
        showProgressSection(false);
        
        Notification.show(
            "❌ Export error: " + error.getMessage(),
            5000, 
            Notification.Position.TOP_END
        ).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
    
    private void triggerDownload(ExportResult result) {
        // Crear StreamResource para la descarga
        getElement().executeJs("""
            const blob = new Blob([new Uint8Array($0)], { type: $1 });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = $2;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
        """, result.getData(), result.getMimeType(), result.getFilename());
    }
    
    private void setExportingState(boolean exporting) {
        this.isExporting = exporting;
        exportButton.setEnabled(!exporting);
        exportButton.setText(exporting ? "Exporting..." : "Export Data");
        
        if (exporting) {
            exportButton.setIcon(VaadinIcon.CLOCK.create());
        } else {
            exportButton.setIcon(VaadinIcon.DOWNLOAD.create());
        }
    }
    
    private void showProgressSection(boolean show) {
        getChildren()
            .filter(component -> component.getClass().getSimpleName().contains("VerticalLayout"))
            .map(component -> (VerticalLayout) component)
            .forEach(layout -> {
                layout.getChildren()
                    .filter(child -> child.getClassNames().contains("progress-section"))
                    .forEach(child -> child.setVisible(show));
            });
    }
}