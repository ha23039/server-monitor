package com.monitoring.server.views.dashboard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.data.entity.AlertConfiguration;
import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.security.SecurityAnnotations.RequiresViewer;
import com.monitoring.server.service.interfaces.AlertConfigService;
import com.monitoring.server.service.interfaces.ProcessInfoService;
import com.monitoring.server.service.interfaces.SystemMonitorService;
import com.monitoring.server.views.MainLayout;
import com.monitoring.server.views.components.AlertBanner;
import com.monitoring.server.views.components.MetricProgressBar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * 🚀 DASHBOARD ULTRA PRO - VERSIÓN SIMPLIFICADA FUNCIONAL
 * Sin dependencias externas - 100% funcional
 */
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("🚀 Enterprise Dashboard - Métricas en Tiempo Real")
@RequiresViewer
public class DashboardView extends VerticalLayout {

    // Services
    private final SystemMonitorService monitorService;
    private final ProcessInfoService processInfoService;
    private final AlertConfigService alertConfigService;
    
    // Componentes principales
    private MetricProgressBar cpuProgressBar;
    private MetricProgressBar memoryProgressBar;
    private MetricProgressBar diskProgressBar;
    private Div realtimeChart;
    private Grid<ProcessInfo> processGrid;
    private AlertBanner alertBanner;
    
    // Controles
    private String selectedTimeRange = "1H";
    private String selectedProcessFilter = "ALL";
    private Tabs periodTabs;
    private Select<String> processFilterSelect;
    private Button exportButton;
    private Button fullscreenButton;
    
    // Estado
    private Span realtimeStatus;
    private Span lastUpdateTime;
    private Span performanceIndicator;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Autowired
    public DashboardView(SystemMonitorService monitorService,
                         ProcessInfoService processInfoService,
                         AlertConfigService alertConfigService) {
        this.monitorService = monitorService;
        this.processInfoService = processInfoService;
        this.alertConfigService = alertConfigService;
        
        initializeUltraProDashboard();
    }
    
    private void initializeUltraProDashboard() {
        addClassName("ultra-dashboard");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        // Aplicar estilos ultra directamente
        setupUltraStyles();
        
        // Crear componentes
        createUltraComponents();
        
        // Estructurar dashboard
        buildDashboardStructure();
        
        // Cargar datos iniciales
        loadInitialData();
        
        // Configurar tiempo real
        setupRealtimeUpdates();
    }
    
    private void setupUltraStyles() {
        getStyle()
            .set("background", "linear-gradient(135deg, #0f1419 0%, #1a202c 50%, #2d3748 100%)")
            .set("min-height", "100vh")
            .set("color", "#F7FAFC")
            .set("font-family", "'Inter', -apple-system, BlinkMacSystemFont, sans-serif");
    }
    
    private void createUltraComponents() {
        // Banner de alertas
        alertBanner = createUltraAlertBanner();
        
        // Tarjetas de métricas ultra
        AlertConfiguration config = alertConfigService.getCurrentConfig();
        cpuProgressBar = createUltraMetricCard("🖥️ CPU", config.getCpuThreshold(), "#4F46E5");
        memoryProgressBar = createUltraMetricCard("💾 RAM", config.getMemoryThreshold(), "#10B981");
        diskProgressBar = createUltraMetricCard("💽 Disco", config.getDiskThreshold(), "#F59E0B");
        
        // Gráfico ultra simplificado
        realtimeChart = createUltraChart();
        
        // Grid ultra pro
        processGrid = createUltraProcessGrid();
        
        // Controles avanzados
        createUltraControls();
        
        // Indicadores de estado
        createStatusIndicators();
    }
    
