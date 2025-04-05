package com.monitoring.server.views.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.monitoring.server.data.entity.AlertConfiguration;
import com.monitoring.server.data.entity.ProcessInfo;
import com.monitoring.server.data.entity.SystemMetric;
import com.monitoring.server.service.interfaces.AlertConfigService;
import com.monitoring.server.service.interfaces.ProcessInfoService;
import com.monitoring.server.service.interfaces.SystemMonitorService;
import com.monitoring.server.views.MainLayout;
import com.monitoring.server.views.components.AlertBanner;
import com.monitoring.server.views.components.MetricChart;
import com.monitoring.server.views.components.MetricProgressBar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard (Métricas en Tiempo Real)")
public class DashboardView extends VerticalLayout {

    private final SystemMonitorService monitorService;
    private final ProcessInfoService processInfoService;
    private final AlertConfigService alertConfigService;
    
    private MetricProgressBar cpuProgressBar;
    private MetricProgressBar memoryProgressBar;
    private MetricProgressBar diskProgressBar;
    private MetricChart systemUsageChart;
    private Grid<ProcessInfo> processGrid;
    private AlertBanner alertBanner;
    
    private String selectedPeriod = "1H";
    private String selectedProcessSortColumn = "CPU";
    
    private Tabs periodTabs;
    
    @Autowired
    public DashboardView(SystemMonitorService monitorService,
                         ProcessInfoService processInfoService,
                         AlertConfigService alertConfigService) {
        this.monitorService = monitorService;
        this.processInfoService = processInfoService;
        this.alertConfigService = alertConfigService;
        
        addClassName("dashboard-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        alertBanner = createAlertBanner();
        Component statusPanel = createStatusPanel();
        Component chartSection = createChartSection();
        Component processSection = createProcessSection();
        
        add(alertBanner, statusPanel, chartSection, processSection);
        
        setupPeriodicRefresh();
    }
    
    private void setupPeriodicRefresh() {
        UI ui = UI.getCurrent();
        ui.setPollInterval(5000);
        ui.addPollListener(event -> {
            ui.access(() -> {
                updateMetrics();
                updateProcessList();
                updateAlertStatus();
            });
        });
        
        updateMetrics();
        updateProcessList();
    }
    
    private Component createStatusPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.setPadding(false);
        layout.setSpacing(true);
        
        HorizontalLayout metricsLayout = new HorizontalLayout();
        metricsLayout.setWidth("100%");
        metricsLayout.setPadding(false);
        metricsLayout.setSpacing(true);
        
        AlertConfiguration config = alertConfigService.getCurrentConfig();
        
        cpuProgressBar = new MetricProgressBar("Uso de CPU", 0, config.getCpuThreshold());
        memoryProgressBar = new MetricProgressBar("Uso de RAM", 0, config.getMemoryThreshold());
        diskProgressBar = new MetricProgressBar("Uso de Disco", 0, config.getDiskThreshold());
        
        Div cpuSection = createMetricSection(cpuProgressBar);
        Div memorySection = createMetricSection(memoryProgressBar);
        Div diskSection = createMetricSection(diskProgressBar);
        
        metricsLayout.add(cpuSection, memorySection, diskSection);
        metricsLayout.setFlexGrow(1, cpuSection, memorySection, diskSection);
        
        layout.add(metricsLayout);
        return layout;
    }
    
    private Div createMetricSection(MetricProgressBar progressBar) {
        Div section = new Div();
        section.addClassName("metric-section");
        section.add(progressBar);
        return section;
    }
    
    private Component createChartSection() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        
        H2 title = new H2("Gráfica de Uso del Sistema");
        title.getStyle().set("margin-top", "0");
        
        Tab tab1h = new Tab("1H");
        Tab tab24h = new Tab("24H");
        Tab tab7d = new Tab("7D");
        Tab tab1m = new Tab("1M");
        
