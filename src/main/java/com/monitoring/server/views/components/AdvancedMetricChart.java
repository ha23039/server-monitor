package com.monitoring.server.views.components;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.monitoring.server.data.entity.SystemMetric;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;

/**
 * Componente avanzado para mostrar gráficos dinámicos usando Chart.js
 * Reemplaza el MetricChart básico con visualizaciones profesionales
 */
@JavaScript("https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.js")
@JavaScript("https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js")
@JavaScript("https://cdn.jsdelivr.net/npm/stomp-websocket@2.3.3/stomp.min.js")
@StyleSheet("./styles/advanced-chart.css")
public class AdvancedMetricChart extends Div {
    
    private final String chartId;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public AdvancedMetricChart() {
        this.chartId = "chart-" + System.currentTimeMillis();
        
        addClassName("advanced-metric-chart");
        setWidth("100%");
        setHeight("400px");
        
        // Crear contenedor para el canvas
        setId(chartId);
        getElement().setProperty("innerHTML", 
            "<canvas id='" + chartId + "-canvas' style='width: 100%; height: 100%;'></canvas>");
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        // Inicializar Chart.js cuando el componente se adjunte al DOM
        getElement().executeJs("""
            // Verificar que Chart.js esté disponible
            if (typeof Chart === 'undefined') {
                console.error('Chart.js no está disponible');
                return;
            }
            
            const ctx = document.getElementById($0 + '-canvas').getContext('2d');
            
            // Configuración del gráfico
            const chartConfig = {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [
                        {
                            label: 'CPU (%)',
                            data: [],
                            borderColor: '#3B82F6',
                            backgroundColor: 'rgba(59, 130, 246, 0.1)',
                            borderWidth: 2,
                            fill: true,
                            tension: 0.3
                        },
                        {
                            label: 'RAM (%)',
                            data: [],
                            borderColor: '#10B981',
                            backgroundColor: 'rgba(16, 185, 129, 0.1)',
                            borderWidth: 2,
                            fill: true,
                            tension: 0.3
                        },
                        {
                            label: 'Disco (%)',
                            data: [],
                            borderColor: '#F59E0B',
                            backgroundColor: 'rgba(245, 158, 11, 0.1)',
                            borderWidth: 2,
                            fill: true,
                            tension: 0.3
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    interaction: {
                        intersect: false,
                        mode: 'index'
                    },
                    plugins: {
                        title: {
                            display: true,
                            text: 'Métricas del Sistema en Tiempo Real',
                            font: {
                                size: 16,
                                weight: 'bold'
                            },
                            color: '#F9FAFB'
                        },
                        legend: {
                            display: true,
                            position: 'top',
                            labels: {
                                color: '#F9FAFB',
                                usePointStyle: true,
                                padding: 20
                            }
                        },
                        tooltip: {
                            backgroundColor: 'rgba(0, 0, 0, 0.8)',
                            titleColor: '#F9FAFB',
                            bodyColor: '#F9FAFB',
                            borderColor: 'rgba(255, 255, 255, 0.2)',
                            borderWidth: 1,
                            displayColors: true,
                            callbacks: {
                                label: function(context) {
                                    return context.dataset.label + ': ' + context.parsed.y.toFixed(1) + '%';
                                }
                            }
                        }
                    },
                    scales: {
                        x: {
                            display: true,
                            title: {
                                display: true,
                                text: 'Tiempo',
                                color: '#F9FAFB'
                            },
                            grid: {
                                color: 'rgba(255, 255, 255, 0.1)',
                                borderColor: 'rgba(255, 255, 255, 0.2)'
                            },
                            ticks: {
                                color: '#F9FAFB',
                                maxTicksLimit: 10
                            }
                        },
                        y: {
                            display: true,
                            title: {
                                display: true,
                                text: 'Uso (%)',
                                color: '#F9FAFB'
                            },
                            min: 0,
                            max: 100,
                            grid: {
                                color: 'rgba(255, 255, 255, 0.1)',
                                borderColor: 'rgba(255, 255, 255, 0.2)'
                            },
                            ticks: {
                                color: '#F9FAFB',
                                callback: function(value) {
                                    return value + '%';
                                }
                            }
                        }
                    },
                    animation: {
                        duration: 1000,
                        easing: 'easeInOutQuart'
                    }
                }
            };
            
            // Crear el gráfico
            window[$0 + '_chart'] = new Chart(ctx, chartConfig);
            
            // Configurar WebSocket para actualizaciones en tiempo real
            const socket = new SockJS('/ws-metrics');
            const stompClient = Stomp.over(socket);
            
            stompClient.connect({}, function(frame) {
                console.log('✅ WebSocket conectado para gráficos en tiempo real');
                
                // Suscribirse a métricas en tiempo real
                stompClient.subscribe('/topic/metrics', function(message) {
                    const metric = JSON.parse(message.body);
                    updateChartWithMetric(metric);
                });
                
                // Suscribirse a alertas
                stompClient.subscribe('/topic/alerts', function(message) {
                    const metric = JSON.parse(message.body);
                    showAlertNotification(metric);
                });
                
            }, function(error) {
                console.error('❌ Error conectando WebSocket:', error);
            });
            
            // Función para actualizar el gráfico con nueva métrica
            function updateChartWithMetric(metric) {
                const chart = window[$0 + '_chart'];
                const now = new Date().toLocaleTimeString();
                
                // Agregar nueva etiqueta de tiempo
                chart.data.labels.push(now);
                
                // Agregar nuevos datos
                chart.data.datasets[0].data.push(metric.cpuUsage);
                chart.data.datasets[1].data.push(metric.memoryUsage);
                chart.data.datasets[2].data.push(metric.diskUsage);
                
                // Mantener solo los últimos 20 puntos para rendimiento
                if (chart.data.labels.length > 20) {
                    chart.data.labels.shift();
                    chart.data.datasets.forEach(dataset => dataset.data.shift());
                }
                
                // Actualizar el gráfico
                chart.update('none'); // Sin animación para tiempo real
            }
            
            // Función para mostrar notificaciones de alerta
            function showAlertNotification(metric) {
                // Cambiar colores de las líneas a rojo cuando hay alerta
                const chart = window[$0 + '_chart'];
                
                if (metric.cpuAlert) {
                    chart.data.datasets[0].borderColor = '#EF4444';
                    chart.data.datasets[0].backgroundColor = 'rgba(239, 68, 68, 0.2)';
                }
                
                if (metric.memoryAlert) {
                    chart.data.datasets[1].borderColor = '#EF4444';
                    chart.data.datasets[1].backgroundColor = 'rgba(239, 68, 68, 0.2)';
                }
                
                if (metric.diskAlert) {
                    chart.data.datasets[2].borderColor = '#EF4444';
                    chart.data.datasets[2].backgroundColor = 'rgba(239, 68, 68, 0.2)';
                }
                
                chart.update();
                
                // Restaurar colores después de 5 segundos
                setTimeout(() => {
                    chart.data.datasets[0].borderColor = '#3B82F6';
                    chart.data.datasets[0].backgroundColor = 'rgba(59, 130, 246, 0.1)';
                    chart.data.datasets[1].borderColor = '#10B981';
                    chart.data.datasets[1].backgroundColor = 'rgba(16, 185, 129, 0.1)';
                    chart.data.datasets[2].borderColor = '#F59E0B';
                    chart.data.datasets[2].backgroundColor = 'rgba(245, 158, 11, 0.1)';
                    chart.update();
                }, 5000);
            }
            
        """, chartId);
    }
    
