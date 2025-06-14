// UltraProcessGrid.java
package com.monitoring.server.views.components.ultra;

import java.util.List;
import java.util.stream.Collectors;
import com.monitoring.server.data.entity.ProcessInfo;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;

/**
 * 🚀 Grid ultra avanzado para procesos
 */
public class UltraProcessGrid extends Grid<ProcessInfo> {
    
    public UltraProcessGrid() {
        super(ProcessInfo.class, false);
        
        addClassName("ultra-process-grid");
        setupUltraGrid();
        setupUltraColumns();
        setupUltraAnimations();
    }
    
    private void setupUltraGrid() {
        setAllRowsVisible(true);
        setMaxHeight("500px");
        addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        
        getStyle()
            .set("background", "rgba(255, 255, 255, 0.02)")
            .set("border-radius", "12px")
            .set("border", "1px solid rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("--lumo-font-size-s", "0.875rem");
    }
    
    private void setupUltraColumns() {
        // PID con estilo especial
        addColumn(ProcessInfo::getProcessId)
            .setHeader("🔢 PID")
            .setWidth("80px")
            .setFlexGrow(0);
        
        // Nombre del proceso con icono
        addColumn(new ComponentRenderer<>(this::createProcessNameComponent))
            .setHeader("⚙️ Proceso")
            .setWidth("200px")
            .setFlexGrow(1);
        
        // Usuario
        addColumn(ProcessInfo::getUsername)
            .setHeader("👤 Usuario")
            .setWidth("120px")
            .setFlexGrow(0);
        
        // Estado con badge
        addColumn(new ComponentRenderer<>(this::createStatusBadge))
            .setHeader("📊 Estado")
            .setWidth("100px")
            .setFlexGrow(0);
        
        // CPU con barra visual
        addColumn(new ComponentRenderer<>(this::createCpuComponent))
            .setHeader("🖥️ CPU (%)")
            .setWidth("120px")
            .setFlexGrow(0);
        
        // Memoria con barra visual
        addColumn(new ComponentRenderer<>(this::createMemoryComponent))
            .setHeader("💾 RAM (%)")
            .setWidth("120px")
            .setFlexGrow(0);
        
        // Disco I/O
        addColumn(p -> String.format("%.1f KB/s", p.getDiskUsage()))
            .setHeader("💽 Disco I/O")
            .setWidth("120px")
            .setFlexGrow(0);
    }
    
    private void setupUltraAnimations() {
        getElement().executeJs("""
            // Configurar animaciones de filas
            this.addEventListener('grid-refresh', function() {
                const rows = this.querySelectorAll('tr');
                rows.forEach((row, index) => {
                    row.style.opacity = '0';
                    row.style.transform = 'translateX(-20px)';
                    
                    setTimeout(() => {
                        row.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';
                        row.style.opacity = '1';
                        row.style.transform = 'translateX(0)';
                    }, index * 50);
                });
            });
        """);
    }
    
    public void updateProcesses(List<ProcessInfo> processes, String filter) {
        List<ProcessInfo> filteredProcesses = filterProcesses(processes, filter);
        setItems(filteredProcesses);
        
        // Animar actualización
        getElement().executeJs("""
            this.dispatchEvent(new CustomEvent('grid-refresh'));
        """);
    }
    
    private List<ProcessInfo> filterProcesses(List<ProcessInfo> processes, String filter) {
        return switch (filter) {
            case "HIGH_CPU" -> processes.stream()
                .filter(p -> p.getCpuUsage() > 10.0)
                .collect(Collectors.toList());
            case "HIGH_MEMORY" -> processes.stream()
                .filter(p -> p.getMemoryUsage() > 5.0)
                .collect(Collectors.toList());
            case "SYSTEM" -> processes.stream()
                .filter(p -> "root".equals(p.getUsername()) || "system".equals(p.getUsername()))
                .collect(Collectors.toList());
            case "USER" -> processes.stream()
                .filter(p -> !"root".equals(p.getUsername()) && !"system".equals(p.getUsername()))
                .collect(Collectors.toList());
            default -> processes;
        };
    }
    
    private Span createProcessNameComponent(ProcessInfo process) {
        Span span = new Span();
        String icon = getProcessIcon(process.getProcessName().toLowerCase());
        span.getElement().setProperty("innerHTML", 
            "<span style='font-weight: 600;'>" + icon + " " + process.getProcessName() + "</span>");
        return span;
    }
    
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
    
    private Span createCpuComponent(ProcessInfo process) {
        return createMetricBar(process.getCpuUsage(), "#4F46E5", "CPU");
    }
    
    private Span createMemoryComponent(ProcessInfo process) {
        return createMetricBar(process.getMemoryUsage(), "#10B981", "RAM");
    }
    
    private Span createMetricBar(double value, String color, String label) {
        Span container = new Span();
        container.getElement().setProperty("innerHTML", 
            "<div style='display: flex; align-items: center; gap: 0.5rem;'>" +
            "<span style='min-width: 45px; font-weight: 600; color: " + color + ";'>" + 
            String.format("%.1f%%", value) + "</span>" +
            "<div style='width: 60px; height: 8px; background: rgba(255,255,255,0.1); border-radius: 4px; overflow: hidden;'>" +
            "<div style='width: " + Math.min(value, 100) + "%; height: 100%; background: " + color + 
            "; border-radius: 4px; transition: width 0.3s ease;'></div>" +
            "</div>" +
            "</div>");
        return container;
    }
    
    private String getProcessIcon(String processName) {
        if (processName.contains("java")) return "☕";
        if (processName.contains("chrome") || processName.contains("firefox")) return "🌐";
        if (processName.contains("code") || processName.contains("idea")) return "💻";
        if (processName.contains("mysql") || processName.contains("postgres")) return "🗄️";
        if (processName.contains("node")) return "🟢";
        if (processName.contains("python")) return "🐍";
        if (processName.contains("docker")) return "🐳";
        if (processName.contains("nginx") || processName.contains("apache")) return "🌐";
        return "⚙️";
    }
}