package com.monitoring.server.views.components;

import java.util.List;

import com.monitoring.server.data.entity.ProcessInfo;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;

/**
 * Grid especializado para mostrar procesos del sistema en tiempo real
 * Incluye formateo visual y actualización automática vía WebSocket
 */
public class RealTimeProcessGrid extends Grid<ProcessInfo> {
    
    public RealTimeProcessGrid() {
        super(ProcessInfo.class, false);
        
        setupGrid();
        setupColumns();
        setupStyling();
    }
    
    private void setupGrid() {
        addClassName("realtime-process-grid");
        setAllRowsVisible(true);
        setMaxHeight("400px");
        addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        
        // Configurar estilos
        getStyle()
            .set("background", "rgba(255, 255, 255, 0.02)")
            .set("border-radius", "8px")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("--lumo-font-size-s", "0.875rem");
    }
    
    private void setupColumns() {
        // Columna de ID del proceso
        addColumn(ProcessInfo::getProcessId)
            .setHeader("PID")
            .setWidth("80px")
            .setFlexGrow(0)
            .setSortable(true);
        
        // Columna de nombre del proceso con icono
        addColumn(new ComponentRenderer<>(this::createProcessNameComponent))
            .setHeader("Proceso")
            .setWidth("200px")
            .setFlexGrow(1)
            .setSortable(true);
        
        // Columna de usuario
        addColumn(ProcessInfo::getUsername)
            .setHeader("Usuario")
            .setWidth("120px")
            .setFlexGrow(0)
            .setSortable(true);
        
        // Columna de estado con badge
        addColumn(new ComponentRenderer<>(this::createStatusBadge))
            .setHeader("Estado")
            .setWidth("100px")
            .setFlexGrow(0)
            .setSortable(true);
        
        // Columna de CPU con barra de progreso
        addColumn(new ComponentRenderer<>(this::createCpuUsageComponent))
            .setHeader("CPU (%)")
            .setWidth("120px")
            .setFlexGrow(0)
            .setSortable(true)
            .setComparator(ProcessInfo::getCpuUsage);
        
        // Columna de memoria con barra de progreso
        addColumn(new ComponentRenderer<>(this::createMemoryUsageComponent))
            .setHeader("RAM (%)")
            .setWidth("120px")
            .setFlexGrow(0)
            .setSortable(true)
            .setComparator(ProcessInfo::getMemoryUsage);
        
        // Columna de disco
        addColumn(process -> String.format("%.1f KB/s", process.getDiskUsage()))
            .setHeader("Disco I/O")
            .setWidth("100px")
            .setFlexGrow(0)
            .setSortable(true)
            .setComparator(ProcessInfo::getDiskUsage);
    }
    
    private void setupStyling() {
        // Estilos adicionales para el grid
        getElement().getStyle()
            .set("--lumo-base-color", "rgba(255, 255, 255, 0.05)")
            .set("--lumo-contrast-5pct", "rgba(255, 255, 255, 0.1)")
            .set("--lumo-contrast-10pct", "rgba(255, 255, 255, 0.15)")
            .set("color", "#F9FAFB");
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // Configurar WebSocket para actualizaciones de procesos en tiempo real
        getElement().executeJs("""
            // Conectar a WebSocket para procesos en tiempo real
            if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
                const socket = new SockJS('/ws-metrics');
                const stompClient = Stomp.over(socket);
                
                stompClient.connect({}, function(frame) {
                    console.log('✅ Process Grid conectado a WebSocket');
                    
                    // Suscribirse a actualizaciones de procesos
                    stompClient.subscribe('/topic/processes', function(message) {
                        const processes = JSON.parse(message.body);
                        // La actualización se maneja desde el lado del servidor
                        console.log('📊 Nuevos datos de procesos recibidos:', processes.length);
                    });
                    
                }, function(error) {
                    console.error('❌ Error conectando Process Grid WebSocket:', error);
                });
            }
        """);
    }
    
    /**
     * Actualiza los procesos mostrados en el grid
     */
    public void updateProcesses(List<ProcessInfo> processes) {
        getUI().ifPresent(ui -> ui.access(() -> {
            setItems(processes);
            
            // Animar la actualización
            getElement().getStyle().set("opacity", "0.7");
            
            ui.getPage().executeJs("""
                setTimeout(() => {
                    $0.style.opacity = '1';
                }, 100);
            """, getElement());
        }));
    }
    
    /**
     * Crea el componente para mostrar el nombre del proceso
     */
    private Span createProcessNameComponent(ProcessInfo process) {
        Span processSpan = new Span(process.getProcessName());
        processSpan.getStyle()
            .set("font-weight", "500")
            .set("color", "#F9FAFB");
        
        // Agregar icono según el tipo de proceso
        String processName = process.getProcessName().toLowerCase();
        String icon = getProcessIcon(processName);
        
        if (!icon.isEmpty()) {
            processSpan.getElement().setProperty("innerHTML", 
                icon + " " + process.getProcessName());
        }
        
        return processSpan;
    }
    