    /**
     * Actualiza el gráfico con datos históricos (para inicialización)
     */
    public void updateChart(List<SystemMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return;
        }
        
        // Limitar a últimos 20 puntos para rendimiento
        List<SystemMetric> limitedMetrics = metrics.stream()
            .limit(20)
            .collect(Collectors.toList());
        
        // Preparar datos para JavaScript
        String labels = limitedMetrics.stream()
            .map(m -> "\"" + m.getTimestamp().format(timeFormatter) + "\"")
            .collect(Collectors.joining(","));
            
        String cpuData = limitedMetrics.stream()
            .map(m -> String.valueOf(m.getCpuUsage()))
            .collect(Collectors.joining(","));
            
        String memoryData = limitedMetrics.stream()
            .map(m -> String.valueOf(m.getMemoryUsage()))
            .collect(Collectors.joining(","));
            
        String diskData = limitedMetrics.stream()
            .map(m -> String.valueOf(m.getDiskUsage()))
            .collect(Collectors.joining(","));
        
        // Actualizar gráfico con datos históricos
        getElement().executeJs("""
            const chart = window[$0 + '_chart'];
            if (chart) {
                chart.data.labels = [$1];
                chart.data.datasets[0].data = [$2];
                chart.data.datasets[1].data = [$3];
                chart.data.datasets[2].data = [$4];
                chart.update();
            }
        """, chartId, labels, cpuData, memoryData, diskData);
    }
    
    /**
     * Limpia el gráfico
     */
    public void clearChart() {
        getElement().executeJs("""
            const chart = window[$0 + '_chart'];
            if (chart) {
                chart.data.labels = [];
                chart.data.datasets.forEach(dataset => dataset.data = []);
                chart.update();
            }
        """, chartId);
    }
}