    private AlertBanner createUltraAlertBanner() {
        AlertBanner banner = new AlertBanner();
        banner.setVisible(false);
        banner.getStyle()
            .set("background", "linear-gradient(135deg, rgba(239, 68, 68, 0.9), rgba(220, 38, 38, 0.8))")
            .set("border-radius", "16px")
            .set("padding", "1rem 1.5rem")
            .set("border", "1px solid rgba(239, 68, 68, 0.3)")
            .set("backdrop-filter", "blur(10px)")
            .set("box-shadow", "0 8px 32px rgba(239, 68, 68, 0.2)")
            .set("color", "white")
            .set("font-weight", "600")
            .set("margin-bottom", "1.5rem");
        return banner;
    }
    
    private MetricProgressBar createUltraMetricCard(String title, double threshold, String accentColor) {
        MetricProgressBar card = new MetricProgressBar(title, 0, threshold);
        
        // Aplicar estilos ultra glass
        card.getStyle()
            .set("background", "linear-gradient(135deg, rgba(255,255,255,0.1), rgba(255,255,255,0.05))")
            .set("border-radius", "20px")
            .set("padding", "2rem")
            .set("border", "1px solid rgba(255,255,255,0.1)")
            .set("backdrop-filter", "blur(20px)")
            .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)")
            .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
            .set("cursor", "pointer")
            .set("min-height", "200px");
        
