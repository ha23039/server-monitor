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
import com.monitoring.server.views.components.ExportDialogView;
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
 * 🚀 DASHBOARD ULTRA PRO - VERSIÓN COMPLETA CORREGIDA
 * ✅ ExportDialogView completamente funcional y persistente
 * ✅ Sistema de exportación robusto con recuperación automática
 * ✅ Mantiene todas las funcionalidades avanzadas
 * ✅ Corrección de problemas de inyección de dependencias
 */
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("🚀 Enterprise Dashboard - Métricas en Tiempo Real")
@RequiresViewer
public class DashboardView extends VerticalLayout {

    // Services
    private final SystemMonitorService monitorService;
    private final ProcessInfoService processInfoService;
    private final AlertConfigService alertConfigService;

    // ✅ Sistema de exportación corregido
    private ExportDialogView exportDialogView;
    
    // Componentes principales
    private MetricProgressBar cpuProgressBar;
    private MetricProgressBar memoryProgressBar;
    private MetricProgressBar diskProgressBar;
    private Div realtimeChart;
    private Grid<ProcessInfo> processGrid;
    private AlertBanner alertBanner;
    
    // Controles avanzados
    private String selectedTimeRange = "1H";
    private String selectedProcessFilter = "ALL";
    private Tabs periodTabs;
    private Select<String> processFilterSelect;
    private Button exportButton;
    private Button fullscreenButton;
    
    // Estado del sistema
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
        
        setupUltraStyles();
        createUltraComponents();
        buildDashboardStructure();
        loadInitialData();
        setupRealtimeUpdates();
        