        periodTabs = new Tabs(tab1h, tab24h, tab7d, tab1m);
        periodTabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            selectedPeriod = selectedTab.getLabel();
            updateChart();
        });
        
        systemUsageChart = new MetricChart();
        
        HorizontalLayout headerLayout = new HorizontalLayout(title, periodTabs);
        headerLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setWidthFull();
        
        layout.add(headerLayout, systemUsageChart);
        return layout;
    }
    
    private Component createProcessSection() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        
        H2 title = new H2("Procesos Más Pesados");
        title.getStyle().set("margin-top", "0");
        
        Select<String> sortSelect = new Select<>();
        sortSelect.setItems("CPU", "Memoria", "Disco");
        sortSelect.setValue("CPU");
        sortSelect.setLabel("Ordenar por");
        sortSelect.addValueChangeListener(event -> {
            selectedProcessSortColumn = event.getValue();
            updateProcessList();
        });
        
        processGrid = new Grid<>();
        processGrid.addColumn(ProcessInfo::getProcessName).setHeader("Proceso");
        processGrid.addColumn(ProcessInfo::getUsername).setHeader("Usuario");
        processGrid.addColumn(ProcessInfo::getStatus).setHeader("Estado");
        processGrid.addColumn(p -> String.format("%.1f%%", p.getCpuUsage())).setHeader("CPU (%)");
        processGrid.addColumn(p -> String.format("%.1f%%", p.getMemoryUsage())).setHeader("Memoria (%)");
        processGrid.addColumn(p -> String.format("%.1f KB/s", p.getDiskUsage())).setHeader("Disco (KB/s)");
        
        processGrid.setAllRowsVisible(true);
        
        HorizontalLayout headerLayout = new HorizontalLayout(title, sortSelect);
        headerLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setWidthFull();
        
        layout.add(headerLayout, processGrid);
        return layout;
    }
    
    private AlertBanner createAlertBanner() {
        AlertBanner banner = new AlertBanner();
        banner.setVisible(false);
        return banner;
    }
    
    private void updateMetrics() {
        SystemMetric currentMetrics = monitorService.getCurrentMetrics();
        
        cpuProgressBar.setValue(currentMetrics.getCpuUsage());
        cpuProgressBar.setAlert(currentMetrics.isCpuAlert());
        
        memoryProgressBar.setValue(currentMetrics.getMemoryUsage());
        memoryProgressBar.setAlert(currentMetrics.isMemoryAlert());
        
        diskProgressBar.setValue(currentMetrics.getDiskUsage());
        diskProgressBar.setAlert(currentMetrics.isDiskAlert());
        
        updateChart();
    }
    
    private void updateChart() {
        List<SystemMetric> metrics = monitorService.getMetricsHistory(selectedPeriod);
        systemUsageChart.updateChart(metrics);
    }
    
    private void updateProcessList() {
        List<ProcessInfo> processes = processInfoService.getHeavyProcesses(10, selectedProcessSortColumn);
        processGrid.setItems(processes);
    }
    
    private void updateAlertStatus() {
        SystemMetric currentMetrics = monitorService.getCurrentMetrics();
        AlertConfiguration config = alertConfigService.getCurrentConfig();
        
        // Compara directamente los valores actuales con los umbrales configurados
        boolean cpuAlert = currentMetrics.getCpuUsage() > config.getCpuThreshold();
        boolean memoryAlert = currentMetrics.getMemoryUsage() > config.getMemoryThreshold();
        boolean diskAlert = currentMetrics.getDiskUsage() > config.getDiskThreshold();
        
        // Actualiza los flags de alerta en el objeto currentMetrics
        currentMetrics.setCpuAlert(cpuAlert);
        currentMetrics.setMemoryAlert(memoryAlert);
        currentMetrics.setDiskAlert(diskAlert);
        
        boolean hasAlerts = cpuAlert || memoryAlert || diskAlert;
        
        Map<String, Double> alertValues = new HashMap<>();
        Map<String, Double> thresholds = new HashMap<>();
        
        // Añadir todas las alertas que superan los umbrales
        if (cpuAlert) {
            alertValues.put("CPU", currentMetrics.getCpuUsage());
            thresholds.put("CPU", config.getCpuThreshold());
            System.out.println("¡ALERTA DE CPU! Valor actual: " + currentMetrics.getCpuUsage() + "%, umbral: " + config.getCpuThreshold() + "%");
        }
        
        if (memoryAlert) {
            alertValues.put("Memoria", currentMetrics.getMemoryUsage());
            thresholds.put("Memoria", config.getMemoryThreshold());
            System.out.println("¡ALERTA DE MEMORIA! Valor actual: " + currentMetrics.getMemoryUsage() + "%, umbral: " + config.getMemoryThreshold() + "%");
        }
        
        if (diskAlert) {
            alertValues.put("Disco", currentMetrics.getDiskUsage());
            thresholds.put("Disco", config.getDiskThreshold());
            System.out.println("¡ALERTA DE DISCO! Valor actual: " + currentMetrics.getDiskUsage() + "%, umbral: " + config.getDiskThreshold() + "%");
        }
        
        System.out.println("Estado de alertas - CPU: " + cpuAlert + ", Memoria: " + memoryAlert + ", Disco: " + diskAlert);
        System.out.println("Total de alertas activas: " + alertValues.size());
        System.out.println("¿Mostrar banner de alertas? " + hasAlerts);
        
        // Asegúrate de que alertBanner.setAlerts() procese correctamente múltiples alertas
        alertBanner.setAlerts(hasAlerts, alertValues, thresholds);
    }
}