        // Efectos hover ultra
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                .set("transform", "translateY(-8px)")
                .set("box-shadow", "0 16px 48px rgba(0,0,0,0.2)")
                .set("border-color", accentColor);
        });
        
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)")
                .set("border-color", "rgba(255,255,255,0.1)");
        });
        
        return card;
    }
    
    private Div createUltraChart() {
        Div chart = new Div();
        chart.addClassName("ultra-chart");
        chart.setHeight("400px");
        chart.setWidth("100%");
        
        chart.getStyle()
            .set("background", "linear-gradient(135deg, rgba(255,255,255,0.05), rgba(255,255,255,0.02))")
            .set("border-radius", "16px")
            .set("padding", "2rem")
            .set("border", "1px solid rgba(255,255,255,0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("position", "relative")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "center")
            .set("align-items", "center");
        
        // Contenido del gráfico ultra
        VerticalLayout chartContent = new VerticalLayout();
        chartContent.setPadding(false);
        chartContent.setSpacing(true);
        chartContent.setAlignItems(FlexComponent.Alignment.CENTER);
        
        H2 chartTitle = new H2("📈 Métricas en Tiempo Real");
        chartTitle.getStyle()
            .set("margin", "0 0 2rem 0")
            .set("color", "#F9FAFB")
            .set("text-align", "center");
        
        Div metricsDisplay = new Div();
        metricsDisplay.setId("metrics-display");
        metricsDisplay.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(3, 1fr)")
            .set("gap", "2rem")
            .set("width", "100%")
            .set("max-width", "600px");
        
        chartContent.add(chartTitle, metricsDisplay);
        chart.add(chartContent);
        
        return chart;
    }
    
    private Grid<ProcessInfo> createUltraProcessGrid() {
        Grid<ProcessInfo> grid = new Grid<>(ProcessInfo.class, false);
        grid.addClassName("ultra-process-grid");
        
        // Estilos ultra
        grid.setAllRowsVisible(true);
        grid.setMaxHeight("400px");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        
        grid.getStyle()
            .set("background", "rgba(255, 255, 255, 0.02)")
            .set("border-radius", "12px")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("--lumo-font-size-s", "0.875rem")
            .set("color", "#F9FAFB");
        
        // Columnas ultra
        setupUltraGridColumns(grid);
        
        return grid;
    }
    
    private void setupUltraGridColumns(Grid<ProcessInfo> grid) {
        // PID
        grid.addColumn(new ComponentRenderer<>(process -> {
            Span span = new Span("🔢 " + process.getProcessId());
            span.getStyle().set("font-weight", "600");
            return span;
        })).setHeader("PID").setWidth("100px").setFlexGrow(0);
        
        // Proceso con icono
        grid.addColumn(new ComponentRenderer<>(process -> {
            String icon = getProcessIcon(process.getProcessName());
            Span span = new Span(icon + " " + process.getProcessName());
            span.getStyle().set("font-weight", "600");
            return span;
        })).setHeader("⚙️ Proceso").setWidth("200px").setFlexGrow(1);
        
        // Usuario
        grid.addColumn(new ComponentRenderer<>(process -> {
            Span span = new Span("👤 " + (process.getUsername() != null ? process.getUsername() : "N/A"));
            return span;
        })).setHeader("Usuario").setWidth("120px").setFlexGrow(0);
        
        // Estado con badge
        grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
            .setHeader("📊 Estado").setWidth("120px").setFlexGrow(0);
        
        // CPU con barra
        grid.addColumn(new ComponentRenderer<>(process -> 
            createMetricBar(process.getCpuUsage(), "#4F46E5")))
            .setHeader("🖥️ CPU (%)").setWidth("140px").setFlexGrow(0);
        
        // Memoria con barra
        grid.addColumn(new ComponentRenderer<>(process -> 
            createMetricBar(process.getMemoryUsage(), "#10B981")))
            .setHeader("💾 RAM (%)").setWidth("140px").setFlexGrow(0);
        
        // Disco I/O
        grid.addColumn(new ComponentRenderer<>(process -> {
            Span span = new Span("💽 " + String.format("%.1f KB/s", process.getDiskUsage()));
            span.getStyle().set("font-family", "monospace");
            return span;
        })).setHeader("Disco I/O").setWidth("130px").setFlexGrow(0);
    }
    
    private void createUltraControls() {
        // Selector de período
        periodTabs = new Tabs();
        periodTabs.add(new Tab("1H"), new Tab("24H"), new Tab("7D"), new Tab("1M"));
        periodTabs.getStyle()
            .set("background", "rgba(255, 255, 255, 0.1)")
            .set("border-radius", "8px")
            .set("padding", "0.25rem");
        
        periodTabs.addSelectedChangeListener(event -> {
            selectedTimeRange = event.getSelectedTab().getLabel();
            updateChart();
        });
        
        // Filtro de procesos
        processFilterSelect = new Select<>();
        processFilterSelect.setItems("ALL", "HIGH_CPU", "HIGH_MEMORY", "SYSTEM", "USER");
        processFilterSelect.setValue("ALL");
        processFilterSelect.setLabel("🔍 Filtrar Procesos");
        styleUltraSelect(processFilterSelect);
        
        processFilterSelect.addValueChangeListener(event -> {
            selectedProcessFilter = event.getValue();
            updateProcessData();
        });
        
        // Botones de acción
        exportButton = new Button("📊 Exportar", VaadinIcon.DOWNLOAD.create());
        exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        styleUltraButton(exportButton, "#4F46E5");
        exportButton.addClickListener(e -> showExportDialog());
        
        fullscreenButton = new Button("⛶ Pantalla Completa", VaadinIcon.EXPAND_SQUARE.create());
        fullscreenButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        styleUltraButton(fullscreenButton, "#059669");
        fullscreenButton.addClickListener(e -> toggleFullscreen());
    }
    
    private void createStatusIndicators() {
        realtimeStatus = new Span("🟢 Tiempo Real Activo");
        realtimeStatus.getStyle()
            .set("background", "linear-gradient(135deg, #10B981, #059669)")
            .set("padding", "0.5rem 1rem")
            .set("border-radius", "20px")
            .set("font-weight", "600")
            .set("font-size", "0.875rem")
            .set("color", "white")
            .set("box-shadow", "0 4px 14px rgba(16, 185, 129, 0.3)");
        
        lastUpdateTime = new Span("⏱️ Cargando...");
        lastUpdateTime.getStyle()
            .set("color", "#9CA3AF")
            .set("font-size", "0.9rem");
        
        performanceIndicator = new Span("📊 98%");
        performanceIndicator.getStyle()
            .set("background", "rgba(79, 70, 229, 0.2)")
            .set("padding", "0.5rem")
            .set("border-radius", "8px")
            .set("font-weight", "600")
            .set("color", "#4F46E5");
    }
    
    private void buildDashboardStructure() {
        VerticalLayout mainContainer = createMainContainer();
        
        // Header ultra
        Component header = createUltraHeader();
        
        // Panel de métricas
        Component metricsPanel = createMetricsPanel();
        
        // Sección de gráficos
        Component chartSection = createChartSection();
        
        // Sección de procesos
        Component processSection = createProcessSection();
        
        // Footer
        Component footer = createFooter();
        
        mainContainer.add(alertBanner, header, metricsPanel, chartSection, processSection, footer);
        add(mainContainer);
    }
    
    private VerticalLayout createMainContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setSizeFull();
        container.setPadding(true);
        container.setSpacing(true);
        container.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        
        container.getStyle()
            .set("max-width", "1600px")
            .set("margin", "0 auto")
            .set("padding", "2rem")
            .set("gap", "2rem");
        
        return container;
    }
    
    private Component createUltraHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        header.getStyle()
            .set("background", "rgba(255, 255, 255, 0.1)")
            .set("border-radius", "16px")
            .set("padding", "1.5rem 2rem")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(20px)")
            .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.1)");
        
        // Título ultra
        HorizontalLayout titleSection = new HorizontalLayout();
        Icon dashIcon = VaadinIcon.DASHBOARD.create();
        dashIcon.setColor("#4F46E5");
        dashIcon.setSize("2rem");
        
        H1 title = new H1("🚀 Enterprise Dashboard");
        title.getStyle()
            .set("background", "linear-gradient(135deg, #4F46E5, #10B981)")
            .set("-webkit-background-clip", "text")
            .set("-webkit-text-fill-color", "transparent")
            .set("font-size", "2.5rem")
            .set("font-weight", "800")
            .set("margin", "0");
        
        titleSection.add(dashIcon, title);
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        
        // Panel de estado
        HorizontalLayout statusPanel = new HorizontalLayout();
        statusPanel.add(realtimeStatus, performanceIndicator);
        statusPanel.setAlignItems(FlexComponent.Alignment.CENTER);
        statusPanel.setSpacing(true);
        
        header.add(titleSection, statusPanel);
        return header;
    }
    
    private Component createMetricsPanel() {
        VerticalLayout panel = createUltraSection("📈 Estado del Sistema");
        
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        H2 panelTitle = new H2("📈 Estado del Sistema");
        panelTitle.getStyle()
            .set("margin", "0")
            .set("color", "#F9FAFB")
            .set("font-weight", "700");
        
        headerLayout.add(panelTitle, lastUpdateTime);
        
        HorizontalLayout metricsGrid = new HorizontalLayout();
        metricsGrid.setWidthFull();
        metricsGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        metricsGrid.setSpacing(true);
        
        metricsGrid.add(cpuProgressBar, memoryProgressBar, diskProgressBar);
        metricsGrid.setFlexGrow(1, cpuProgressBar, memoryProgressBar, diskProgressBar);
        
        panel.add(headerLayout, metricsGrid);
        return panel;
    }
    
    private Component createChartSection() {
        VerticalLayout section = createUltraSection("📊 Análisis en Tiempo Real");
        
        HorizontalLayout chartHeader = new HorizontalLayout();
        chartHeader.setWidthFull();
        chartHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        chartHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        
        H2 chartTitle = new H2("📊 Análisis en Tiempo Real");
        chartTitle.getStyle()
            .set("margin", "0")
            .set("color", "#F9FAFB")
            .set("font-weight", "700");
        
        chartHeader.add(chartTitle, periodTabs);
        
        section.add(chartHeader, realtimeChart);
        return section;
    }
    
    private Component createProcessSection() {
        VerticalLayout section = createUltraSection("⚙️ Procesos del Sistema");
        
        HorizontalLayout processHeader = new HorizontalLayout();
        processHeader.setWidthFull();
        processHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        processHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        
        H2 processTitle = new H2("⚙️ Procesos del Sistema");
        processTitle.getStyle()
            .set("margin", "0")
            .set("color", "#F9FAFB")
            .set("font-weight", "700");
        
        processHeader.add(processTitle, processFilterSelect);
        
        section.add(processHeader, processGrid);
        return section;
    }
    
    private Component createFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        
        footer.getStyle()
            .set("background", "rgba(255, 255, 255, 0.1)")
            .set("border-radius", "16px")
            .set("padding", "1.5rem 2rem")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(20px)")
            .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.1)");
        
        Span systemInfo = new Span("🖥️ Server Monitor Enterprise v2.0 | 📡 Datos en tiempo real");
        systemInfo.getStyle()
            .set("color", "#9CA3AF")
            .set("font-size", "0.875rem");
        
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.add(exportButton, fullscreenButton);
        actionButtons.setSpacing(true);
        
        footer.add(systemInfo, actionButtons);
        return footer;
    }
    
    private VerticalLayout createUltraSection(String title) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        
        section.getStyle()
            .set("background", "rgba(255, 255, 255, 0.1)")
            .set("border-radius", "16px")
            .set("padding", "2rem")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(20px)")
            .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.1)");
        
        return section;
    }
    
    // Métodos de actualización
    private void loadInitialData() {
        updateMetrics();
        updateChart();
        updateProcessData();
        updateAlertStatus();
        
        lastUpdateTime.setText("⏱️ " + LocalDateTime.now().format(timeFormatter));
        
        showNotification("🚀 Dashboard Ultra Pro cargado", NotificationVariant.LUMO_SUCCESS);
    }
    
    private void setupRealtimeUpdates() {
        UI.getCurrent().setPollInterval(5000);
        UI.getCurrent().addPollListener(event -> {
            updateMetrics();
            updateChart();
            updateProcessData();
            updateAlertStatus();
            
            lastUpdateTime.setText("⏱️ " + LocalDateTime.now().format(timeFormatter));
        });
    }
    
    private void updateMetrics() {
        try {
            SystemMetric currentMetrics = monitorService.getCurrentMetrics();
            
            cpuProgressBar.setValue(currentMetrics.getCpuUsage());
            cpuProgressBar.setAlert(currentMetrics.isCpuAlert());
            
            memoryProgressBar.setValue(currentMetrics.getMemoryUsage());
            memoryProgressBar.setAlert(currentMetrics.isMemoryAlert());
            
            diskProgressBar.setValue(currentMetrics.getDiskUsage());
            diskProgressBar.setAlert(currentMetrics.isDiskAlert());
            
        } catch (Exception e) {
            showNotification("Error actualizando métricas: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void updateChart() {
        try {
            List<SystemMetric> metrics = monitorService.getMetricsHistory(selectedTimeRange);
            updateChartDisplay(metrics);
        } catch (Exception e) {
            showNotification("Error actualizando gráfico: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void updateChartDisplay(List<SystemMetric> metrics) {
        // Find the metricsDisplay Div by traversing the realtimeChart's children
        Div metricsDisplay = null;
        for (Component child : realtimeChart.getChildren().toList()) {
            if (child instanceof VerticalLayout layout) {
                for (Component grandChild : layout.getChildren().toList()) {
                    if (grandChild instanceof Div div && "metrics-display".equals(div.getId().orElse(null))) {
                        metricsDisplay = div;
                        break;
                    }
                }
            }
            if (metricsDisplay != null) break;
        }

        if (metricsDisplay != null) {
            metricsDisplay.removeAll();

            if (metrics != null && !metrics.isEmpty()) {
                SystemMetric latest = metrics.get(metrics.size() - 1);

                metricsDisplay.add(createChartMetricCard("🖥️ CPU", latest.getCpuUsage(), "#4F46E5"));
                metricsDisplay.add(createChartMetricCard("💾 RAM", latest.getMemoryUsage(), "#10B981"));
                metricsDisplay.add(createChartMetricCard("💽 Disco", latest.getDiskUsage(), "#F59E0B"));
            } else {
                metricsDisplay.add(new Span("📊 Cargando métricas..."));
            }
        }
    }
    
    @SuppressWarnings("unused")
    private Div createChartMetricCard(String label, double value, String color) {
        Div card = new Div();
        card.getStyle()
            .set("background", "rgba(255,255,255,0.05)")
            .set("border-radius", "12px")
            .set("padding", "1.5rem")
            .set("text-align", "center")
            .set("border-left", "4px solid " + color);
        
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("display", "block")
            .set("font-weight", "600")
            .set("color", color)
            .set("margin-bottom", "0.5rem");
        
        Span valueSpan = new Span(String.format("%.1f%%", value));
        valueSpan.getStyle()
            .set("display", "block")
            .set("font-size", "2rem")
            .set("font-weight", "700")
            .set("color", value > 80 ? "#EF4444" : color);
        
        card.add(labelSpan, valueSpan);
        return card;
    }
    
    private void updateProcessData() {
        try {
            List<ProcessInfo> processes = processInfoService.getHeavyProcesses(15, "CPU");
            List<ProcessInfo> filteredProcesses = filterProcesses(processes);
            processGrid.setItems(filteredProcesses);
        } catch (Exception e) {
            showNotification("Error actualizando procesos: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }
    
    private List<ProcessInfo> filterProcesses(List<ProcessInfo> processes) {
        if (processes == null) return List.of();
        
        return switch (selectedProcessFilter) {
            case "HIGH_CPU" -> processes.stream().filter(p -> p.getCpuUsage() > 10.0).toList();
            case "HIGH_MEMORY" -> processes.stream().filter(p -> p.getMemoryUsage() > 5.0).toList();
            case "SYSTEM" -> processes.stream().filter(p -> "root".equals(p.getUsername()) || "system".equals(p.getUsername())).toList();
            case "USER" -> processes.stream().filter(p -> !"root".equals(p.getUsername()) && !"system".equals(p.getUsername())).toList();
            default -> processes;
        };
    }
    
    private void updateAlertStatus() {
        try {
            SystemMetric currentMetrics = monitorService.getCurrentMetrics();
            AlertConfiguration config = alertConfigService.getCurrentConfig();
            
            Map<String, Double> alerts = new HashMap<>();
            Map<String, Double> thresholds = new HashMap<>();
            
            boolean cpuAlert = currentMetrics.getCpuUsage() > config.getCpuThreshold();
            boolean memoryAlert = currentMetrics.getMemoryUsage() > config.getMemoryThreshold();
            boolean diskAlert = currentMetrics.getDiskUsage() > config.getDiskThreshold();
            
            if (cpuAlert) {
                alerts.put("CPU", currentMetrics.getCpuUsage());
                thresholds.put("CPU", config.getCpuThreshold());
            }
            if (memoryAlert) {
                alerts.put("Memoria", currentMetrics.getMemoryUsage());
                thresholds.put("Memoria", config.getMemoryThreshold());
            }
            if (diskAlert) {
                alerts.put("Disco", currentMetrics.getDiskUsage());
                thresholds.put("Disco", config.getDiskThreshold());
            }
            
            alertBanner.setAlerts(!alerts.isEmpty(), alerts, thresholds);
            
        } catch (Exception e) {
            showNotification("Error evaluando alertas: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }
    
    // Métodos de utilidad
    private Span createStatusBadge(ProcessInfo process) {
        Span badge = new Span(process.getStatus());
        
        String color = switch (process.getStatus()) {
            case "RUNNING" -> "#10B981";
            case "SLEEPING" -> "#6B7280";
            case "STOPPED" -> "#EF4444";
            case "ZOMBIE" -> "#F59E0B";
            default -> "#9CA3AF";
        };
        
        badge.getStyle()
            .set("background", color)
            .set("color", "white")
            .set("padding", "0.25rem 0.75rem")
            .set("border-radius", "12px")
            .set("font-size", "0.75rem")
            .set("font-weight", "600")
            .set("text-transform", "uppercase");
        
        return badge;
    }
    
    private Span createMetricBar(double value, String color) {
        Span container = new Span();
        String alertColor = value > 80 ? "#EF4444" : color;
        
        container.getElement().setProperty("innerHTML", 
            "<div style='display: flex; align-items: center; gap: 0.5rem;'>" +
            "<span style='min-width: 50px; font-weight: 600; color: " + alertColor + "; font-family: monospace;'>" + 
            String.format("%.1f%%", value) + "</span>" +
            "<div style='width: 60px; height: 8px; background: rgba(255,255,255,0.1); border-radius: 4px; overflow: hidden;'>" +
            "<div style='width: " + Math.min(value, 100) + "%; height: 100%; background: " + alertColor + 
            "; border-radius: 4px; transition: width 0.3s ease;'></div>" +
            "</div>" +
            "</div>");
        return container;
    }
    
    private String getProcessIcon(String processName) {
        if (processName == null) return "⚙️";
        String name = processName.toLowerCase();
        
        if (name.contains("java")) return "☕";
        if (name.contains("chrome") || name.contains("firefox")) return "🌐";
        if (name.contains("code") || name.contains("idea")) return "💻";
        if (name.contains("mysql") || name.contains("postgres")) return "🗄️";
        if (name.contains("node")) return "🟢";
        if (name.contains("python")) return "🐍";
        if (name.contains("docker")) return "🐳";
        if (name.contains("nginx") || name.contains("apache")) return "🌐";
        if (name.contains("systemd")) return "⚙️";
        if (name.contains("kernel")) return "🔧";
        return "⚙️";
    }
    
    private void styleUltraSelect(Select<?> select) {
        select.getStyle()
            .set("background", "rgba(255, 255, 255, 0.1)")
            .set("border-radius", "12px")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("color", "#F9FAFB")
            .set("--lumo-contrast-10pct", "rgba(79, 70, 229, 0.1)")
            .set("--lumo-primary-color", "#4F46E5");
    }
    
    private void styleUltraButton(Button button, String color) {
        button.getStyle()
            .set("background", "linear-gradient(135deg, " + color + ", " + color + "CC)")
            .set("border-radius", "12px")
            .set("padding", "0.75rem 1.5rem")
            .set("font-weight", "600")
            .set("color", "white")
            .set("border", "none")
            .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.1)")
            .set("transition", "all 0.3s ease");
        
        button.getElement().addEventListener("mouseenter", e -> 
            button.getStyle()
                .set("transform", "translateY(-2px)")
                .set("box-shadow", "0 8px 25px rgba(0, 0, 0, 0.2)"));
        
        button.getElement().addEventListener("mouseleave", e -> 
            button.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 4px 14px rgba(0, 0, 0, 0.1)"));
    }
    
    // Event handlers
    private void showExportDialog() {
        showNotification("📊 Preparando exportación ultra avanzada...", NotificationVariant.LUMO_PRIMARY);
        // TODO: Implementar en Paso 2
    }
    
    private void toggleFullscreen() {
        getElement().executeJs("""
            if (!document.fullscreenElement) {
                document.documentElement.requestFullscreen();
            } else {
                document.exitFullscreen();
            }
        """);
        showNotification("🖥️ Modo pantalla completa", NotificationVariant.LUMO_SUCCESS);
    }
    
    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(variant);
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // Configurar animaciones de entrada
        getElement().executeJs("""
            // Animación de entrada ultra suave
            const elements = this.querySelectorAll('> *');
            elements.forEach((el, index) => {
                el.style.opacity = '0';
                el.style.transform = 'translateY(30px)';
                
                setTimeout(() => {
                    el.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
                    el.style.opacity = '1';
                    el.style.transform = 'translateY(0)';
                }, index * 100);
            });
            
            // Mensaje de bienvenida
            console.log('🚀 Ultra Dashboard cargado - Sin dependencias externas');
        """);
        
        showNotification("✅ Dashboard conectado en tiempo real", NotificationVariant.LUMO_SUCCESS);
    }
}