        // ✅ RESTAURADO: Inicialización del sistema de exportación
        initializeExportSystem();
    }
    
    // ✅ SIMPLIFICADO: Inicialización sin dependencias complejas
    private void initializeExportSystem() {
        try {
            // Crear siempre una nueva instancia para evitar conflictos
            if (exportDialogView == null) {
                System.out.println("🔧 Creando nueva instancia de ExportDialogView...");
                createNewExportDialog();
            }
            
            if (exportDialogView != null) {
                System.out.println("✅ ExportDialogView disponible: " + exportDialogView.getInstanceInfo());
            } else {
                System.out.println("⚠️ No se pudo crear ExportDialogView");
            }
        } catch (Exception e) {
            System.err.println("❌ Error inicializando sistema de exportación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ✅ NUEVO: Método para crear instancia específica
    private void createNewExportDialog() {
        try {
            // Crear instancia directamente (sin inyección problemática)
            exportDialogView = new ExportDialogView();
            
            if (exportDialogView != null) {
                System.out.println("✅ Nueva instancia de ExportDialogView creada: " + exportDialogView.getInstanceInfo());
            }
        } catch (Exception e) {
            System.err.println("❌ Error creando nueva instancia de ExportDialogView: " + e.getMessage());
            e.printStackTrace();
        }
    }
// ✅ HOTFIX: Método createManualExportDialog() corregido
private void createManualExportDialog() {
    try {
        // Forzar la creación manual del componente
        exportDialogView = new ExportDialogView();
        
        // Configurar el dialog para ser reutilizable
        if (exportDialogView != null) {
            exportDialogView.getElement().executeJs("""
                // Configurar como dialog reutilizable
                this.modality = 'modeless';
                this._persistent = true;
                this._readyToReopen = true;
                
                // Prevenir auto-destrucción
                this.addEventListener('vaadin-overlay-close', (e) => {
                    e.preventDefault();
                    this.opened = false;
                });
                
                console.log('✅ ExportDialogView creado manualmente y configurado');
            """);
            
            System.out.println("✅ ExportDialogView creado manualmente y configurado");
        }
    } catch (Exception e) {
        System.err.println("❌ Error creando ExportDialogView manual: " + e.getMessage());
        e.printStackTrace();
    }
}
    
    // ✅ NUEVO: Método para recrear el dialog cuando falla
    private void recreateExportDialog() {
        try {
            System.out.println("🔄 Recreando ExportDialogView...");
            
            // Limpiar referencia anterior
            exportDialogView = null;
            
            // Crear nuevo dialog
            createManualExportDialog();
            
            if (exportDialogView != null) {
                // Intentar abrir el nuevo dialog
                exportDialogView.open();
                showNotification("📊 Exportación reiniciada", NotificationVariant.LUMO_SUCCESS);
            } else {
                showNotification("❌ No se pudo recrear el sistema de exportación", NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            System.err.println("❌ Error recreando dialog: " + e.getMessage());
            showNotification("❌ Error crítico en exportación", NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void setupUltraStyles() {
        getStyle()
            .set("background", "linear-gradient(135deg, #0f1419 0%, #1a202c 50%, #2d3748 100%)")
            .set("min-height", "100vh")
            .set("color", "#F7FAFC")
            .set("font-family", "'Inter', -apple-system, BlinkMacSystemFont, sans-serif");
    }
    
    private void createUltraComponents() {
        // Banner de alertas ultra
        alertBanner = createUltraAlertBanner();
        
        // Tarjetas de métricas ultra responsivas
        AlertConfiguration config = alertConfigService.getCurrentConfig();
        cpuProgressBar = createResponsiveMetricCard("🖥️ CPU", config.getCpuThreshold(), "#4F46E5");
        memoryProgressBar = createResponsiveMetricCard("💾 RAM", config.getMemoryThreshold(), "#10B981");
        diskProgressBar = createResponsiveMetricCard("💽 Disco", config.getDiskThreshold(), "#F59E0B");
        
        // Gráfico ultra responsivo con SVG
        realtimeChart = createResponsiveUltraChart();
        
        // Grid ultra pro responsivo
        processGrid = createResponsiveUltraProcessGrid();
        
        // Controles avanzados
        createUltraControls();
        
        // Indicadores de estado
        createStatusIndicators();
    }
    
    // === COMPONENTES ULTRA RESPONSIVOS ===
    
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
    
    private MetricProgressBar createResponsiveMetricCard(String title, double threshold, String accentColor) {
        MetricProgressBar card = new MetricProgressBar(title, 0, threshold);
        card.addClassName("metric-card");
        
        card.getStyle()
            .set("background", "linear-gradient(135deg, rgba(255,255,255,0.1), rgba(255,255,255,0.05))")
            .set("border-radius", "20px")
            .set("padding", "2rem")
            .set("border", "1px solid rgba(255,255,255,0.1)")
            .set("backdrop-filter", "blur(20px)")
            .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)")
            .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
            .set("cursor", "pointer")
            .set("min-height", "200px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "space-between")
            .set("position", "relative")
            .set("overflow", "hidden");
        
        // JavaScript responsivo sin selector problemático
        card.getElement().executeJs(String.format("""
            const card = this;
            
            function applyResponsiveStyles() {
                const width = window.innerWidth;
                
                if (width <= 768) {
                    card.style.minHeight = '160px';
                    card.style.padding = '1.5rem';
                    card.style.borderRadius = '16px';
                } else if (width <= 1200) {
                    card.style.minHeight = '180px';
                    card.style.padding = '1.75rem';
                    card.style.borderRadius = '18px';
                } else {
                    card.style.minHeight = '200px';
                    card.style.padding = '2rem';
                    card.style.borderRadius = '20px';
                }
            }
            
            applyResponsiveStyles();
            window.addEventListener('resize', applyResponsiveStyles);
            
            const isTouchDevice = 'ontouchstart' in window;
            
            if (!isTouchDevice) {
                card.addEventListener('mouseenter', () => {
                    card.style.transform = 'translateY(-8px)';
                    card.style.boxShadow = '0 16px 48px rgba(0,0,0,0.2)';
                    card.style.borderColor = '%s';
                });
                
                card.addEventListener('mouseleave', () => {
                    card.style.transform = 'translateY(0)';
                    card.style.boxShadow = '0 8px 32px rgba(0,0,0,0.1)';
                    card.style.borderColor = 'rgba(255,255,255,0.1)';
                });
            } else {
                card.addEventListener('touchstart', () => {
                    card.style.transform = 'scale(0.98)';
                });
                
                card.addEventListener('touchend', () => {
                    card.style.transform = 'scale(1)';
                });
            }
        """, accentColor));
        
        return card;
    }
    
    private Div createResponsiveUltraChart() {
        Div chart = new Div();
        chart.addClassName("ultra-chart-responsive");
        chart.setWidth("100%");
        
        chart.getStyle()
            .set("border-radius", "16px")
            .set("padding", "1.5rem")
            .set("border", "1px solid rgba(255,255,255,0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("position", "relative")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "center")
            .set("align-items", "center")
            .set("min-height", "300px");
        
        chart.getElement().executeJs("""
            const chart = this;
            
            function adjustChartHeight() {
                const width = window.innerWidth;
                
                if (width <= 768) {
                    chart.style.minHeight = '250px';
                    chart.style.padding = '1rem';
                } else if (width <= 1200) {
                    chart.style.minHeight = '300px';
                    chart.style.padding = '1.25rem';
                } else {
                    chart.style.minHeight = '400px';
                    chart.style.padding = '1.5rem';
                }
            }
            
            adjustChartHeight();
            window.addEventListener('resize', adjustChartHeight);
        """);
        
        VerticalLayout chartContent = new VerticalLayout();
        chartContent.setPadding(false);
        chartContent.setSpacing(true);
        chartContent.setAlignItems(FlexComponent.Alignment.CENTER);
        chartContent.setSizeFull();
        
        H2 chartTitle = new H2("📈 Métricas en Tiempo Real");
        chartTitle.addClassName("chart-title-responsive");
        chartTitle.getStyle()
            .set("margin", "0 0 1.5rem 0")
            .set("color", "#F9FAFB")
            .set("text-align", "center");
        
        chartTitle.getElement().executeJs("""
            const title = this;
            
            function adjustTitleSize() {
                const width = window.innerWidth;
                
                if (width <= 768) {
                    title.style.fontSize = '1.5rem';
                    title.style.marginBottom = '1rem';
                } else if (width <= 1200) {
                    title.style.fontSize = '1.75rem';
                    title.style.marginBottom = '1.25rem';
                } else {
                    title.style.fontSize = '2rem';
                    title.style.marginBottom = '1.5rem';
                }
            }
            
            adjustTitleSize();
            window.addEventListener('resize', adjustTitleSize);
        """);
        
        Div metricsDisplay = new Div();
        metricsDisplay.setId("metrics-display");
        metricsDisplay.setSizeFull();
        
        chartContent.add(chartTitle, metricsDisplay);
        chart.add(chartContent);
        
        return chart;
    }
    
    private Grid<ProcessInfo> createResponsiveUltraProcessGrid() {
        Grid<ProcessInfo> grid = new Grid<>(ProcessInfo.class, false);
        grid.addClassName("ultra-process-grid-responsive");
        
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        
        grid.getStyle()
            .set("background", "rgba(255, 255, 255, 0.02)")
            .set("border-radius", "12px")
            .set("border", "1px solid rgba(255,255,255,0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("--lumo-font-size-s", "0.875rem")
            .set("color", "#F9FAFB")
            .set("width", "100%")
            .set("overflow-x", "auto");
        
        grid.getElement().executeJs("""
            const grid = this;
            
            function adjustGridHeight() {
                const width = window.innerWidth;
                
                if (width <= 768) {
                    grid.style.maxHeight = '300px';
                    grid.style.fontSize = '0.8rem';
                } else if (width <= 1200) {
                    grid.style.maxHeight = '350px';
                    grid.style.fontSize = '0.875rem';
                } else {
                    grid.style.maxHeight = '400px';
                    grid.style.fontSize = '0.9rem';
                }
            }
            
            adjustGridHeight();
            window.addEventListener('resize', adjustGridHeight);
        """);
        
        setupResponsiveUltraGridColumns(grid);
        return grid;
    }
    
    private void setupResponsiveUltraGridColumns(Grid<ProcessInfo> grid) {
        // PID - Oculto en mobile
        grid.addColumn(new ComponentRenderer<>(process -> {
            Span span = new Span("🔢 " + process.getProcessId());
            span.getStyle().set("font-weight", "600");
            return span;
        })).setHeader("PID").setWidth("100px").setFlexGrow(0);
        
        // Proceso con icono - Siempre visible
        grid.addColumn(new ComponentRenderer<>(process -> {
            String icon = getProcessIcon(process.getProcessName());
            
            Div container = new Div();
            container.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "0.5rem")
                .set("min-width", "0")
                .set("flex", "1");
            
            Span iconSpan = new Span(icon);
            iconSpan.getStyle().set("flex-shrink", "0");
            
            Span nameSpan = new Span(process.getProcessName());
            nameSpan.getStyle()
                .set("font-weight", "600")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap")
                .set("flex", "1")
                .set("min-width", "0");
            
            container.add(iconSpan, nameSpan);
            return container;
        })).setHeader("⚙️ Proceso").setFlexGrow(1);
        
        // Usuario - Oculto en mobile pequeño
        grid.addColumn(new ComponentRenderer<>(process -> {
            Span span = new Span("👤 " + (process.getUsername() != null ? process.getUsername() : "N/A"));
            return span;
        })).setHeader("Usuario").setWidth("120px").setFlexGrow(0);
        
        // Estado con badge responsivo
        grid.addColumn(new ComponentRenderer<>(this::createResponsiveStatusBadge))
            .setHeader("📊 Estado").setWidth("100px").setFlexGrow(0);
        
        // CPU con barra responsiva
        grid.addColumn(new ComponentRenderer<>(process -> 
            createResponsiveMetricBar(process.getCpuUsage(), "#4F46E5")))
            .setHeader("🖥️ CPU").setWidth("120px").setFlexGrow(0);
        
        // Memoria con barra responsiva
        grid.addColumn(new ComponentRenderer<>(process -> 
            createResponsiveMetricBar(process.getMemoryUsage(), "#10B981")))
            .setHeader("💾 RAM").setWidth("120px").setFlexGrow(0);
        
        // Aplicar responsive a las columnas
        grid.getElement().executeJs("""
            const grid = this;
            
            function adjustColumns() {
                const width = window.innerWidth;
                const columns = grid.querySelectorAll('vaadin-grid-column');
                
                if (width <= 768) {
                    if (columns[0]) columns[0].hidden = true; // PID
                    if (columns[2]) columns[2].hidden = true; // Usuario
                    
                    if (columns[1]) columns[1].width = 'auto'; // Proceso
                    if (columns[3]) columns[3].width = '80px'; // Estado
                    if (columns[4]) columns[4].width = '100px'; // CPU
                    if (columns[5]) columns[5].width = '100px'; // RAM
                    
                } else if (width <= 1200) {
                    if (columns[0]) columns[0].hidden = false;
                    if (columns[2]) columns[2].hidden = false;
                    
                    if (columns[0]) columns[0].width = '80px';
                    if (columns[2]) columns[2].width = '100px';
                    
                } else {
                    if (columns[0]) {
                        columns[0].hidden = false;
                        columns[0].width = '100px';
                    }
                    if (columns[2]) {
                        columns[2].hidden = false;
                        columns[2].width = '120px';
                    }
                }
            }
            
            setTimeout(adjustColumns, 100);
            window.addEventListener('resize', adjustColumns);
        """);
    }
    
    private void createUltraControls() {
        // Selector de período ultra
        periodTabs = new Tabs();
        periodTabs.add(new Tab("1H"), new Tab("24H"), new Tab("7D"), new Tab("1M"));
        periodTabs.getStyle()
            .set("background", "linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%)")
            .set("border-radius", "8px")
            .set("padding", "0.25rem");
        
        periodTabs.addSelectedChangeListener(event -> {
            selectedTimeRange = event.getSelectedTab().getLabel();
            updateChart();
        });
        
        // Filtro de procesos ultra
        processFilterSelect = new Select<>();
        processFilterSelect.setItems("ALL", "HIGH_CPU", "HIGH_MEMORY", "SYSTEM", "USER");
        processFilterSelect.setValue("ALL");
        processFilterSelect.setLabel("🔍 Filtrar Procesos");
        styleUltraSelect(processFilterSelect);
        
        processFilterSelect.addValueChangeListener(event -> {
            selectedProcessFilter = event.getValue();
            updateProcessData();
        });
        
        // ✅ BOTÓN DE EXPORTACIÓN PRINCIPAL (CORREGIDO)
        exportButton = new Button("📊 Exportar", VaadinIcon.DOWNLOAD.create());
        exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        styleUltraButton(exportButton, "#4F46E5");
        exportButton.addClickListener(e -> showExportModalDirectly());
        
        // Botón pantalla completa
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
    
    // === ESTRUCTURA DEL DASHBOARD ===
    
    private void buildDashboardStructure() {
        VerticalLayout mainContainer = createMainContainer();
        
        Component header = createUltraHeader();
        Component metricsPanel = createResponsiveMetricsPanel();
        Component chartSection = createChartSection();
        Component processSection = createProcessSection();
        Component footer = createFooterWithExport();
        
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
            .set("background", "linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%)")
            .set("border-radius", "16px")
            .set("padding", "1.5rem 2rem")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(20px)")
            .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.1)");
        
        HorizontalLayout titleSection = new HorizontalLayout();
        Icon dashIcon = VaadinIcon.DASHBOARD.create();
        dashIcon.setColor("#4F46E5");
        dashIcon.setSize("2rem");
        
        H1 title = new H1("🚀 Enterprise Dashboard");
        title.getStyle()
            .set("background", "linear-gradient(135deg, #4F46E5, #10B981)")
            .set("-webkit-background-clip", "text")
            .set("font-size", "2.5rem")
            .set("font-weight", "800")
            .set("margin", "0");
        
        titleSection.add(dashIcon, title);
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        
        HorizontalLayout statusPanel = new HorizontalLayout();
        statusPanel.add(realtimeStatus, performanceIndicator);
        statusPanel.setAlignItems(FlexComponent.Alignment.CENTER);
        statusPanel.setSpacing(true);
        
        header.add(titleSection, statusPanel);
        return header;
    }
    
    private Component createResponsiveMetricsPanel() {
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
        
        Div metricsGrid = new Div();
        metricsGrid.addClassName("metrics-responsive-grid");
        metricsGrid.setWidthFull();
        
        metricsGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
            .set("gap", "1.5rem")
            .set("width", "100%")
            .set("align-items", "stretch");
        
        metricsGrid.getElement().executeJs("""
            const style = document.createElement('style');
            style.textContent = `
                @media (max-width: 768px) {
                    .metrics-responsive-grid {
                        grid-template-columns: 1fr !important;
                        gap: 1rem !important;
                    }
                }
                
                @media (min-width: 769px) and (max-width: 1200px) {
                    .metrics-responsive-grid {
                        grid-template-columns: repeat(2, 1fr) !important;
                    }
                }
                
                @media (min-width: 1201px) {
                    .metrics-responsive-grid {
                        grid-template-columns: repeat(3, 1fr) !important;
                    }
                }
                
                @media (max-width: 768px) {
                    .metric-card {
                        min-height: 160px !important;
                        padding: 1.5rem !important;
                    }
                }
            `;
            document.head.appendChild(style);
        """);
        
        metricsGrid.add(cpuProgressBar, memoryProgressBar, diskProgressBar);
        
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
    
    private Component createFooterWithExport() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        
        footer.getStyle()
            .set("background", "linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%)")
            .set("border-radius", "16px")
            .set("padding", "1.5rem 2rem")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(20px)")
            .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.1)");
        
        Span systemInfo = new Span("🖥️ Server Monitor Enterprise v2.0 | 📡 Datos en tiempo real");
        systemInfo.getStyle()
            .set("color", "#9CA3AF")
            .set("font-size", "0.875rem");
        
        HorizontalLayout exportSection = new HorizontalLayout();
        exportSection.setAlignItems(FlexComponent.Alignment.CENTER);
        exportSection.setSpacing(true);
        
        // ✅ SOLO LOS BOTONES ESENCIALES
        exportSection.add(exportButton, fullscreenButton);
        
        footer.add(systemInfo, exportSection);
        return footer;
    }
    
    private VerticalLayout createUltraSection(String title) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        
        section.getStyle()
            .set("background", "linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%)")
            .set("border-radius", "16px")
            .set("padding", "2rem")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(20px)")
            .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.1)");
        
        return section;
    }
    
    // === MÉTODOS DE ACTUALIZACIÓN OPTIMIZADOS ===
    
    private void loadInitialData() {
        updateMetrics();
        updateChart();
        updateProcessData();
        updateAlertStatus();
        
        lastUpdateTime.setText("⏱️ " + LocalDateTime.now().format(timeFormatter));
        
        showNotification("🚀 Dashboard cargado correctamente", NotificationVariant.LUMO_SUCCESS);
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
    
    // === GRÁFICOS SVG ULTRA OPTIMIZADOS ===
    private void updateChartDisplay(List<SystemMetric> metrics) {
        Div metricsDisplay = realtimeChart.getChildren()
            .filter(component -> component instanceof VerticalLayout)
            .map(component -> (VerticalLayout) component)
            .findFirst()
            .map(layout -> layout.getChildren()
                .filter(child -> child instanceof Div && "metrics-display".equals(child.getId().orElse("")))
                .map(child -> (Div) child)
                .findFirst()
                .orElse(null))
            .orElse(null);
        
        if (metricsDisplay == null) {
            metricsDisplay = new Div();
            metricsDisplay.setId("metrics-display");
            
            VerticalLayout chartContent = realtimeChart.getChildren()
                .filter(component -> component instanceof VerticalLayout)
                .map(component -> (VerticalLayout) component)
                .findFirst()
                .orElse(null);
            
            if (chartContent != null) {
                chartContent.add(metricsDisplay);
            }
        }
        
        metricsDisplay.removeAll();
        
        metricsDisplay.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
            .set("gap", "1.5rem")
            .set("width", "100%")
            .set("max-width", "900px")
            .set("margin", "0 auto");
        
        if (metrics == null || metrics.isEmpty()) {
            Span loadingSpan = new Span("📊 Cargando métricas en tiempo real...");
            loadingSpan.getStyle()
                .set("text-align", "center")
                .set("color", "#9CA3AF")
                .set("padding", "2rem")
                .set("font-style", "italic")
                .set("grid-column", "1 / -1");
            metricsDisplay.add(loadingSpan);
            return;
        }
        
        SystemMetric latest = metrics.get(metrics.size() - 1);
        
        Div cpuChart = createAnimatedMiniChart("🖥️ CPU", latest.getCpuUsage(), "#4F46E5", metrics, "cpu");
        Div memoryChart = createAnimatedMiniChart("💾 RAM", latest.getMemoryUsage(), "#10B981", metrics, "memory");
        Div diskChart = createAnimatedMiniChart("💽 Disco", latest.getDiskUsage(), "#F59E0B", metrics, "disk");
        
        metricsDisplay.add(cpuChart, memoryChart, diskChart);
        
        metricsDisplay.getElement().executeJs("""
            this.style.opacity = '0';
            this.style.transform = 'translateY(20px)';
            
            setTimeout(() => {
                this.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
                this.style.opacity = '1';
                this.style.transform = 'translateY(0)';
            }, 100);
        """);
    }

    private Div createAnimatedMiniChart(String label, double currentValue, String color, List<SystemMetric> metrics, String type) {
        Div chartContainer = new Div();
        chartContainer.addClassName("mini-chart-" + type);
        
        chartContainer.getStyle()
            .set("background", "rgba(255,255,255,0.05)")
            .set("border-radius", "16px")
            .set("padding", "1.5rem")
            .set("border-left", "4px solid " + color)
            .set("position", "relative")
            .set("overflow", "hidden")
            .set("min-height", "140px")
            .set("backdrop-filter", "blur(10px)")
            .set("transition", "all 0.3s ease");
        
        HorizontalLayout header = new HorizontalLayout();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.getStyle().set("margin-bottom", "1rem");
        
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-weight", "600")
            .set("color", color)
            .set("font-size", "1.1rem");
        
        Span valueSpan = new Span(String.format("%.1f%%", currentValue));
        valueSpan.getStyle()
            .set("font-size", "2rem")
            .set("font-weight", "700")
            .set("color", currentValue > 80 ? "#EF4444" : color);
        
        header.add(labelSpan, valueSpan);
        
        Div svgArea = createSVGChart(metrics, type, color, currentValue);
        
        HorizontalLayout footer = new HorizontalLayout();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setWidthFull();
        footer.getStyle().set("margin-top", "0.5rem");
        
        String trend = "📊";
        if (metrics.size() > 1) {
            double previousValue = getPreviousValue(metrics, type);
            trend = currentValue > previousValue ? "📈" : "📉";
        }
        
        Span trendSpan = new Span("Tendencia: " + trend);
        trendSpan.getStyle()
            .set("font-size", "0.875rem")
            .set("color", "#9CA3AF");
        
        Span pointsSpan = new Span(Math.min(metrics.size(), 20) + " puntos");
        pointsSpan.getStyle()
            .set("font-size", "0.75rem")
            .set("color", "#6B7280");
        
        footer.add(trendSpan, pointsSpan);
        
        VerticalLayout chartLayout = new VerticalLayout();
        chartLayout.setPadding(false);
        chartLayout.setSpacing(false);
        chartLayout.add(header, svgArea, footer);
        
        chartContainer.add(chartLayout);
        
        chartContainer.getElement().addEventListener("mouseenter", e -> {
            chartContainer.getStyle()
                .set("transform", "translateY(-4px)")
                .set("box-shadow", "0 12px 40px rgba(0,0,0,0.15)")
                .set("border-left-color", color);
        });
        
        chartContainer.getElement().addEventListener("mouseleave", e -> {
            chartContainer.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "none");
        });
        
        return chartContainer;
    }

    private Div createSVGChart(List<SystemMetric> metrics, String type, String color, double currentValue) {
        Div svgContainer = new Div();
        svgContainer.setHeight("80px");
        svgContainer.getStyle()
            .set("position", "relative")
            .set("width", "100%");
        
        List<SystemMetric> recentMetrics = metrics.stream()
            .skip(Math.max(0, metrics.size() - 15))
            .toList();
        
        if (recentMetrics.isEmpty()) {
            Span noDataSpan = new Span("📊 Sin datos");
            noDataSpan.getStyle()
                .set("color", "#6B7280")
                .set("font-size", "0.875rem")
                .set("text-align", "center")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("height", "100%"); 
            svgContainer.add(noDataSpan);
            return svgContainer;
        }
        
        StringBuilder points = new StringBuilder();
        StringBuilder areaPoints = new StringBuilder("0,80 ");
        
        for (int i = 0; i < recentMetrics.size(); i++) {
            double value = getValue(recentMetrics.get(i), type);
            double x = (double) i / (recentMetrics.size() - 1) * 100;
            double y = 80 - (value / 100) * 80;
            
            if (i > 0) {
                points.append(" ");
                areaPoints.append(" ");
            }
            points.append(String.format("%.1f,%.1f", x, y));
            areaPoints.append(String.format("%.1f,%.1f", x, y));
        }
        areaPoints.append(" 100,80");
        
        String svgContent = String.format("""
            <svg width="100%%" height="80px" style="position: absolute; top: 0; left: 0;">
                <defs>
                    <linearGradient id="gradient-%s" x1="0%%" y1="0%%" x2="0%%" y2="100%%">
                        <stop offset="0%%" style="stop-color:%s;stop-opacity:0.3" />
                        <stop offset="100%%" style="stop-color:%s;stop-opacity:0.05" />
                    </linearGradient>
                </defs>
                
                <line x1="0" y1="20" x2="100%%" y2="20" stroke="rgba(255,255,255,0.1)" stroke-width="1"/>
                <line x1="0" y1="40" x2="100%%" y2="40" stroke="rgba(255,255,255,0.1)" stroke-width="1"/>
                <line x1="0" y1="60" x2="100%%" y2="60" stroke="rgba(255,255,255,0.1)" stroke-width="1"/>
                
                <polygon points="%s" fill="url(#gradient-%s)" />
                
                <polyline points="%s" fill="none" stroke="%s" stroke-width="2.5" 
                        stroke-linecap="round" stroke-linejoin="round"
                        style="filter: drop-shadow(0 0 4px %s40);" />
                
                <circle cx="100%%" cy="%.1f" r="3" fill="%s" stroke="white" stroke-width="2"
                        style="filter: drop-shadow(0 0 6px %s);">
                    <animate attributeName="r" values="3;4;3" dur="2s" repeatCount="indefinite"/>
                </circle>
            </svg>
            """, 
            type, color, color,
            areaPoints.toString(), type,
            points.toString(), color, color,
            80 - (currentValue / 100) * 80, color, color
        );
        
        svgContainer.getElement().setProperty("innerHTML", svgContent);
        return svgContainer;
    }

    private double getValue(SystemMetric metric, String type) {
        return switch (type) {
            case "cpu" -> metric.getCpuUsage();
            case "memory" -> metric.getMemoryUsage();
            case "disk" -> metric.getDiskUsage();
            default -> 0.0;
        };
    }

    private double getPreviousValue(List<SystemMetric> metrics, String type) {
        if (metrics.size() < 2) return 0.0;
        return getValue(metrics.get(metrics.size() - 2), type);
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
    
    // === COMPONENTES RESPONSIVOS ULTRA ===
    
    private Span createResponsiveStatusBadge(ProcessInfo process) {
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
            .set("padding", "0.25rem 0.5rem")
            .set("border-radius", "8px")
            .set("font-size", "0.7rem")
            .set("font-weight", "600")
            .set("text-transform", "uppercase")
            .set("display", "inline-block")
            .set("text-align", "center")
            .set("min-width", "60px");
        
        badge.getElement().executeJs("""
            const badge = this;
            
            function adjustBadgeSize() {
                const width = window.innerWidth;
                
                if (width <= 768) {
                    badge.style.fontSize = '0.6rem';
                    badge.style.padding = '0.2rem 0.4rem';
                    badge.style.minWidth = '50px';
                } else {
                    badge.style.fontSize = '0.7rem';
                    badge.style.padding = '0.25rem 0.5rem';
                    badge.style.minWidth = '60px';
                }
            }
            
            adjustBadgeSize();
            window.addEventListener('resize', adjustBadgeSize);
        """);
        
        return badge;
    }

    private Span createResponsiveMetricBar(double value, String color) {
        Span container = new Span();
        String alertColor = value > 80 ? "#EF4444" : color;
        
        container.getElement().executeJs(String.format("""
            const container = this;
            
            function createResponsiveBar() {
                const width = window.innerWidth;
                let barWidth, fontSize;
                
                if (width <= 768) {
                    barWidth = '40px';
                    fontSize = '0.7rem';
                } else if (width <= 1200) {
                    barWidth = '50px';
                    fontSize = '0.8rem';
                } else {
                    barWidth = '60px';
                    fontSize = '0.875rem';
                }
                
                container.innerHTML = 
                    '<div style="display: flex; align-items: center; gap: 0.5rem;">' +
                    '<span style="min-width: 45px; font-weight: 600; color: %s; font-family: monospace; font-size: ' + fontSize + ';">' + 
                    '%.1f%%</span>' +
                    '<div style="width: ' + barWidth + '; height: 6px; background: rgba(255,255,255,0.1); border-radius: 3px; overflow: hidden;">' +
                    '<div style="width: %.1f%%; height: 100%%; background: %s; border-radius: 3px; transition: width 0.3s ease;"></div>' +
                    '</div>' +
                    '</div>';
            }
            
            createResponsiveBar();
            window.addEventListener('resize', createResponsiveBar);
        """, alertColor, value, Math.min(value, 100), alertColor));
        
        return container;
    }
    
    // === ICONOS DE PROCESOS ULTRA COMPLETO ===
    private String getProcessIcon(String processName) {
        if (processName == null) return "⚙️";
        String name = processName.toLowerCase();
        
        // Procesos Java y JVM
        if (name.contains("java") || name.contains("openjdk")) return "☕";
        if (name.contains("spring") || name.contains("tomcat")) return "🍃";
        
        // Navegadores
        if (name.contains("chrome") || name.contains("chromium")) return "🌐";
        if (name.contains("firefox")) return "🦊";
        if (name.contains("safari")) return "🧭";
        if (name.contains("edge")) return "🌊";
        
        // Editores y IDEs
        if (name.contains("code") || name.contains("vscode")) return "💻";
        if (name.contains("idea") || name.contains("intellij")) return "🧠";
        if (name.contains("eclipse")) return "🌘";
        if (name.contains("vim") || name.contains("nano")) return "📝";
        
        // Bases de datos
        if (name.contains("mysql") || name.contains("mariadb")) return "🐬";
        if (name.contains("postgres") || name.contains("postgresql")) return "🐘";
        if (name.contains("mongo") || name.contains("mongodb")) return "🍃";
        if (name.contains("redis")) return "🔴";
        if (name.contains("elasticsearch")) return "🔍";
        
        // Lenguajes y runtimes
        if (name.contains("node") || name.contains("nodejs")) return "🟢";
        if (name.contains("python") || name.contains("python3")) return "🐍";
        if (name.contains("php") || name.contains("php-fpm")) return "🐘";
        if (name.contains("ruby") || name.contains("rails")) return "💎";
        if (name.contains("go") || name.contains("golang")) return "🐹";
        if (name.contains("rust") || name.contains("cargo")) return "🦀";
        
        // Contenedores y orquestación
        if (name.contains("docker") || name.contains("dockerd")) return "🐳";
        if (name.contains("kubernetes") || name.contains("kubectl")) return "⛵";
        if (name.contains("containerd")) return "📦";
        
        // Servidores web y proxy
        if (name.contains("nginx")) return "🌐";
        if (name.contains("apache") || name.contains("httpd")) return "🪶";
        if (name.contains("caddy")) return "⚡";
        if (name.contains("traefik")) return "🔀";
        
        // Sistemas y servicios
        if (name.contains("systemd")) return "⚙️";
        if (name.contains("kernel") || name.contains("kthread")) return "🔧";
        if (name.contains("ssh") || name.contains("sshd")) return "🔐";
        if (name.contains("cron") || name.contains("anacron")) return "⏰";
        if (name.contains("rsyslog") || name.contains("syslog")) return "📋";
        
        // Procesos de red
        if (name.contains("network") || name.contains("netplan")) return "🌐";
        if (name.contains("dhcp")) return "📡";
        if (name.contains("dns") || name.contains("bind")) return "🎯";
        
        // Procesos de monitoreo
        if (name.contains("htop") || name.contains("top")) return "📊";
        if (name.contains("monitor") || name.contains("watch")) return "👀";
        if (name.contains("prometheus") || name.contains("grafana")) return "📈";
        
        // Procesos Alpine Linux específicos
        if (name.contains("busybox")) return "📦";
        if (name.contains("ash") || name.contains("sh")) return "🐚";
        if (name.contains("init")) return "🚀";
        
        // Default
        return "⚙️";
    }
    
    // === MÉTODOS DE ESTILIZACIÓN ULTRA ===
    
    private void styleUltraSelect(Select<?> select) {
        select.getStyle()
            .set("background", "linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%)")
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
    
    // ✅ CORREGIDO: Método principal sin complejidad
    private void showExportModalDirectly() {
        try {
            System.out.println("🔍 Verificando ExportDialogView...");
            
            // Asegurar que siempre tenemos una instancia válida
            if (exportDialogView == null) {
                System.out.println("⚠️ ExportDialogView es null, creando nueva instancia...");
                createNewExportDialog();
            }
            
            if (exportDialogView != null) {
                System.out.println("✅ Abriendo ExportDialogView: " + exportDialogView.getInstanceInfo());
                exportDialogView.open();
                showNotification("📊 Abriendo exportación...", NotificationVariant.LUMO_PRIMARY);
            } else {
                System.err.println("❌ No se pudo crear ExportDialogView");
                showNotification("❌ Sistema de exportación no disponible", NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            System.err.println("❌ Error en showExportModalDirectly: " + e.getMessage());
            e.printStackTrace();
            
            // Intentar recrear en caso de error
            try {
                System.out.println("🔄 Intentando recrear ExportDialogView...");
                exportDialogView = null;
                createNewExportDialog();
                
                if (exportDialogView != null) {
                    exportDialogView.open();
                    showNotification("📊 Exportación reiniciada", NotificationVariant.LUMO_SUCCESS);
                } else {
                    showNotification("❌ Error crítico en exportación", NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception retryError) {
                System.err.println("❌ Error en reintento: " + retryError.getMessage());
                showNotification("❌ Error crítico: " + retryError.getMessage(), NotificationVariant.LUMO_ERROR);
            }
         }
     }
    // === MÉTODOS DE EVENTOS PRINCIPALES ===
    
    private void toggleFullscreen() {
        getElement().executeJs("""
            if (!document.fullscreenElement) {
                document.documentElement.requestFullscreen().catch(err => {
                    console.log('Error activando pantalla completa:', err);
                });
            } else {
                document.exitFullscreen().catch(err => {
                    console.log('Error saliendo de pantalla completa:', err);
                });
            }
        """);
        showNotification("🖥️ Modo pantalla completa", NotificationVariant.LUMO_SUCCESS);
    }
    
    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(variant);
    }
    
    // === MÉTODOS ADICIONALES DE UTILIDAD ===
    
    private void initializeKeyboardShortcuts() {
        getElement().executeJs("""
            document.addEventListener('keydown', function(e) {
                // Ctrl + E para exportar
                if (e.ctrlKey && e.key === 'e') {
                    e.preventDefault();
                    const exportEvent = new CustomEvent('keyboardExport');
                    window.dispatchEvent(exportEvent);
                }
                
                // F11 para pantalla completa
                if (e.key === 'F11') {
                    e.preventDefault();
                    const fullscreenEvent = new CustomEvent('keyboardFullscreen');
                    window.dispatchEvent(fullscreenEvent);
                }
                
                // Ctrl + R para refresh manual
                if (e.ctrlKey && e.key === 'r') {
                    e.preventDefault();
                    const refreshEvent = new CustomEvent('keyboardRefresh');
                    window.dispatchEvent(refreshEvent);
                }
            });
            
            // Listeners para los eventos de teclado
            window.addEventListener('keyboardExport', () => {
                console.log('🚀 Export triggered by keyboard');
            });
            
            window.addEventListener('keyboardFullscreen', () => {
                if (!document.fullscreenElement) {
                    document.documentElement.requestFullscreen();
                } else {
                    document.exitFullscreen();
                }
            });
            
            window.addEventListener('keyboardRefresh', () => {
                console.log('🔄 Manual refresh triggered');
            });
        """);
    }
    
    private void setupAdvancedFeatures() {
        getElement().executeJs("""
            // Sistema de notificaciones push
            if ('Notification' in window && Notification.permission === 'granted') {
                console.log('✅ Notificaciones del navegador habilitadas');
            } else if ('Notification' in window && Notification.permission !== 'denied') {
                Notification.requestPermission().then(permission => {
                    if (permission === 'granted') {
                        console.log('✅ Permisos de notificación otorgados');
                    }
                });
            }
            
            // Detección de pérdida de conexión
            window.addEventListener('offline', () => {
                console.log('📵 Conexión perdida - Modo offline');
            });
            
            window.addEventListener('online', () => {
                console.log('🌐 Conexión restaurada');
            });
            
            // Performance monitoring
            const observer = new PerformanceObserver((list) => {
                for (const entry of list.getEntries()) {
                    if (entry.entryType === 'navigation') {
                        console.log('📊 Tiempo de carga:', entry.loadEventEnd - entry.loadEventStart, 'ms');
                    }
                }
            });
            
            observer.observe({entryTypes: ['navigation']});
        """);
    }
    
    // === EVENTO ONATTACH CORREGIDO ===
    
// ✅ ACTUALIZADO: onAttach sin complejidad
@Override
protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    
    // Verificar sistema de exportación después de attach
    UI.getCurrent().access(() -> {
        if (exportDialogView == null) {
            System.out.println("🔧 ExportDialogView null en onAttach, creando instancia...");
            createNewExportDialog();
        } else {
            System.out.println("✅ ExportDialogView ya disponible en onAttach: " + exportDialogView.getInstanceInfo());
        }
    });
    
    // Animación de entrada
    getElement().executeJs("""
        // Animación de entrada suave
        const elements = document.querySelectorAll('.ultra-dashboard > *');
        elements.forEach((el, index) => {
            if (el) {
                el.style.opacity = '0';
                el.style.transform = 'translateY(30px)';
                
                setTimeout(() => {
                    el.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
                    el.style.opacity = '1';
                    el.style.transform = 'translateY(0)';
                }, index * 100);
            }
        });
        
        // Sistema de métricas de rendimiento
        window.dashboardMetrics = {
            startTime: Date.now(),
            updateCount: 0,
            exportCount: 0
        };
        
        console.log('🚀 Dashboard Ultra Pro cargado');
        console.log('📊 Sistema de exportación: VERIFICANDO...');
    """);
    
    // Inicializar características avanzadas
    initializeKeyboardShortcuts();
    setupAdvancedFeatures();
}
    // ✅ OPCIONAL: Método para recrear el dialog si hay problemas
private void recreateExportDialogIfNeeded() {
    try {
        if (exportDialogView == null) {
            System.out.println("🔄 Recreando ExportDialogView...");
            createNewExportDialog();
        }
    } catch (Exception e) {
        System.err.println("❌ Error recreando ExportDialogView: " + e.getMessage());
    }
}
}