    /**
     * Crea un badge para el estado del proceso
     */
    private Span createStatusBadge(ProcessInfo process) {
        Span statusBadge = new Span(process.getStatus());
        statusBadge.getElement().getThemeList().add("badge");
        
        // Colores según el estado
        String bgColor = switch (process.getStatus()) {
            case "RUNNING" -> "#10B981";
            case "SLEEPING" -> "#6B7280";
            case "STOPPED" -> "#EF4444";
            case "ZOMBIE" -> "#F59E0B";
            default -> "#9CA3AF";
        };
        
        statusBadge.getStyle()
            .set("background", bgColor)
            .set("color", "white")
            .set("padding", "0.25rem 0.5rem")
            .set("border-radius", "0.375rem")
            .set("font-size", "0.75rem")
            .set("font-weight", "500")
            .set("text-transform", "uppercase");
        
        return statusBadge;
    }
    
    /**
     * Crea componente visual para el uso de CPU
     */
    private Span createCpuUsageComponent(ProcessInfo process) {
        double cpuUsage = process.getCpuUsage();
        
        Span container = new Span();
        container.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.5rem");
        
        // Texto del porcentaje
        Span percentageText = new Span(String.format("%.1f%%", cpuUsage));
        percentageText.getStyle()
            .set("min-width", "45px")
            .set("font-weight", "500")
            .set("color", cpuUsage > 80 ? "#EF4444" : "#F9FAFB");
        
        // Mini barra de progreso
        Span progressBar = new Span();
        progressBar.getStyle()
            .set("width", "40px")
            .set("height", "6px")
            .set("background", "rgba(255, 255, 255, 0.2)")
            .set("border-radius", "3px")
            .set("position", "relative")
            .set("overflow", "hidden");
        
        Span progressFill = new Span();
        progressFill.getStyle()
            .set("position", "absolute")
            .set("left", "0")
            .set("top", "0")
            .set("height", "100%")
            .set("width", Math.min(cpuUsage, 100) + "%")
            .set("background", cpuUsage > 80 ? "#EF4444" : 
                              cpuUsage > 60 ? "#F59E0B" : "#10B981")
            .set("border-radius", "3px")
            .set("transition", "width 0.3s ease");
        
        progressBar.add(progressFill);
        container.add(percentageText, progressBar);
        
        return container;
    }
    
    /**
     * Crea componente visual para el uso de memoria
     */
    private Span createMemoryUsageComponent(ProcessInfo process) {
        double memoryUsage = process.getMemoryUsage();
        
        Span container = new Span();
        container.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.5rem");
        
        // Texto del porcentaje
        Span percentageText = new Span(String.format("%.1f%%", memoryUsage));
        percentageText.getStyle()
            .set("min-width", "45px")
            .set("font-weight", "500")
            .set("color", memoryUsage > 80 ? "#EF4444" : "#F9FAFB");
        
        // Mini barra de progreso
        Span progressBar = new Span();
        progressBar.getStyle()
            .set("width", "40px")
            .set("height", "6px")
            .set("background", "rgba(255, 255, 255, 0.2)")
            .set("border-radius", "3px")
            .set("position", "relative")
            .set("overflow", "hidden");
        
        Span progressFill = new Span();
        progressFill.getStyle()
            .set("position", "absolute")
            .set("left", "0")
            .set("top", "0")
            .set("height", "100%")
            .set("width", Math.min(memoryUsage, 100) + "%")
            .set("background", memoryUsage > 80 ? "#EF4444" : 
                              memoryUsage > 60 ? "#F59E0B" : "#3B82F6")
            .set("border-radius", "3px")
            .set("transition", "width 0.3s ease");
        
        progressBar.add(progressFill);
        container.add(percentageText, progressBar);
        
        return container;
    }
    
    /**
     * Obtiene icono apropiado según el nombre del proceso
     */
    private String getProcessIcon(String processName) {
        if (processName.contains("java")) return "☕";
        if (processName.contains("chrome") || processName.contains("firefox")) return "🌐";
        if (processName.contains("code") || processName.contains("idea")) return "💻";
        if (processName.contains("mysql") || processName.contains("postgres")) return "🗄️";
        if (processName.contains("node")) return "🟢";
        if (processName.contains("python")) return "🐍";
        if (processName.contains("docker")) return "🐳";
        if (processName.contains("nginx") || processName.contains("apache")) return "🌐";
        return "";
    }
}