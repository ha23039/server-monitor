package com.monitoring.server.views.dashboard;

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
import com.monitoring.server.views.components.AdvancedMetricChart;
import com.monitoring.server.views.components.AlertBanner;
import com.monitoring.server.views.components.MetricProgressBar;
import com.monitoring.server.views.components.RealTimeProcessGrid;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard - Métricas en Tiempo Real")
@RequiresViewer
public class DashboardView extends VerticalLayout {

    private final SystemMonitorService monitorService;
    private final ProcessInfoService processInfoService;
    private final AlertConfigService alertConfigService;
    
    // Componentes principales
    private MetricProgressBar cpuProgressBar;
    private MetricProgressBar memoryProgressBar;
    private MetricProgressBar diskProgressBar;
    private AdvancedMetricChart systemUsageChart;
    private RealTimeProcessGrid processGrid;
    private AlertBanner alertBanner;
    
    // Controles
    private String selectedPeriod = "1H";
    private String selectedProcessSortColumn = "CPU";
    private Tabs periodTabs;
    private Button exportButton;
    private Span lastUpdateLabel;
    private Span connectionStatusLabel;
    
    @Autowired
    public DashboardView(SystemMonitorService monitorService,
                         ProcessInfoService processInfoService,
                         AlertConfigService alertConfigService) {
        this.monitorService = monitorService;
        this.processInfoService = processInfoService;
        this.alertConfigService = alertConfigService;
        
        addClassName("dashboard-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        initializeComponents();
        setupLayout();
        setupWebSocketStatusMonitoring();
        loadInitialData();
    }
    
    private void initializeComponents() {
        // Crear contenedor principal
        VerticalLayout mainContainer = createMainContainer();
        
        // Inicializar componentes
        alertBanner = createAlertBanner();
        systemUsageChart = new AdvancedMetricChart();
        processGrid = new RealTimeProcessGrid();
        
        // Configurar componentes principales
        Component statusPanel = createStatusPanel();
        Component chartSection = createChartSection();
        Component processSection = createProcessSection();
        Component exportSection = createExportSection();
        
        // Agregar al contenedor principal
        mainContainer.add(alertBanner, statusPanel, chartSection, processSection, exportSection);
        add(mainContainer);
    }
    
    private void setupLayout() {
        // Layout ya configurado en initializeComponents()
    }
    
    private void setupWebSocketStatusMonitoring() {
        // Configurar indicador de estado de conexión en tiempo real
        connectionStatusLabel = new Span("🔴 Desconectado");
        connectionStatusLabel.getStyle()
            .set("position", "fixed")
            .set("top", "10px")
            .set("right", "20px")
            .set("background", "rgba(0, 0, 0, 0.8)")
            .set("color", "white")
            .set("padding", "0.5rem 1rem")
            .set("border-radius", "20px")
            .set("font-size", "0.8rem")
            .set("z-index", "1000");
        
        // Simular conexión WebSocket exitosa después de un momento
        UI.getCurrent().access(() -> {
            connectionStatusLabel.setText("🟢 Tiempo Real Activo");
            Notification.show("✅ Dashboard conectado en tiempo real", 3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        
        add(connectionStatusLabel);
    }
    
    private void loadInitialData() {
        try {
            // Cargar métricas iniciales
            updateMetrics();
            updateProcessList();
            updateChart();
            updateAlertStatus();
            
            // Actualizar timestamp
            lastUpdateLabel.setText("Última actualización: " + 
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
            
        } catch (Exception e) {
            Notification.show("❌ Error cargando datos iniciales: " + e.getMessage(), 
                             5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private VerticalLayout createMainContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setSizeFull();
        container.setPadding(true);
        container.setSpacing(true);
        container.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        
        container.getStyle()
            .set("padding", "2rem")
            .set("padding-left", "3rem")
            .set("padding-right", "2rem")
            .set("max-width", "1400px")
            .set("margin", "0 auto")
            .set("box-sizing", "border-box");
            
        return container;
    }
    
    private Component createStatusPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.getStyle().set("margin-bottom", "2rem");
        
        // Header con título y estado
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        H2 title = new H2("Estado del Sistema");
        title.getStyle()
            .set("margin", "0")
            .set("color", "#F9FAFB")
            .set("font-weight", "600");
        
        lastUpdateLabel = new Span("Cargando...");
        lastUpdateLabel.getStyle()
            .set("color", "#9CA3AF")
            .set("font-size", "0.9rem");
        
        headerLayout.add(title, lastUpdateLabel);
        
        // Panel de métricas
        HorizontalLayout metricsLayout = new HorizontalLayout();
        metricsLayout.setWidth("100%");
        metricsLayout.setPadding(false);
        metricsLayout.setSpacing(true);
        metricsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        AlertConfiguration config = alertConfigService.getCurrentConfig();
        
        cpuProgressBar = new MetricProgressBar("Uso de CPU", 0, config.getCpuThreshold());
        memoryProgressBar = new MetricProgressBar("Uso de RAM", 0, config.getMemoryThreshold());
        diskProgressBar = new MetricProgressBar("Uso de Disco", 0, config.getDiskThreshold());
        
        Div cpuSection = createMetricSection(cpuProgressBar);
        Div memorySection = createMetricSection(memoryProgressBar);
        Div diskSection = createMetricSection(diskProgressBar);
        
        metricsLayout.add(cpuSection, memorySection, diskSection);
        metricsLayout.setFlexGrow(1, cpuSection, memorySection, diskSection);
        
        layout.add(headerLayout, metricsLayout);
        return layout;
    }
    
    private Component createChartSection() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.getStyle()
            .set("background", "rgba(255, 255, 255, 0.05)")
            .set("border-radius", "12px")
            .set("padding", "2rem")
            .set("margin-bottom", "2rem")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(10px)");
        
        H2 title = new H2("Procesos Más Pesados");
        title.getStyle()
            .set("margin", "0 0 1.5rem 0")
            .set("color", "#F9FAFB")
            .set("font-weight", "600")
            .set("font-size", "1.5rem");
        
        Select<String> sortSelect = new Select<>();
        sortSelect.setItems("CPU", "Memoria", "Disco");
        sortSelect.setValue("CPU");
        sortSelect.setLabel("Ordenar por");
        sortSelect.getStyle()
            .set("background", "rgba(255, 255, 255, 0.1)")
            .set("border-radius", "8px")
            .set("min-width", "150px");
            
        sortSelect.addValueChangeListener(event -> {
            selectedProcessSortColumn = event.getValue();
            updateProcessList();
        });
        
        HorizontalLayout headerLayout = new HorizontalLayout(title, sortSelect);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setWidthFull();
        headerLayout.getStyle().set("margin-bottom", "1rem");
        
        layout.add(headerLayout, processGrid);
        return layout;
    }
    
    private Component createExportSection() {
        HorizontalLayout exportLayout = new HorizontalLayout();
        exportLayout.setWidthFull();
        exportLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        exportLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        exportLayout.getStyle()
            .set("padding", "1rem 0")
            .set("border-top", "1px solid rgba(255, 255, 255, 0.1)");
        
        exportButton = new Button("Exportar Métricas", VaadinIcon.DOWNLOAD.create());
        exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        exportButton.getStyle()
            .set("background", "linear-gradient(135deg, #3B82F6, #1D4ED8)")
            .set("border", "none")
            .set("box-shadow", "0 4px 14px 0 rgba(59, 130, 246, 0.3)");
        
        exportButton.addClickListener(e -> showExportDialog());
        
        Span exportInfo = new Span("💾 Exporta métricas del sistema en CSV o PDF");
        exportInfo.getStyle()
            .set("color", "#9CA3AF")
            .set("font-size", "0.9rem")
            .set("margin-right", "1rem");
        
        exportLayout.add(exportInfo, exportButton);
        return exportLayout;
    }
    
    private Div createMetricSection(MetricProgressBar progressBar) {
        Div section = new Div();
        section.addClassName("metric-section");
        section.getStyle()
            .set("background", "rgba(255, 255, 255, 0.05)")
            .set("border-radius", "12px")
            .set("padding", "1.5rem")
            .set("margin", "0 0.5rem")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("min-height", "120px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("transition", "all 0.3s ease");
        
        // Efecto hover
        section.getElement().addEventListener("mouseenter", e -> {
            section.getStyle().set("transform", "translateY(-2px)");
            section.getStyle().set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.15)");
        });
        
        section.getElement().addEventListener("mouseleave", e -> {
            section.getStyle().set("transform", "translateY(0)");
            section.getStyle().set("box-shadow", "none");
        });
        
        section.add(progressBar);
        return section;
    }
    
    private AlertBanner createAlertBanner() {
        AlertBanner banner = new AlertBanner();
        banner.setVisible(false);
        banner.getStyle().set("margin-bottom", "1.5rem");
        return banner;
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
            
            // Actualizar timestamp
            if (lastUpdateLabel != null) {
                lastUpdateLabel.setText("Última actualización: " + 
                    java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
            
        } catch (Exception e) {
            showErrorNotification("Error actualizando métricas: " + e.getMessage());
        }
    }
    
    private void updateChart() {
        try {
            List<SystemMetric> metrics = monitorService.getMetricsHistory(selectedPeriod);
            systemUsageChart.updateChart(metrics);
        } catch (Exception e) {
            showErrorNotification("Error actualizando gráfico: " + e.getMessage());
        }
    }
    
    private void updateProcessList() {
        try {
            List<ProcessInfo> processes = processInfoService.getHeavyProcesses(10, selectedProcessSortColumn);
            processGrid.updateProcesses(processes);
        } catch (Exception e) {
            showErrorNotification("Error actualizando procesos: " + e.getMessage());
        }
    }
    
    private void updateAlertStatus() {
        try {
            SystemMetric currentMetrics = monitorService.getCurrentMetrics();
            AlertConfiguration config = alertConfigService.getCurrentConfig();
            
            boolean cpuAlert = currentMetrics.getCpuUsage() > config.getCpuThreshold();
            boolean memoryAlert = currentMetrics.getMemoryUsage() > config.getMemoryThreshold();
            boolean diskAlert = currentMetrics.getDiskUsage() > config.getDiskThreshold();
            
            currentMetrics.setCpuAlert(cpuAlert);
            currentMetrics.setMemoryAlert(memoryAlert);
            currentMetrics.setDiskAlert(diskAlert);
            
            boolean hasAlerts = cpuAlert || memoryAlert || diskAlert;
            
            Map<String, Double> alertValues = new HashMap<>();
            Map<String, Double> thresholds = new HashMap<>();
            
            if (cpuAlert) {
                alertValues.put("CPU", currentMetrics.getCpuUsage());
                thresholds.put("CPU", config.getCpuThreshold());
            }
            
            if (memoryAlert) {
                alertValues.put("Memoria", currentMetrics.getMemoryUsage());
                thresholds.put("Memoria", config.getMemoryThreshold());
            }
            
            if (diskAlert) {
                alertValues.put("Disco", currentMetrics.getDiskUsage());
                thresholds.put("Disco", config.getDiskThreshold());
            }
            
            alertBanner.setAlerts(hasAlerts, alertValues, thresholds);
            
        } catch (Exception e) {
            showErrorNotification("Error evaluando alertas: " + e.getMessage());
        }
    }
    
    private void showExportDialog() {
        // Implementación del diálogo de exportación (próximamente en el siguiente paso)
        Notification.show("🚧 Función de exportación en desarrollo - Paso 3", 
                         3000, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }
    
private void showErrorNotification(String message) {
    Notification.show("❌ " + message, 5000, Notification.Position.TOP_END)
        .addThemeVariants(NotificationVariant.LUMO_ERROR);
}

/**
 * Sección de procesos más pesados (debajo del gráfico).
 */
private Component createProcessSection() {
    VerticalLayout layout = new VerticalLayout();
    layout.setPadding(false);
    layout.setSpacing(true);
    layout.getStyle()
        .set("background", "rgba(255, 255, 255, 0.05)")
        .set("border-radius", "12px")
        .set("padding", "2rem")
        .set("margin-bottom", "2rem")
        .set("border", "1px solid rgba(255, 255, 255, 0.1)")
        .set("backdrop-filter", "blur(10px)");

    H2 title = new H2("Procesos Más Pesados");
    title.getStyle()
        .set("margin", "0 0 1.5rem 0")
        .set("color", "#F9FAFB")
        .set("font-weight", "600")
        .set("font-size", "1.5rem");

    Select<String> sortSelect = new Select<>();
    sortSelect.setItems("CPU", "Memoria", "Disco");
    sortSelect.setValue(selectedProcessSortColumn);
    sortSelect.setLabel("Ordenar por");
    sortSelect.getStyle()
        .set("background", "rgba(255, 255, 255, 0.1)")
        .set("border-radius", "8px")
        .set("min-width", "150px");

    sortSelect.addValueChangeListener(event -> {
        selectedProcessSortColumn = event.getValue();
        updateProcessList();
    });

    HorizontalLayout headerLayout = new HorizontalLayout(title, sortSelect);
    headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
    headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    headerLayout.setWidthFull();
    headerLayout.getStyle().set("margin-bottom", "1rem");

    layout.add(headerLayout, processGrid);
    return layout;
}
}