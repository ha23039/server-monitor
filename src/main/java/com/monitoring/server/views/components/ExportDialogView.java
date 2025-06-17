package com.monitoring.server.views.components;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
 * 🎨 Dialog simplificado para exportaciones - SIN dependencias circulares
 * Utiliza llamadas directas a endpoints en lugar de servicios inyectados
 */
@Component
public class ExportDialogView extends Dialog {

    private static final Logger logger = LoggerFactory.getLogger(ExportDialogView.class);
    
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
    
    // === MÉTODOS DE EXPORTACIÓN SIMPLIFICADOS ===
    
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
            
            // Usar JavaScript para abrir la descarga
            UI.getCurrent().getPage().executeJs("""
                console.log('🚀 Abriendo descarga:', $0);
                
                // Crear un enlace temporal para la descarga
                const link = document.createElement('a');
                link.href = $0;
                link.target = '_blank';
                link.style.display = 'none';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                
                console.log('✅ Descarga iniciada exitosamente');
                """, exportUrl).then(result -> {
                
                // Simular un pequeño delay para dar feedback al usuario
                UI.getCurrent().access(() -> {
                    setExportingState(false);
                    showProgressSection(false);
                    
                    Notification.show(
                        "✅ Export started! Check your downloads folder.",
                        3000, 
                        Notification.Position.TOP_END
                    ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    
                    close();
                });
            });
            
        } catch (Exception e) {
            logger.error("❌ Error iniciando exportación", e);
            handleExportError(e);
        }
    }
    
    private String buildExportUrl() {
        String baseUrl = "/api/export";
        String type = typeSelector.getValue();
        String format = formatSelector.getValue();
        String period = mapPeriodToString(periodSelector.getValue());
        
        // Construir URL según el tipo seleccionado
        String endpoint = switch (type) {
            case "System Metrics" -> {
                if ("CSV".equals(format)) {
                    yield baseUrl + "/csv/metrics?period=" + period;
                } else {
                    yield baseUrl + "/metrics?format=" + format + "&period=" + period;
                }
            }
            case "Process Data" -> {
                String filter = processFilterSelector.getValue();
                if ("CSV".equals(format)) {
                    yield baseUrl + "/csv/processes?filter=" + filter;
                } else {
                    yield baseUrl + "/processes?format=" + format + "&filter=" + filter;
                }
            }
            case "Complete Report" -> {
                if ("PDF".equals(format)) {
                    yield baseUrl + "/pdf/complete-report?period=" + period;
                } else if ("EXCEL".equals(format)) {
                    yield baseUrl + "/excel/analysis?period=" + period;
                } else {
                    yield baseUrl + "/complete-report?format=" + format + "&period=" + period;
                }
            }
            default -> baseUrl + "/metrics?format=CSV&period=24H";
        };
        
        // Agregar parámetros adicionales
        StringBuilder urlBuilder = new StringBuilder(endpoint);
        
        // Agregar título del reporte si está especificado
        if (reportTitleField.getValue() != null && !reportTitleField.getValue().trim().isEmpty()) {
            urlBuilder.append(endpoint.contains("?") ? "&" : "?")
                     .append("reportTitle=").append(reportTitleField.getValue().trim());
        }
        
        // Agregar parámetros de fechas personalizadas si están seleccionadas
        if ("Custom Range".equals(periodSelector.getValue()) && 
            startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            
            urlBuilder.append(endpoint.contains("?") ? "&" : "?")
                     .append("startDate=").append(startDatePicker.getValue().toString())
                     .append("&endDate=").append(endDatePicker.getValue().toString());
        }
        
        // Agregar parámetros de opciones avanzadas
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
        
        return urlBuilder.toString();
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
        exportButton.setEnabled(!exporting);
        exportButton.setText(exporting ? "Exporting..." : "Export Data");
        
        if (exporting) {
            exportButton.setIcon(VaadinIcon.CLOCK.create());
            progressLabel.setText("Preparing download...");
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